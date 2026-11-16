package br.gov.sp.cps.trocabook.service;

import android.os.Looper;
import android.os.Handler;
import android.util.Log;

import java.io.IOException;
import java.text.Normalizer;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

import br.gov.sp.cps.trocabook.BuildConfig;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;

/**
 * Cliente da Groq. Chamadas síncronas que consultam a API devem ocorrer fora da thread principal.
 */
public class IaService {

    private static final String TAG = "IaService";
    private static final String GROQ_BASE_URL = "https://api.groq.com/openai/v1/";
    private static final List<String> MODELOS_GROQ = Arrays.asList(
            "openai/gpt-oss-120b",
            "openai/gpt-oss-20b",
            "qwen/qwen3.8-27b"
    );
    private static final Set<String> STOP_WORDS = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
            "a", "o", "as", "os", "um", "uma", "uns", "umas", "de", "do", "da", "dos", "das",
            "em", "no", "na", "nos", "nas", "por", "para", "com", "sem", "sob", "sobre",
            "e", "ou", "mas", "que", "qual", "quais", "quero", "queria", "busco", "procuro",
            "livro", "livros", "obra", "obras", "historia", "algo", "algum", "alguma",
            "parecido", "parecidos", "tipo", "estilo", "ler", "leitura", "bom", "boa", "muito"
    )));

    private final GroqApi api;
    private final String apiKey;

    public IaService() {
        this(BuildConfig.GROQ_API_KEY);
    }

    IaService(String apiKey) {
        this.apiKey = apiKey == null ? "" : apiKey.trim();
        this.api = new Retrofit.Builder()
                .baseUrl(GROQ_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(GroqApi.class);
    }

    public String gerarDescricaoAnuncio(
            String titulo,
            String autores,
            String categorias,
            String tipoNegociacao,
            String rascunhoUsuario
    ) {
        if (!apiKey.isEmpty()) {
            String detalhesExtras = valorOuPadrao(
                    rascunhoUsuario,
                    "O dono não informou detalhes físicos específicos. Foque na obra e convide interessados a perguntar sobre o estado do exemplar."
            );
            String prompt = String.format(Locale.ROOT, """
                    Você é um assistente da plataforma Trocabook, uma comunidade brasileira de troca e venda de livros usados entre leitores.
                    Escreva uma descrição curta, acolhedora e persuasiva em português do Brasil para o anúncio de um livro.

                    Dados do livro:
                    - Título: %s
                    - Autor(es): %s
                    - Categorias: %s
                    - Modalidade: %s
                    - Observações do dono: %s

                    Regras:
                    - Use no máximo 2 parágrafos curtos e 380 caracteres no total.
                    - Traduza naturalmente categorias em inglês para português.
                    - Não use markdown, asteriscos, hashtags ou listas.
                    - Não invente estado de conservação ou detalhes não informados.
                    - Retorne somente o texto da descrição.
                    """,
                    valorOuPadrao(titulo, "Não informado"),
                    valorOuPadrao(autores, "Não informado"),
                    valorOuPadrao(categorias, "Geral"),
                    valorOuPadrao(tipoNegociacao, "Troca ou venda"),
                    detalhesExtras
            );

            try {
                return limitarDescricao(chamarIa(prompt));
            } catch (IOException | RuntimeException e) {
                Log.w(TAG, "Falha ao consultar Groq para a descrição; usando o fallback local.");
            }
        }

        return limitarDescricao(gerarDescricaoFallbackLocal(
                titulo, autores, categorias, tipoNegociacao, rascunhoUsuario
        ));
    }

    public List<String> identificarTitulosParaAnuncio(String consultaUsuario) {
        if (estaVazio(consultaUsuario)) {
            return Collections.emptyList();
        }

        if (!apiKey.isEmpty()) {
            String prompt = String.format(Locale.ROOT, """
                    O usuário está cadastrando um livro na plataforma Trocabook e digitou: "%s".
                    Se for um título exato, retorne esse título. Se for uma descrição de enredo, tema, autor ou pista, identifique até 3 títulos reais de livros compatíveis.

                    Regras:
                    - Retorne somente os títulos, separados por ponto e vírgula (;).
                    - Não inclua numeração, autores, aspas, explicações ou texto extra.
                    - Exemplo: Dom Casmurro; Memórias Póstumas de Brás Cubas
                    """, consultaUsuario.trim());

            try {
                String resposta = chamarIa(prompt);
                List<String> titulos = Arrays.stream(resposta.split("[;\\n]+"))
                        .map(titulo -> titulo.replaceAll("^[-*\\d.\\s\"']+|[\"']+$", "").trim())
                        .filter(titulo -> !estaVazio(titulo) && titulo.length() <= 120)
                        .distinct()
                        .limit(3)
                        .collect(Collectors.toList());
                if (!titulos.isEmpty()) {
                    return titulos;
                }
            } catch (IOException | RuntimeException e) {
                Log.w(TAG, "Falha ao consultar Groq para identificar títulos; usando o fallback local.");
            }
        }

        return Collections.singletonList(consultaUsuario.trim());
    }

    public List<String> extrairTermosBuscaSemantica(String consultaUsuario) {
        if (estaVazio(consultaUsuario)) {
            return Collections.emptyList();
        }

        Set<String> termos = new LinkedHashSet<>(removerStopWords(consultaUsuario));

        if (!apiKey.isEmpty()) {
            String prompt = String.format(Locale.ROOT, """
                    Você é o mecanismo de busca semântica do Trocabook, uma plataforma brasileira de troca e venda de livros.
                    A consulta do usuário é: "%s"

                    Gere de 5 a 10 palavras-chave curtas para localizar livros compatíveis: temas, sinônimos, gêneros em português e inglês, autores ou títulos relacionados.
                    Retorne somente as palavras-chave, separadas por vírgula, sem frases, numeração ou explicações.
                    Exemplo: suspense, thriller, mistério, investigação, ficção, fiction, Agatha Christie
                    """, consultaUsuario.trim());

            try {
                Arrays.stream(chamarIa(prompt).split("[,;\\n]+"))
                        .map(this::normalizarTexto)
                        .filter(termo -> termo.length() >= 2 && termo.length() <= 50)
                        .limit(12)
                        .forEach(termos::add);
            } catch (IOException | RuntimeException e) {
                Log.w(TAG, "Falha ao expandir a busca com Groq; usando os termos locais.");
            }
        }

        return new ArrayList<>(termos);
    }

    public String normalizarTexto(String texto) {
        if (texto == null) {
            return "";
        }
        return Normalizer.normalize(texto, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
                .toLowerCase(Locale.ROOT)
                .trim();
    }

    public List<String> removerStopWords(String texto) {
        if (estaVazio(texto)) {
            return Collections.emptyList();
        }
        return Arrays.stream(normalizarTexto(texto).split("[^a-z0-9]+"))
                .filter(token -> token.length() >= 2 && !STOP_WORDS.contains(token))
                .distinct()
                .collect(Collectors.toList());
    }

    public List<String> extrairTokensLocais(String texto) {
        return removerStopWords(texto);
    }

    public void gerarDescricaoAnuncioAsync(
            Executor executor,
            String titulo,
            String autores,
            String categorias,
            String tipoNegociacao,
            String rascunhoUsuario,
            DescricaoCallback callback
    ) {
        Handler mainHandler = new Handler(Looper.getMainLooper());
        executor.execute(() -> {
            try {
                String descricao = gerarDescricaoAnuncio(
                        titulo,
                        autores,
                        categorias,
                        tipoNegociacao,
                        rascunhoUsuario
                );
                mainHandler.post(() -> callback.onSuccess(descricao));
            } catch (RuntimeException e) {
                mainHandler.post(() -> callback.onError(e));
            }
        });
    }

    private String chamarIa(String prompt) throws IOException {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            throw new IllegalStateException("As chamadas ao Groq devem ser feitas fora da thread principal.");
        }

        IOException ultimoErro = null;

        for (String modelo : MODELOS_GROQ) {
            try {
                ChatRequest request = new ChatRequest(
                        modelo,
                        0.4,
                        250,
                        Collections.singletonList(new ChatMessage("user", prompt))
                );
                Response<ChatResponse> response = api.completar(
                        "Bearer " + apiKey,
                        request
                ).execute();

                if (!response.isSuccessful() || response.body() == null) {
                    ultimoErro = new IOException("Resposta HTTP inválida da API Groq.");
                    continue;
                }

                ChatResponse body = response.body();
                if (body.choices != null && !body.choices.isEmpty()
                        && body.choices.get(0).message != null
                        && body.choices.get(0).message.content != null
                        && !estaVazio(body.choices.get(0).message.content)) {
                    return body.choices.get(0).message.content.trim();
                }
                ultimoErro = new IOException("A API Groq retornou uma resposta vazia.");
            } catch (IOException | RuntimeException e) {
                ultimoErro = e instanceof IOException
                        ? (IOException) e
                        : new IOException("Falha ao consultar a API Groq.", e);
            }
        }

        throw new IOException("Não foi possível consultar os modelos da API Groq.", ultimoErro);
    }

    private String gerarDescricaoFallbackLocal(
            String titulo,
            String autores,
            String categorias,
            String tipoNegociacao,
            String rascunhoUsuario
    ) {
        StringBuilder descricao = new StringBuilder("Estou disponibilizando o livro \"")
                .append(valorOuPadrao(titulo, "Livro").replaceAll("\\s+", " "))
                .append("\"");

        if (!estaVazio(autores)) {
            descricao.append(", de ").append(autores.trim().replaceAll("\\s+", " "));
        }
        descricao.append(".");

        if (!estaVazio(categorias)) {
            String categoria = categorias.trim().replaceAll("(?i)\\bFiction\\b", "Ficção");
            descricao.append(" Uma ótima leitura para quem gosta de ")
                    .append(categoria.replaceAll("\\s+", " "))
                    .append(".");
        }

        descricao.append("\n\n");
        if (!estaVazio(rascunhoUsuario)) {
            String observacao = rascunhoUsuario.trim().replaceAll("\\s+", " ");
            descricao.append(observacao);
            if (!observacao.endsWith(".") && !observacao.endsWith("!")) {
                descricao.append(".");
            }
            descricao.append(" ");
        }

        String modalidade = tipoNegociacao == null
                ? ""
                : tipoNegociacao.trim().toUpperCase(Locale.ROOT);
        switch (modalidade) {
            case "TROCA" -> descricao.append("Disponível para troca. Chame no chat para combinar!");
            case "VENDA" -> descricao.append("Disponível para venda. Chame no chat para saber mais!");
            case "AMBOS" -> descricao.append("Disponível para troca ou venda. Envie sua proposta pelo chat!");
            default -> descricao.append("Chame no chat para saber mais ou fazer uma proposta!");
        }
        return descricao.toString();
    }

    private String limitarDescricao(String texto) {
        if (estaVazio(texto)) {
            return "";
        }

        String semMarkdown = texto.trim()
                .replace("*", "")
                .replaceAll("(?m)^\\s*#+\\s*", "")
                .replaceAll("(?m)^\\s*[-+]\\s+", "");
        String[] paragrafos = semMarkdown.split("\\R\\s*\\R");
        String descricao = String.join("\n\n",
                Arrays.stream(paragrafos)
                        .map(String::trim)
                        .filter(paragrafo -> !paragrafo.isEmpty())
                        .limit(2)
                        .collect(Collectors.toList())
        );
        if (descricao.length() > 380) {
            descricao = descricao.substring(0, 379).trim() + "…";
        }
        return descricao;
    }

    private String valorOuPadrao(String valor, String padrao) {
        return estaVazio(valor) ? padrao : valor.trim();
    }

    private boolean estaVazio(String texto) {
        return texto == null || texto.trim().isEmpty();
    }

    public interface DescricaoCallback {
        void onSuccess(String descricao);
        void onError(Exception erro);
    }

    private interface GroqApi {
        @POST("chat/completions")
        Call<ChatResponse> completar(
                @Header("Authorization") String authorization,
                @Body ChatRequest request
        );
    }

    private static class ChatRequest {
        final String model;
        final double temperature;
        final int max_tokens;
        final List<ChatMessage> messages;

        ChatRequest(String model, double temperature, int maxTokens, List<ChatMessage> messages) {
            this.model = model;
            this.temperature = temperature;
            this.max_tokens = maxTokens;
            this.messages = messages;
        }
    }

    private static class ChatMessage {
        final String role;
        final String content;

        ChatMessage(String role, String content) {
            this.role = role;
            this.content = content;
        }
    }

    private static class ChatResponse {
        List<ChatChoice> choices;
    }

    private static class ChatChoice {
        ChatMessage message;
    }
}
