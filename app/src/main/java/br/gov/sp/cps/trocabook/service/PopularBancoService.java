package br.gov.sp.cps.trocabook.service;

import android.content.Context;

import br.gov.sp.cps.trocabook.model.Anuncio;
import br.gov.sp.cps.trocabook.model.Livro;
import br.gov.sp.cps.trocabook.model.Usuario;

import java.util.Collections;

public class PopularBancoService {

    private final UsuarioService usuarioService;
    private final LivroService livroService;
    private final AnuncioService anuncioService;
    private final FileService fileService;

    public interface Callback {
        void onComplete();

        void onError(String erro);
    }

    public PopularBancoService(Context context) {

        Context appContext =
                context.getApplicationContext();

        this.usuarioService =
                new UsuarioService();

        this.livroService =
                new LivroService();

        this.anuncioService =
                new AnuncioService();

        this.fileService =
                new FileService(appContext);
    }

    /**
     * Inicia o povoamento do banco.
     */
    public void popularBanco(Callback callback) {

        criarUsuarioPedro(callback);
    }

    /**
     * Cria o usuário Pedro Lucas.
     */
    private void criarUsuarioPedro(
            Callback callback) {

        criarUsuario(
                "user_pedro",
                "Pedro Lucas",
                "pedro@gmail.com",
                "pedro.rec@gmail.com",
                "(11)99999-1111",
                "52998224725",
                "12.345.678-9",
                "15/10/2000",
                "Masculino",
                "file:///android_asset/perfil/foto_pedro_lucas.png",
                callback,
                () -> criarLivrosPedro(callback)
        );
    }

    /**
     * Cria os livros e anúncios do Pedro.
     */
    private void criarLivrosPedro(
            Callback callback) {

        criarLivroCompleto(
                "Drácula",
                "VENDA",
                "capas/capa_dracula.png",
                "user_pedro",
                callback,
                () -> criarLivroCompleto(
                        "Harry Potter",
                        "TROCA",
                        "capas/capa_harry_potter.png",
                        "user_pedro",
                        callback,
                        () -> criarUsuarioVinicius(callback)
                )
        );
    }

    /**
     * Cria o usuário Vinicius.
     */
    private void criarUsuarioVinicius(
            Callback callback) {

        criarUsuario(
                "user_vinicius",
                "Vinicius",
                "vinicius@gmail.com",
                "vinicius.rec@gmail.com",
                "(11)98888-2222",
                "11144477735",
                "98.765.432-1",
                "10/08/1998",
                "Masculino",
                "file:///android_asset/perfil/foto_vini.png",
                callback,
                () -> criarLivrosVinicius(callback)
        );
    }

    /**
     * Cria os livros e anúncios do Vinicius.
     */
    private void criarLivrosVinicius(
            Callback callback) {

        criarLivroCompleto(
                "Floresta Sombria",
                "VENDA",
                "capas/capa_floresta_sombria.png",
                "user_vinicius",
                callback,
                () -> criarUsuarioRafaela(callback)
        );
    }

    /**
     * Cria a usuária Rafaela.
     */
    private void criarUsuarioRafaela(
            Callback callback) {

        criarUsuario(
                "user_rafaela",
                "Rafaela",
                "rafaela@gmail.com",
                "rafaela.rec@gmail.com",
                "(11)97777-3333",
                "12345678909",
                "55.666.777-8",
                "01/05/1999",
                "Feminino",
                "file:///android_asset/perfil/foto_rafaela.png",
                callback,
                () -> criarLivrosRafaela(callback)
        );
    }

    /**
     * Cria os livros e anúncios da Rafaela.
     */
    private void criarLivrosRafaela(
            Callback callback) {

        criarLivroCompleto(
                "Reflexões",
                "VENDA",
                "capas/capa_reflexoes.png",
                "user_rafaela",
                callback,
                () -> criarLivroCompleto(
                        "O Pequeno Príncipe",
                        "TROCA",
                        "capas/capa_o_pequeno_principe.png",
                        "user_rafaela",
                        callback,
                        () -> criarUsuarioSquirtle(callback)
                )
        );
    }

    /**
     * Cria o usuário Squirtle.
     */
    private void criarUsuarioSquirtle(
            Callback callback) {

        criarUsuario(
                "user_squirtle",
                "Squirtle",
                "squirtle@gmail.com",
                "squirtle.rec@gmail.com",
                "(11)96666-4444",
                "98765432100",
                "11.222.333-4",
                "20/03/1997",
                "Masculino",
                "file:///android_asset/perfil/foto_squirtle_boladao.png",
                callback,
                () -> criarLivrosSquirtle(callback)
        );
    }

    /**
     * Cria os livros e anúncios do Squirtle.
     */
    private void criarLivrosSquirtle(
            Callback callback) {

        criarLivroCompleto(
                "A Era da IA",
                "VENDA",
                "capas/capa_a_era_da_ia.png",
                "user_squirtle",
                callback,
                callback::onComplete
        );
    }

    /**
     * Cria um usuário caso ele ainda não exista.
     */
    private void criarUsuario(
            String userId,
            String nome,
            String emailPrincipal,
            String emailRecuperacao,
            String telefone,
            String cpf,
            String rg,
            String nascimento,
            String genero,
            String fotoPerfil,
            Callback callback,
            Runnable proximo) {

        usuarioService.usuarioExiste(
                userId,
                new UsuarioService.ServiceCallback<Boolean>() {

                    @Override
                    public void onSucesso(
                            Boolean existe) {

                        if (Boolean.TRUE.equals(existe)) {

                            proximo.run();
                            return;
                        }

                        usuarioService.salvarDadosUsuario(
                                userId,
                                nome,
                                emailPrincipal,
                                emailRecuperacao,
                                telefone,
                                cpf,
                                rg,
                                nascimento,
                                genero,
                                fotoPerfil,
                                new UsuarioService.ServiceCallback<Usuario>() {

                                    @Override
                                    public void onSucesso(
                                            Usuario usuario) {

                                        proximo.run();
                                    }

                                    @Override
                                    public void onErro(
                                            String erro) {

                                        callback.onError(
                                                "Erro ao criar usuário "
                                                        + nome
                                                        + ": "
                                                        + erro
                                        );
                                    }
                                }
                        );
                    }

                    @Override
                    public void onErro(
                            String erro) {

                        callback.onError(
                                "Erro ao verificar usuário "
                                        + userId
                                        + ": "
                                        + erro
                        );
                    }
                }
        );
    }

    /**
     * Cria um livro a partir de uma imagem dos assets
     * e, após salvá-lo, cria seu anúncio.
     */
    private void criarLivroCompleto(
            String titulo,
            String tipoNegociacao,
            String caminhoImagem,
            String userId,
            Callback callback,
            Runnable proximo) {

        livroService.salvarLivroAssets(
                titulo,
                Collections.singletonList(
                        "Autor Desconhecido"
                ),
                Collections.singletonList(
                        "Literatura"
                ),
                caminhoImagem,
                fileService,
                new LivroService.Callback<Livro>() {

                    @Override
                    public void onSucesso(
                            Livro livro) {

                        criarAnuncio(
                                livro,
                                tipoNegociacao,
                                userId,
                                callback,
                                proximo
                        );
                    }

                    @Override
                    public void onErro(
                            Exception e) {

                        callback.onError(
                                "Erro ao criar livro "
                                        + titulo
                                        + ": "
                                        + obterMensagemErro(e)
                        );
                    }
                }
        );
    }

    /**
     * Cria o anúncio relacionado ao livro.
     */
    private void criarAnuncio(
            Livro livro,
            String tipoNegociacao,
            String userId,
            Callback callback,
            Runnable proximo) {

        anuncioService.criarAnuncio(
                livro,
                "Livro disponível para negociação",
                tipoNegociacao,
                userId,
                new AnuncioService.Callback<Anuncio>() {

                    @Override
                    public void onSuccess(
                            Anuncio anuncio) {

                        proximo.run();
                    }

                    @Override
                    public void onError(
                            String erro) {

                        callback.onError(
                                "Erro ao criar anúncio de "
                                        + livro.getTitulo()
                                        + ": "
                                        + erro
                        );
                    }
                }
        );
    }

    /**
     * Verifica se o banco já foi populado.
     *
     * Caso o usuário Pedro ainda não exista,
     * inicia o povoamento.
     */
    public void iniciarBanco(
            Callback callback) {

        usuarioService.usuarioExiste(
                "user_pedro",
                new UsuarioService.ServiceCallback<Boolean>() {

                    @Override
                    public void onSucesso(
                            Boolean existe) {

                        if (Boolean.TRUE.equals(existe)) {

                            callback.onComplete();
                            return;
                        }

                        popularBanco(callback);
                    }

                    @Override
                    public void onErro(
                            String erro) {

                        callback.onError(
                                "Erro ao verificar banco: "
                                        + erro
                        );
                    }
                }
        );
    }

    private String obterMensagemErro(
            Exception e) {

        if (e == null
                || e.getMessage() == null
                || e.getMessage().isEmpty()) {

            return "erro desconhecido";
        }

        return e.getMessage();
    }
}