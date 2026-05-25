package br.gov.sp.cps.trocabook.service;

import java.util.Arrays;
import java.util.Collections;

import br.gov.sp.cps.trocabook.model.Livro;

public class PopularBancoService {

    private UsuarioService usuarioService;

    private LivroService livroService;

    private AnuncioService anuncioService;

    private NegociacoesService negociacoesService;

    public PopularBancoService() {

        usuarioService = new UsuarioService();

        livroService = new LivroService();

        anuncioService = new AnuncioService();

        negociacoesService = new NegociacoesService();
    }

    public void popularBanco() {

        System.out.println("popular");

        criarUsuarioPedro();

        criarUsuarioVinicius();

        criarUsuarioRafaela();

        criarUsuarioSquirtle();
    }

    private void criarUsuarioPedro() {

        System.out.println("Criando usuario pedro");

        String userId = "user_pedro";

        usuarioService.usuarioExiste(
                userId,

                new UsuarioService.ServiceCallback<Boolean>() {

                    @Override
                    public void onSuccess(Boolean existe) {

                        if (existe) return;

                        usuarioService.salvarDadosUsuario(
                                userId,
                                "pedro@gmail.com",
                                "pedro.rec@gmail.com",
                                "(11)99999-1111",
                                "52998224725",
                                "12.345.678-9",
                                "15/10/2000",
                                "Masculino",
                                "file:///android_asset/perfil/foto_pedro_lucas.png",

                                new UsuarioService.ServiceCallback<Void>() {

                                    @Override
                                    public void onSuccess(Void data) {

                                        criarLivroCompleto(
                                                userId,
                                                "Drácula",
                                                "file:///android_asset/capas/capa_dracula.png",
                                                "VENDA"
                                        );

                                        criarLivroCompleto(
                                                userId,
                                                "Harry Potter",
                                                "file:///android_asset/capas/capa_harry_potter.png",
                                                "TROCA"
                                        );
                                    }

                                    @Override
                                    public void onError(String erro) {

                                    }
                                }
                        );
                    }

                    @Override
                    public void onError(String erro) {

                    }
                }
        );
    }

    private void criarUsuarioVinicius() {

        System.out.println("Criando usuario vinicius");

        String userId = "user_vinicius";

        usuarioService.usuarioExiste(
                userId,

                new UsuarioService.ServiceCallback<Boolean>() {

                    @Override
                    public void onSuccess(Boolean existe) {

                        if (existe) return;

                        usuarioService.salvarDadosUsuario(
                                userId,
                                "vinicius@gmail.com",
                                "vinicius.rec@gmail.com",
                                "(11)98888-2222",
                                "11144477735",
                                "98.765.432-1",
                                "10/08/1998",
                                "Masculino",
                                "file:///android_asset/perfil/foto_vini.png",

                                new UsuarioService.ServiceCallback<Void>() {

                                    @Override
                                    public void onSuccess(Void data) {

                                        criarLivroCompleto(
                                                userId,
                                                "Floresta Sombria",
                                                "file:///android_asset/capas/capa_floresta_sombria.png",
                                                "VENDA"
                                        );

                                        criarLivroCompleto(
                                                userId,
                                                "Fundamentos de HTML5 e CSS3",
                                                "file:///android_asset/capas/capa_a_era_da_ia.png",
                                                "TROCA"
                                        );
                                    }

                                    @Override
                                    public void onError(String erro) {

                                    }
                                }
                        );
                    }

                    @Override
                    public void onError(String erro) {

                    }
                }
        );
    }

    private void criarUsuarioRafaela() {

        String userId = "user_rafaela";

        usuarioService.usuarioExiste(
                userId,

                new UsuarioService.ServiceCallback<Boolean>() {

                    @Override
                    public void onSuccess(Boolean existe) {

                        if (existe) return;

                        usuarioService.salvarDadosUsuario(
                                userId,
                                "rafaela@gmail.com",
                                "rafaela.rec@gmail.com",
                                "(11)97777-3333",
                                "12345678909",
                                "55.666.777-8",
                                "01/05/1999",
                                "Feminino",
                                "file:///android_asset/perfil/foto_rafaela.png",

                                new UsuarioService.ServiceCallback<Void>() {

                                    @Override
                                    public void onSuccess(Void data) {

                                        criarLivroCompleto(
                                                userId,
                                                "Reflexões",
                                                "file:///android_asset/capas/capa_reflexoes.png",
                                                "VENDA"
                                        );

                                        criarLivroCompleto(
                                                userId,
                                                "O Pequeno Príncipe",
                                                "file:///android_asset/capas/capa_o_pequeno_principe.png",
                                                "TROCA"
                                        );
                                    }

                                    @Override
                                    public void onError(String erro) {

                                    }
                                }
                        );
                    }

                    @Override
                    public void onError(String erro) {

                    }
                }
        );
    }

    private void criarUsuarioSquirtle() {

        String userId = "user_squirtle";

        usuarioService.usuarioExiste(
                userId,

                new UsuarioService.ServiceCallback<Boolean>() {

                    @Override
                    public void onSuccess(Boolean existe) {

                        if (existe) return;

                        usuarioService.salvarDadosUsuario(
                                userId,
                                "squirtle@gmail.com",
                                "squirtle.rec@gmail.com",
                                "(11)96666-4444",
                                "98765432100",
                                "11.222.333-4",
                                "20/03/1997",
                                "Masculino",
                                "file:///android_asset/perfil/foto_squirtle_boladao.png",

                                new UsuarioService.ServiceCallback<Void>() {

                                    @Override
                                    public void onSuccess(Void data) {

                                        criarLivroCompleto(
                                                userId,
                                                "A Era da IA",
                                                "file:///android_asset/capas/capa_a_era_da_ia.png",
                                                "VENDA"
                                        );

                                        criarLivroCompleto(
                                                userId,
                                                "Coleção de Livros",
                                                "file:///android_asset/capas/capa_reflexoes.png",
                                                "TROCA"
                                        );
                                    }

                                    @Override
                                    public void onError(String erro) {

                                    }
                                }
                        );
                    }

                    @Override
                    public void onError(String erro) {

                    }
                }
        );
    }

    private void criarLivroCompleto(
            String userId,
            String titulo,
            String caminhoImagem,
            String tipoNegociacao
    ) {

        livroService.salvarLivroAssets(
                titulo,
                Arrays.asList("Autor Desconhecido"),
                Collections.singletonList("Literatura"),
                caminhoImagem,

                new LivroService.Callback<Livro>() {

                    @Override
                    public void onSucesso(Livro livro) {

                        anuncioService.criarAnuncio(
                                livro,
                                "Livro disponível para negociação",
                                tipoNegociacao,
                                userId,

                                new AnuncioService.Callback<Void>() {

                                    @Override
                                    public void onSuccess(Void dados) {

                                        negociacoesService.criarNegociacao(
                                                livro,
                                                userId,
                                                tipoNegociacao,

                                                new NegociacoesService.Callback<Void>() {

                                                    @Override
                                                    public void onSuccess(Void dados) {

                                                    }

                                                    @Override
                                                    public void onError(String erro) {

                                                    }
                                                }
                                        );
                                    }

                                    @Override
                                    public void onError(String mensagem) {

                                    }
                                }
                        );
                    }

                    @Override
                    public void onErro(Exception e) {

                        e.printStackTrace();
                    }
                }
        );
    }

    public void iniciarBanco(Runnable onComplete) {

        usuarioService.usuarioExiste(
                "user_pedro",

                new UsuarioService.ServiceCallback<Boolean>() {

                    @Override
                    public void onSuccess(Boolean existe) {

                        if (!existe) {

                            popularBanco();
                        }

                        onComplete.run();
                    }

                    @Override
                    public void onError(String erro) {

                        onComplete.run();
                    }
                }
        );
    }
}