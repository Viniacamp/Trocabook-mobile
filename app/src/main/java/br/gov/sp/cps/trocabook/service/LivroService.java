package br.gov.sp.cps.trocabook.service;

import android.net.Uri;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import br.gov.sp.cps.trocabook.adapter.GoogleAPIBooksAdapter;
import br.gov.sp.cps.trocabook.model.Autor;
import br.gov.sp.cps.trocabook.model.Categoria;
import br.gov.sp.cps.trocabook.model.Livro;
import br.gov.sp.cps.trocabook.model.dto.GoogleBooksResponse;
import br.gov.sp.cps.trocabook.model.dto.LivroBuscaOutput;
import br.gov.sp.cps.trocabook.repository.AutorRepository;
import br.gov.sp.cps.trocabook.repository.CategoriaRepository;
import br.gov.sp.cps.trocabook.repository.LivroRepository;
import br.gov.sp.cps.trocabook.service.googlebooks.GoogleBooksService;

public class LivroService {

    private final LivroRepository livroRepository;
    private final AutorRepository autorRepository;
    private final CategoriaRepository categoriaRepository;
    private final GoogleBooksService googleBooksService;
    private final GoogleAPIBooksAdapter googleAPIBooksAdapter;

    public interface Callback<T> {
        void onSucesso(T dados);
        void onErro(Exception e);
    }

    public LivroService() {

        this.livroRepository =
                new LivroRepository();

        this.autorRepository =
                new AutorRepository();

        this.categoriaRepository =
                new CategoriaRepository();

        this.googleBooksService =
                new GoogleBooksService();

        this.googleAPIBooksAdapter =
                new GoogleAPIBooksAdapter();
    }

    /**
     * Busca livros utilizando a API do Google Books.
     */
    public void buscarLivros(
            String titulo,
            Callback<List<LivroBuscaOutput>> callback) {

        if (titulo == null
                || titulo.trim().isEmpty()) {

            callback.onErro(
                    new IllegalArgumentException(
                            "O título do livro não pode ser vazio"
                    )
            );
            return;
        }

        googleBooksService.buscarTitulo(
                titulo.trim(),
                new GoogleBooksService.Callback<GoogleBooksResponse>() {

                    @Override
                    public void onSucesso(
                            GoogleBooksResponse resposta) {

                        try {

                            List<LivroBuscaOutput> livros =
                                    googleAPIBooksAdapter
                                            .adaptar(resposta);

                            callback.onSucesso(livros);

                        } catch (Exception e) {

                            callback.onErro(e);
                        }
                    }

                    @Override
                    public void onErro(
                            String erro) {

                        callback.onErro(
                                new RuntimeException(erro)
                        );
                    }
                }
        );
    }

    /**
     * Cadastra no Firebase um livro proveniente do Google Books.
     *
     * Antes de criar um novo documento, verifica se o
     * googleBooksId já está cadastrado.
     */
    public void cadastrarLivroGoogleBooks(
            LivroBuscaOutput livroOutput,
            Callback<Livro> callback) {

        if (livroOutput == null) {

            callback.onErro(
                    new IllegalArgumentException(
                            "Livro inválido"
                    )
            );
            return;
        }

        String googleBooksId =
                livroOutput.getGoogleBooksId();

        if (googleBooksId == null
                || googleBooksId.trim().isEmpty()) {

            callback.onErro(
                    new IllegalArgumentException(
                            "Google Books ID inválido"
                    )
            );
            return;
        }

        livroRepository.buscarPorGoogleBooksId(
                googleBooksId,
                new LivroRepository.Callback<Livro>() {

                    @Override
                    public void onSucesso(
                            Livro livroExistente) {

                        if (livroExistente != null) {

                            callback.onSucesso(
                                    livroExistente
                            );
                            return;
                        }

                        criarLivroGoogleBooks(
                                livroOutput,
                                callback
                        );
                    }

                    @Override
                    public void onErro(
                            Exception e) {

                        callback.onErro(e);
                    }
                }
        );
    }

    /**
     * Cria um Livro a partir dos dados retornados
     * pelo Google Books.
     */
    private void criarLivroGoogleBooks(
            LivroBuscaOutput livroOutput,
            Callback<Livro> callback) {

        List<String> autores =
                limparLista(
                        livroOutput.getAutores()
                );

        List<String> categorias =
                limparLista(
                        livroOutput.getCategorias()
                );

        resolverAutores(
                autores,
                0,
                new ArrayList<>(),
                new Callback<List<String>>() {

                    @Override
                    public void onSucesso(
                            List<String> idsAutores) {

                        resolverCategorias(
                                categorias,
                                0,
                                new ArrayList<>(),
                                new Callback<List<String>>() {

                                    @Override
                                    public void onSucesso(
                                            List<String> idsCategorias) {

                                        Livro livro =
                                                new Livro();

                                        livro.setId(
                                                UUID.randomUUID()
                                                        .toString()
                                        );

                                        livro.setGoogleBooksId(
                                                livroOutput.getGoogleBooksId()
                                        );

                                        livro.setTitulo(
                                                livroOutput.getTitulo()
                                        );

                                        livro.setIdsAutores(
                                                idsAutores
                                        );

                                        livro.setIdsCategorias(
                                                idsCategorias
                                        );

                                        livro.setPublicadora(
                                                livroOutput
                                                        .getPublicadora()
                                        );

                                        livro.setDataPublicacao(
                                                livroOutput
                                                        .getDataPublicacao()
                                        );

                                        livro.setUrlImagem(
                                                livroOutput.getUrlImagem()
                                        );

                                        salvarLivroNoBanco(
                                                livro,
                                                callback
                                        );
                                    }

                                    @Override
                                    public void onErro(
                                            Exception e) {

                                        callback.onErro(e);
                                    }
                                }
                        );
                    }

                    @Override
                    public void onErro(
                            Exception e) {

                        callback.onErro(e);
                    }
                }
        );
    }

    /**
     * Salva um livro criado manualmente pelo usuário.
     */
    public void salvarLivro(
            String titulo,
            List<String> autores,
            List<String> categorias,
            Uri imageUri,
            FileService fileService,
            Callback<Livro> callback) {

        if (titulo == null
                || titulo.trim().isEmpty()) {

            callback.onErro(
                    new IllegalArgumentException(
                            "O título do livro é obrigatório"
                    )
            );
            return;
        }

        if (imageUri == null) {

            callback.onErro(
                    new IllegalArgumentException(
                            "A imagem do livro é obrigatória"
                    )
            );
            return;
        }

        if (fileService == null) {

            callback.onErro(
                    new IllegalArgumentException(
                            "FileService não pode ser nulo"
                    )
            );
            return;
        }

        List<String> autoresLimpos =
                limparLista(autores);

        List<String> categoriasLimpos =
                limparLista(categorias);

        fileService.salvarImagem(
                imageUri,
                new FileService.Callback<String>() {

                    @Override
                    public void onSuccess(
                            String caminhoImagem) {

                        resolverAutores(
                                autoresLimpos,
                                0,
                                new ArrayList<>(),
                                new Callback<List<String>>() {

                                    @Override
                                    public void onSucesso(
                                            List<String> idsAutores) {

                                        resolverCategorias(
                                                categoriasLimpos,
                                                0,
                                                new ArrayList<>(),
                                                new Callback<List<String>>() {

                                                    @Override
                                                    public void onSucesso(
                                                            List<String> idsCategorias) {

                                                        Livro livro =
                                                                new Livro();

                                                        livro.setId(
                                                                UUID.randomUUID()
                                                                        .toString()
                                                        );

                                                        livro.setGoogleBooksId(
                                                                null
                                                        );

                                                        livro.setTitulo(
                                                                titulo.trim()
                                                        );

                                                        livro.setIdsAutores(
                                                                idsAutores
                                                        );

                                                        livro.setIdsCategorias(
                                                                idsCategorias
                                                        );

                                                        livro.setUrlImagem(
                                                                caminhoImagem
                                                        );

                                                        salvarLivroNoBanco(
                                                                livro,
                                                                callback
                                                        );
                                                    }

                                                    @Override
                                                    public void onErro(
                                                            Exception e) {

                                                        fileService
                                                                .deletarImagem(
                                                                        caminhoImagem
                                                                );

                                                        callback.onErro(e);
                                                    }
                                                }
                                        );
                                    }

                                    @Override
                                    public void onErro(
                                            Exception e) {

                                        fileService
                                                .deletarImagem(
                                                        caminhoImagem
                                                );

                                        callback.onErro(e);
                                    }
                                }
                        );
                    }

                    @Override
                    public void onError(
                            String erro) {

                        callback.onErro(
                                new RuntimeException(erro)
                        );
                    }
                }
        );
    }

    /**
     * Salva um livro utilizando uma imagem localizada
     * nos assets do aplicativo.
     *
     * Esse método é utilizado principalmente pelo
     * PopularBancoService.
     */
    public void salvarLivroAssets(
            String titulo,
            List<String> autores,
            List<String> categorias,
            String caminhoImagem,
            FileService fileService,
            Callback<Livro> callback) {

        if (titulo == null
                || titulo.trim().isEmpty()) {

            callback.onErro(
                    new IllegalArgumentException(
                            "O título do livro é obrigatório"
                    )
            );
            return;
        }

        if (fileService == null) {

            callback.onErro(
                    new IllegalArgumentException(
                            "FileService não pode ser nulo"
                    )
            );
            return;
        }

        if (caminhoImagem == null
                || caminhoImagem.trim().isEmpty()) {

            callback.onErro(
                    new IllegalArgumentException(
                            "Caminho da imagem inválido"
                    )
            );
            return;
        }

        List<String> autoresLimpos =
                limparLista(autores);

        List<String> categoriasLimpos =
                limparLista(categorias);

        fileService.salvarImagemAsset(
                caminhoImagem,
                new FileService.Callback<String>() {

                    @Override
                    public void onSuccess(
                            String caminhoImagemSalva) {

                        resolverAutores(
                                autoresLimpos,
                                0,
                                new ArrayList<>(),
                                new Callback<List<String>>() {

                                    @Override
                                    public void onSucesso(
                                            List<String> idsAutores) {

                                        resolverCategorias(
                                                categoriasLimpos,
                                                0,
                                                new ArrayList<>(),
                                                new Callback<List<String>>() {

                                                    @Override
                                                    public void onSucesso(
                                                            List<String> idsCategorias) {

                                                        Livro livro =
                                                                new Livro();

                                                        livro.setId(
                                                                UUID.randomUUID()
                                                                        .toString()
                                                        );

                                                        livro.setGoogleBooksId(
                                                                null
                                                        );

                                                        livro.setTitulo(
                                                                titulo.trim()
                                                        );

                                                        livro.setIdsAutores(
                                                                idsAutores
                                                        );

                                                        livro.setIdsCategorias(
                                                                idsCategorias
                                                        );

                                                        livro.setUrlImagem(
                                                                caminhoImagemSalva
                                                        );

                                                        salvarLivroNoBanco(
                                                                livro,
                                                                callback
                                                        );
                                                    }

                                                    @Override
                                                    public void onErro(
                                                            Exception e) {

                                                        fileService
                                                                .deletarImagem(
                                                                        caminhoImagemSalva
                                                                );

                                                        callback.onErro(e);
                                                    }
                                                }
                                        );
                                    }

                                    @Override
                                    public void onErro(
                                            Exception e) {

                                        fileService
                                                .deletarImagem(
                                                        caminhoImagemSalva
                                                );

                                        callback.onErro(e);
                                    }
                                }
                        );
                    }

                    @Override
                    public void onError(
                            String erro) {

                        callback.onErro(
                                new RuntimeException(erro)
                        );
                    }
                }
        );
    }

    /**
     * Resolve os autores pelo nome.
     *
     * Se o autor já existir, utiliza seu ID.
     * Caso contrário, cria um novo autor.
     */
    private void resolverAutores(
            List<String> nomes,
            int indice,
            List<String> ids,
            Callback<List<String>> callback) {

        if (indice >= nomes.size()) {

            callback.onSucesso(ids);
            return;
        }

        String nome =
                nomes.get(indice);

        autorRepository.buscarPorNome(
                nome,
                new AutorRepository.Callback<Autor>() {

                    @Override
                    public void onSucesso(
                            Autor autor) {

                        if (autor != null) {

                            ids.add(
                                    autor.getId()
                            );

                            resolverAutores(
                                    nomes,
                                    indice + 1,
                                    ids,
                                    callback
                            );

                            return;
                        }

                        Autor novoAutor =
                                new Autor();

                        novoAutor.setId(
                                UUID.randomUUID()
                                        .toString()
                        );

                        novoAutor.setNome(
                                nome
                        );

                        autorRepository.salvar(
                                novoAutor,
                                new AutorRepository.Callback<Autor>() {

                                    @Override
                                    public void onSucesso(
                                            Autor autorSalvo) {

                                        ids.add(
                                                autorSalvo.getId()
                                        );

                                        resolverAutores(
                                                nomes,
                                                indice + 1,
                                                ids,
                                                callback
                                        );
                                    }

                                    @Override
                                    public void onErro(
                                            Exception e) {

                                        callback.onErro(e);
                                    }
                                }
                        );
                    }

                    @Override
                    public void onErro(
                            Exception e) {

                        callback.onErro(e);
                    }
                }
        );
    }

    /**
     * Resolve as categorias pelo nome.
     */
    private void resolverCategorias(
            List<String> nomes,
            int indice,
            List<String> ids,
            Callback<List<String>> callback) {

        if (indice >= nomes.size()) {

            callback.onSucesso(ids);
            return;
        }

        String nome =
                nomes.get(indice);

        categoriaRepository.buscarPorNome(
                nome,
                new CategoriaRepository.Callback<Categoria>() {

                    @Override
                    public void onSucesso(
                            Categoria categoria) {

                        if (categoria != null) {

                            ids.add(
                                    categoria.getId()
                            );

                            resolverCategorias(
                                    nomes,
                                    indice + 1,
                                    ids,
                                    callback
                            );

                            return;
                        }

                        Categoria novaCategoria =
                                new Categoria();

                        novaCategoria.setId(
                                UUID.randomUUID()
                                        .toString()
                        );

                        novaCategoria.setNome(
                                nome
                        );

                        categoriaRepository.salvar(
                                novaCategoria,
                                new CategoriaRepository.Callback<Categoria>() {

                                    @Override
                                    public void onSucesso(
                                            Categoria categoriaSalva) {

                                        ids.add(
                                                categoriaSalva.getId()
                                        );

                                        resolverCategorias(
                                                nomes,
                                                indice + 1,
                                                ids,
                                                callback
                                        );
                                    }

                                    @Override
                                    public void onErro(
                                            Exception e) {

                                        callback.onErro(e);
                                    }
                                }
                        );
                    }

                    @Override
                    public void onErro(
                            Exception e) {

                        callback.onErro(e);
                    }
                }
        );
    }

    /**
     * Busca um livro pelo ID do documento.
     */
    public void buscarPorId(
            String id,
            Callback<Livro> callback) {

        if (id == null
                || id.trim().isEmpty()) {

            callback.onErro(
                    new IllegalArgumentException(
                            "ID do livro inválido"
                    )
            );
            return;
        }

        livroRepository.buscarPorUid(
                id,
                new LivroRepository.Callback<Livro>() {

                    @Override
                    public void onSucesso(
                            Livro livro) {

                        callback.onSucesso(
                                livro
                        );
                    }

                    @Override
                    public void onErro(
                            Exception e) {

                        callback.onErro(e);
                    }
                }
        );
    }

    /**
     * Busca todos os livros cadastrados.
     */
    public void buscarTodos(
            Callback<List<Livro>> callback) {

        livroRepository.buscarTodos(
                new LivroRepository.Callback<List<Livro>>() {

                    @Override
                    public void onSucesso(
                            List<Livro> livros) {

                        callback.onSucesso(
                                livros
                        );
                    }

                    @Override
                    public void onErro(
                            Exception e) {

                        callback.onErro(e);
                    }
                }
        );
    }

    /**
     * Busca categorias cadastradas.
     */
    public void buscarCategorias(
            Callback<List<String>> callback) {

        categoriaRepository.buscarTodos(
                new CategoriaRepository.Callback<List<Categoria>>() {

                    @Override
                    public void onSucesso(
                            List<Categoria> categorias) {

                        Set<String> nomes =
                                new HashSet<>();

                        for (Categoria categoria : categorias) {

                            if (categoria != null
                                    && categoria.getNome() != null) {

                                nomes.add(
                                        categoria.getNome()
                                );
                            }
                        }

                        callback.onSucesso(
                                new ArrayList<>(nomes)
                        );
                    }

                    @Override
                    public void onErro(
                            Exception e) {

                        callback.onErro(e);
                    }
                }
        );
    }

    private void salvarLivroNoBanco(
            Livro livro,
            Callback<Livro> callback) {

        livroRepository.salvar(
                livro,
                new LivroRepository.Callback<Livro>() {

                    @Override
                    public void onSucesso(
                            Livro livroSalvo) {

                        callback.onSucesso(
                                livroSalvo
                        );
                    }

                    @Override
                    public void onErro(
                            Exception e) {

                        callback.onErro(e);
                    }
                }
        );
    }

    private List<String> limparLista(
            List<String> lista) {

        List<String> resultado =
                new ArrayList<>();

        if (lista == null) {
            return resultado;
        }

        Set<String> valores =
                new HashSet<>();

        for (String valor : lista) {

            if (valor == null) {
                continue;
            }

            String valorLimpo =
                    valor.trim();

            if (!valorLimpo.isEmpty()) {
                valores.add(valorLimpo);
            }
        }

        resultado.addAll(valores);

        return resultado;
    }
}