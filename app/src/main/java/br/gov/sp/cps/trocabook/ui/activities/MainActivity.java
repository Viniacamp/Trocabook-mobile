package br.gov.sp.cps.trocabook.ui.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import br.gov.sp.cps.trocabook.R;
import br.gov.sp.cps.trocabook.model.Anuncio;
import br.gov.sp.cps.trocabook.model.Usuario;
import br.gov.sp.cps.trocabook.service.AnuncioService;
import br.gov.sp.cps.trocabook.service.AuthService;
import br.gov.sp.cps.trocabook.service.InteracaoService;
import br.gov.sp.cps.trocabook.service.IaService;
import br.gov.sp.cps.trocabook.service.UsuarioService;
import br.gov.sp.cps.trocabook.service.chat.ChatService;
import br.gov.sp.cps.trocabook.ui.adapters.LivroHorizontalAdapter;
import br.gov.sp.cps.trocabook.ui.adapters.ResultadoBuscaAdapter;
import br.gov.sp.cps.trocabook.ui.adapters.UsuarioHorizontalAdapter;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigation;

    private RecyclerView recyclerUsuarios;
    private RecyclerView recyclerLivrosDestaque;
    private RecyclerView recyclerPropostas;
    private RecyclerView recyclerResultadosBusca;

    private View cardResultadosPesquisa;
    private View layoutLoading;
    private View layoutSemResultados;

    private ImageView imgLoading;
    private EditText editPesquisa;

    private UsuarioService usuarioService;
    private AnuncioService anuncioService;
    private IaService iaService;

    private InteracaoService interacaoService;
    private AuthService authService;

    private ResultadoBuscaAdapter resultadoBuscaAdapter;

    private final List<Anuncio> listaResultados =
            new ArrayList<>();
    private final List<Anuncio> anunciosAtivos =
            new ArrayList<>();

    private final Handler handlerPesquisa = new Handler(android.os.Looper.getMainLooper());
    private final ExecutorService executorPesquisa = Executors.newFixedThreadPool(2);
    private Runnable pesquisaRunnable;
    private int versaoPesquisa;
    private int carregamentosIniciais = 3;
    private boolean buscaEmAndamento;
    private boolean anunciosPesquisaCarregados;
    private boolean activityEncerrando;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);

        setContentView(R.layout.activity_main);

        iniciarComponentes();

        FirebaseUser usuarioAtual =
                authService.getUsuarioAtual();

        if (usuarioAtual == null) {
            finish();
            return;
        }

        ActivityResultLauncher<String> requestPermissionLauncher =
                registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                    if (!isGranted) {
                        Toast.makeText(this, "Permissão de notificação negada.", Toast.LENGTH_SHORT).show();
                    }
                });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
                    PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }

        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        android.util.Log.w("FCM_DEBUG", "Falha ao obter token FCM", task.getException());
                        return;
                    }

                    // Token FCM obtido com sucesso
                    String token = task.getResult();
                    android.util.Log.d("FCM_DEBUG", "Token atual: " + token);

                    // Salva no seu banco usando o seu UsuarioService
                    usuarioService.atualizarFcmToken(
                            usuarioAtual.getUid(),
                            token,
                            new UsuarioService.ServiceCallback<Void>() {
                                @Override
                                public void onSucesso(Void data) {
                                    android.util.Log.d("FCM_DEBUG", "Token sincronizado com sucesso!");
                                }

                                @Override
                                public void onErro(String erro) {
                                    android.util.Log.e("FCM_DEBUG", "Erro ao sincronizar token: " + erro);
                                }
                            }
                    );
                });


        configurarRecyclerViews();
        configurarPesquisa();
        configurarBottomNavigation();
        atualizarIndicadorCarregamento();
        carregarDados();
    }

    private void iniciarComponentes() {

        usuarioService =
                new UsuarioService();

        anuncioService =
                new AnuncioService();

        interacaoService = new InteracaoService();

        iaService =
                new IaService();

        authService =
                new AuthService(this);

        bottomNavigation =
                findViewById(R.id.bottomNavigation);

        recyclerUsuarios =
                findViewById(R.id.recyclerUsuarios);

        recyclerLivrosDestaque =
                findViewById(R.id.recyclerLivrosDestaque);

        recyclerPropostas =
                findViewById(R.id.recyclerPropostas);

        recyclerResultadosBusca =
                findViewById(R.id.recyclerPesquisa);

        cardResultadosPesquisa =
                findViewById(R.id.cardResultadosPesquisa);

        layoutLoading =
                findViewById(R.id.layoutLoadingPesquisa);

        layoutSemResultados =
                findViewById(R.id.layoutSemResultados);

        imgLoading =
                findViewById(R.id.imgLoadingPesquisa);

        editPesquisa =
                findViewById(R.id.editPesquisa);
    }

    private void configurarRecyclerViews() {

        recyclerUsuarios.setLayoutManager(
                new LinearLayoutManager(
                        this,
                        LinearLayoutManager.HORIZONTAL,
                        false
                )
        );

        recyclerLivrosDestaque.setLayoutManager(
                new LinearLayoutManager(
                        this,
                        LinearLayoutManager.HORIZONTAL,
                        false
                )
        );

        recyclerPropostas.setLayoutManager(
                new LinearLayoutManager(
                        this,
                        LinearLayoutManager.HORIZONTAL,
                        false
                )
        );

        recyclerResultadosBusca.setLayoutManager(
                new LinearLayoutManager(this)
        );

        resultadoBuscaAdapter =
                new ResultadoBuscaAdapter(
                        listaResultados,
                        anuncio -> {

                            Intent intent =
                                    new Intent(
                                            MainActivity.this,
                                            DetalhesAnuncioActivity.class
                                    );

                            intent.putExtra(
                                    "anuncioId",
                                    anuncio.getId()
                            );

                            startActivity(intent);
                        }
                );

        recyclerResultadosBusca.setAdapter(
                resultadoBuscaAdapter
        );
    }

    private void configurarPesquisa() {
        editPesquisa.addTextChangedListener(
                new android.text.TextWatcher() {

                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        String texto = s.toString().trim();
                        int idPesquisa = ++versaoPesquisa;

                        // 1. Cancela o envio anterior enquanto o usuário continua digitando ("h" -> "ha" -> "har")
                        if (pesquisaRunnable != null) {
                            handlerPesquisa.removeCallbacks(pesquisaRunnable);
                        }

                        if (texto.length() < 2) {
                            buscaEmAndamento = false;
                            cardResultadosPesquisa.setVisibility(View.GONE);
                            listaResultados.clear();
                            resultadoBuscaAdapter.notifyDataSetChanged();
                            atualizarIndicadorCarregamento();
                            return;
                        }

                        // 2. Aguarda o usuário parar de digitar por 600ms antes de buscar e registrar
                        pesquisaRunnable = () -> {
                            // Garante que o texto atual na caixa é exatamente o texto final digitado
                            if (editPesquisa.getText().toString().trim().equals(texto)) {

                                // Realiza a busca visual na tela
                                buscarAnuncios(texto, idPesquisa);

                                // 3. REGISTRA COM SEGURANÇA: Só vai disparar para "harry", ignorando "h", "ha", etc.
                                FirebaseUser usuarioAtual = authService.getUsuarioAtual();
                                if (usuarioAtual != null) {
                                    interacaoService.registrarPesquisa(
                                            usuarioAtual.getUid(),
                                            texto,
                                            new InteracaoService.Callback<br.gov.sp.cps.trocabook.model.Interacao>() {
                                                @Override public void onSuccess(br.gov.sp.cps.trocabook.model.Interacao dados) {}
                                                @Override public void onError(String erro) {
                                                    android.util.Log.e("INTERACAO_LOG", "Erro ao registrar pesquisa: " + erro);
                                                }
                                            }
                                    );
                                }
                            }
                        };

                        // Agenda a execução para 600 milissegundos após o último caractere digitado
                        handlerPesquisa.postDelayed(pesquisaRunnable, 600);
                    }

                    @Override
                    public void afterTextChanged(android.text.Editable s) {}
                }
        );
    }

    private void buscarAnuncios(String texto, int idPesquisa) {

        cardResultadosPesquisa
                .setVisibility(View.VISIBLE);

        layoutSemResultados
                .setVisibility(View.GONE);

        recyclerResultadosBusca
                .setVisibility(View.GONE);

        mostrarLoading(true);

        if (anunciosPesquisaCarregados) {
            processarBusca(new ArrayList<>(anunciosAtivos), texto, idPesquisa);
            return;
        }

        anuncioService.buscarTodosOsAnuncios(
                new AnuncioService.Callback<List<Anuncio>>() {

                    @Override
                    public void onSuccess(
                            List<Anuncio> dados) {
                        if (activityEncerrando || isFinishing() || idPesquisa != versaoPesquisa) {
                            return;
                        }
                        processarBusca(dados, texto, idPesquisa);
                    }

                    @Override
                    public void onError(
                            String erro) {
                        runOnUiThread(() -> {
                            if (activityEncerrando || isFinishing() || idPesquisa != versaoPesquisa) {
                                return;
                            }
                            mostrarLoading(false);
                            layoutSemResultados.setVisibility(View.VISIBLE);
                            android.util.Log.e("HOME_BUSCA", "Erro ao buscar anúncios: " + erro);
                            Toast.makeText(MainActivity.this,
                                    "Não foi possível realizar a busca. Tente novamente.",
                                    Toast.LENGTH_SHORT).show();
                        });
                    }
                }
        );
    }

    private void processarBusca(List<Anuncio> anuncios, String texto, int idPesquisa) {
        executorPesquisa.execute(() -> {
            List<String> termos = iaService.extrairTokensLocais(texto);
            if (texto.trim().split("\\s+").length >= 3 || texto.contains("?")) {
                termos = iaService.extrairTermosBuscaSemantica(texto);
            }
            List<AnuncioPontuado> resultados =
                    ranquearAnuncios(anuncios, texto, termos);

            runOnUiThread(() -> {
                if (activityEncerrando || isFinishing() || idPesquisa != versaoPesquisa) {
                    return;
                }

                listaResultados.clear();
                for (AnuncioPontuado resultado : resultados) {
                    listaResultados.add(resultado.anuncio);
                }

                resultadoBuscaAdapter.notifyDataSetChanged();
                mostrarLoading(false);

                if (listaResultados.isEmpty()) {
                    layoutSemResultados.setVisibility(View.VISIBLE);
                } else {
                    recyclerResultadosBusca.setVisibility(View.VISIBLE);
                }
            });
        });
    }

    private List<AnuncioPontuado> ranquearAnuncios(
            List<Anuncio> anuncios,
            String consulta,
            List<String> termos
    ) {
        List<AnuncioPontuado> resultados = new ArrayList<>();
        String consultaNormalizada = iaService.normalizarTexto(consulta);

        if (anuncios == null) {
            return resultados;
        }

        for (Anuncio anuncio : anuncios) {
            if (anuncio == null) {
                continue;
            }

            String titulo = iaService.normalizarTexto(anuncio.getTitulo());
            String descricao = iaService.normalizarTexto(anuncio.getDescricao());
            int pontuacao = 0;

            if (!consultaNormalizada.isEmpty() && titulo.contains(consultaNormalizada)) {
                pontuacao += 100;
            }
            if (!consultaNormalizada.isEmpty() && descricao.contains(consultaNormalizada)) {
                pontuacao += 40;
            }

            if (termos != null) {
                for (String termo : termos) {
                    String termoNormalizado = iaService.normalizarTexto(termo);
                    if (termoNormalizado.length() < 2) {
                        continue;
                    }
                    if (titulo.contains(termoNormalizado)) {
                        pontuacao += 25;
                    }
                    if (descricao.contains(termoNormalizado)) {
                        pontuacao += 10;
                    }
                }
            }

            if (pontuacao > 0) {
                resultados.add(new AnuncioPontuado(anuncio, pontuacao));
            }
        }

        resultados.sort((primeiro, segundo) ->
                Integer.compare(segundo.pontuacao, primeiro.pontuacao));
        return resultados;
    }

    private static class AnuncioPontuado {
        private final Anuncio anuncio;
        private final int pontuacao;

        private AnuncioPontuado(Anuncio anuncio, int pontuacao) {
            this.anuncio = anuncio;
            this.pontuacao = pontuacao;
        }
    }

    private void carregarDados() {
        android.util.Log.d(
                "HOME_DEBUG",
                ">>> carregarDados() foi chamado"
        );

        carregarUsuarios();

        carregarAnuncios();

        carregarLivrosRecentes();
    }

    private void carregarUsuarios() {

        usuarioService.buscarUsuarios(
                new UsuarioService.ServiceCallback<List<Usuario>>() {

                    @Override
                    public void onSucesso(
                            List<Usuario> dados) {

                        runOnUiThread(() -> {
                            if (!activityEncerrando && !isFinishing() && dados != null) {
                                UsuarioHorizontalAdapter adapter =
                                        new UsuarioHorizontalAdapter(
                                            dados,
                                            usuario -> {

                                                FirebaseUser usuarioAtual =
                                                        authService
                                                                .getUsuarioAtual();

                                                if (usuarioAtual == null) {
                                                    return;
                                                }

                                                Intent intent;

                                                if (usuario.getId()
                                                        .equals(
                                                                usuarioAtual
                                                                        .getUid()
                                                        )) {

                                                    intent =
                                                            new Intent(
                                                                    MainActivity.this,
                                                                    MeusLivrosActivity.class
                                                            );

                                                } else {

                                                    intent =
                                                            new Intent(
                                                                    MainActivity.this,
                                                                    TelaPerfilNegociadorActivity.class
                                                            );

                                                    intent.putExtra(
                                                            "userId",
                                                            usuario.getId()
                                                    );
                                                }

                                                startActivity(intent);
                                            }
                                        );
                                recyclerUsuarios.setAdapter(adapter);
                            }
                            finalizarCarregamentoInicial();
                        });
                    }

                    @Override
                    public void onErro(
                            String erro) {
                        android.util.Log.e("HOME_DEBUG", "Erro ao carregar usuários: " + erro);
                        runOnUiThread(() -> {
                            if (!activityEncerrando && !isFinishing()) {
                                finalizarCarregamentoInicial();
                            }
                        });
                    }
                }
        );
    }

    private void carregarAnuncios() {
        android.util.Log.d(
                "HOME_DEBUG",
                ">>> carregarAnuncios() foi chamado"
        );


        anuncioService.buscarTodosOsAnuncios(
                new AnuncioService.Callback<List<Anuncio>>() {


                    @Override
                    public void onSuccess(
                            List<Anuncio> dados) {
                        android.util.Log.d(
                                "HOME_DEBUG",
                                ">>> buscarTodosOsAnuncios SUCCESS. Quantidade: "
                                        + (dados == null ? "null" : dados.size())
                        );


                        if (dados != null) {
                            for (Anuncio anuncio : dados) {
                                android.util.Log.d(
                                        "HOME_CAPA",
                                        "Título: " + anuncio.getTitulo()
                                                + " | Capa: " + anuncio.getCapa()
                                );
                            }
                        }


                        final List<Anuncio> anuncios = dados == null
                                ? new ArrayList<>()
                                : new ArrayList<>(dados.subList(
                                        0,
                                        Math.min(10, dados.size())
                                ));

                        runOnUiThread(() -> {
                            if (activityEncerrando || isFinishing()) {
                                return;
                            }
                            anunciosAtivos.clear();
                            if (dados != null) {
                                anunciosAtivos.addAll(dados);
                            }
                            anunciosPesquisaCarregados = true;

                            LivroHorizontalAdapter<Anuncio> adapter =
                                    new LivroHorizontalAdapter<>(
                                            anuncios,
                                            anuncio -> {

                                                Intent intent =
                                                        new Intent(
                                                                MainActivity.this,
                                                                DetalhesAnuncioActivity.class
                                                        );

                                                intent.putExtra(
                                                        "anuncioId",
                                                        anuncio.getId()
                                                );

                                                startActivity(intent);
                                            }
                                    );

                            recyclerLivrosDestaque
                                    .setAdapter(adapter);

                            finalizarCarregamentoInicial();
                        });
                    }

                    @Override
                    public void onError(
                            String erro) {
                        android.util.Log.e(
                                "HOME_DEBUG",
                                ">>> ERRO ao carregar anúncios: " + erro
                        );

                        runOnUiThread(() -> {
                            if (!activityEncerrando && !isFinishing()) {
                                finalizarCarregamentoInicial();
                            }
                        });
                    }
                }
        );
    }

    private void carregarLivrosRecentes() {
        anuncioService.buscarAnunciosMaisRecentes(
                new AnuncioService.Callback<List<Anuncio>>() {

                    @Override
                    public void onSuccess(
                            List<Anuncio> dados) {
                        runOnUiThread(() -> {
                            if (activityEncerrando || isFinishing()) {
                                return;
                            }
                            LivroHorizontalAdapter<Anuncio> adapter =
                                    new LivroHorizontalAdapter<>(
                                            dados != null ? dados : new ArrayList<>(),
                                            anuncio -> {
                                                Intent intent = new Intent(
                                                        MainActivity.this,
                                                        DetalhesAnuncioActivity.class
                                                );
                                                intent.putExtra("anuncioId", anuncio.getId());
                                                startActivity(intent);
                                            }
                                    );
                            recyclerPropostas.setAdapter(adapter);
                            finalizarCarregamentoInicial();
                        });
                    }

                    @Override
                    public void onError(
                            String erro) {
                        android.util.Log.e("HOME_DEBUG", "Erro ao carregar anúncios recentes: " + erro);
                        runOnUiThread(() -> {
                            if (!activityEncerrando && !isFinishing()) {
                                finalizarCarregamentoInicial();
                            }
                        });
                    }
                }
        );
    }

    private void mostrarLoading(boolean mostrar) {
        buscaEmAndamento = mostrar;
        if (mostrar) {
            recyclerResultadosBusca
                    .setVisibility(View.GONE);
        }
        atualizarIndicadorCarregamento();
    }

    private void finalizarCarregamentoInicial() {
        if (carregamentosIniciais > 0) {
            carregamentosIniciais--;
        }
        atualizarIndicadorCarregamento();
    }

    private void atualizarIndicadorCarregamento() {
        boolean mostrar = carregamentosIniciais > 0 || buscaEmAndamento;
        layoutLoading.setVisibility(mostrar ? View.VISIBLE : View.GONE);

        if (imgLoading == null) {
            return;
        }
        if (mostrar) {
            Animation rotate = AnimationUtils.loadAnimation(this, R.anim.rotacao_loading);
            imgLoading.startAnimation(rotate);
        } else {
            imgLoading.clearAnimation();
        }
    }

    @Override
    protected void onDestroy() {
        activityEncerrando = true;
        versaoPesquisa++;
        handlerPesquisa.removeCallbacksAndMessages(null);
        executorPesquisa.shutdownNow();
        super.onDestroy();
    }

    private void configurarBottomNavigation() {

        bottomNavigation.setSelectedItemId(
                R.id.menu_home
        );

        bottomNavigation.setOnItemSelectedListener(
                item -> {

                    int id =
                            item.getItemId();

                    Class<?> tela = null;

                    if (id == R.id.menu_home) {

                        return true;

                    } else if (id == R.id.menu_anuncio) {

                        tela =
                                BuscaActivity.class;

                    } else if (id == R.id.menu_livros) {

                        tela =
                                MeusLivrosActivity.class;

                    } else if (id == R.id.menu_chat) {

                        tela =
                                ConversasActivity.class;

                    } else if (id == R.id.menu_catalogo) {

                        tela = CatalogoActivity.class;
                    }

                    if (tela != null) {

                        Intent intent =
                                new Intent(
                                        this,
                                        tela
                                );

                        intent.setFlags(
                                Intent.FLAG_ACTIVITY_CLEAR_TOP
                                        | Intent.FLAG_ACTIVITY_SINGLE_TOP
                        );

                        startActivity(intent);
                    }

                    return true;
                }
        );
    }

    @Override
    protected void onResume() {

        super.onResume();

        if (bottomNavigation != null) {

            bottomNavigation.getMenu()
                    .findItem(
                            R.id.menu_home
                    )
                    .setChecked(true);
        }
    }
}