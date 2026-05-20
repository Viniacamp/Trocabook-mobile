package br.gov.sp.cps.trocabook.service;

import android.content.Context;
import android.net.Uri;

import java.util.Arrays;
import java.util.Collections;

import br.gov.sp.cps.trocabook.R;
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

    public void popularBanco(Context context) {
        System.out.println("popular");
        criarUsuarioPedro(context);
        criarUsuarioVinicius(context);
        criarUsuarioRafaela(context);
        criarUsuarioSquirtle(context);
    }

    private void criarUsuarioPedro(Context context) {
        System.out.println("Criando usuario pedro");
        String userId = "user_pedro";

        usuarioService.usuarioExiste(userId, new UsuarioService.ServiceCallback<Boolean>() {

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
                        "foto_pedro_lucas",
                        new UsuarioService.ServiceCallback<Void>() {

                            @Override
                            public void onSuccess(Void data) {

                                criarLivroCompleto(
                                        context,
                                        userId,
                                        "Drácula",
                                        R.drawable.capa_dracula,
                                        "VENDA"
                                );

                                criarLivroCompleto(
                                        context,
                                        userId,
                                        "Harry Potter",
                                        R.drawable.capa_harry_potter,
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
        });
    }

    private void criarUsuarioVinicius(Context context) {
        System.out.println("Criando usuario vinicius");
        String userId = "user_vinicius";

        usuarioService.usuarioExiste(userId, new UsuarioService.ServiceCallback<Boolean>() {

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
                        "foto_vini",
                        new UsuarioService.ServiceCallback<Void>() {

                            @Override
                            public void onSuccess(Void data) {

                                criarLivroCompleto(
                                        context,
                                        userId,
                                        "Floresta Sombria",
                                        R.drawable.capa_floresta_sombria,
                                        "VENDA"
                                );

                                criarLivroCompleto(
                                        context,
                                        userId,
                                        "Fundamentos de HTML5 e CSS3",
                                        R.drawable.capa_a_era_da_ia,
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
        });
    }

    private void criarUsuarioRafaela(Context context) {

        String userId = "user_rafaela";

        usuarioService.usuarioExiste(userId, new UsuarioService.ServiceCallback<Boolean>() {

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
                        "foto_rafaela",
                        new UsuarioService.ServiceCallback<Void>() {

                            @Override
                            public void onSuccess(Void data) {

                                criarLivroCompleto(
                                        context,
                                        userId,
                                        "Reflexões",
                                        R.drawable.capa_reflexoes,
                                        "VENDA"
                                );

                                criarLivroCompleto(
                                        context,
                                        userId,
                                        "O Pequeno Príncipe",
                                        R.drawable.capa_o_pequeno_principe,
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
        });
    }

    private void criarUsuarioSquirtle(Context context) {

        String userId = "user_squirtle";

        usuarioService.usuarioExiste(userId, new UsuarioService.ServiceCallback<Boolean>() {

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
                        "foto_squirtle_boladao",
                        new UsuarioService.ServiceCallback<Void>() {

                            @Override
                            public void onSuccess(Void data) {

                                criarLivroCompleto(
                                        context,
                                        userId,
                                        "A Era da IA",
                                        R.drawable.capa_a_era_da_ia,
                                        "VENDA"
                                );

                                criarLivroCompleto(
                                        context,
                                        userId,
                                        "Coleção de Livros",
                                        R.drawable.capa_reflexoes,
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
        });
    }

    private void criarLivroCompleto(
            Context context,
            String userId,
            String titulo,
            int drawable,
            String tipoNegociacao
    ) {

        Uri uri = Uri.parse(
                "android.resource://"
                        + context.getPackageName()
                        + "/"
                        + drawable
        );

        livroService.salvarLivro(
                titulo,
                Arrays.asList("Autor Desconhecido"),
                Collections.singletonList("Literatura"),
                uri,
                new FileService(context),
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

                    }
                }
        );
    }

    public void iniciarBanco(Context context){

        usuarioService.usuarioExiste(
                "user_pedro",

                new UsuarioService.ServiceCallback<Boolean>() {

                    @Override
                    public void onSuccess(Boolean existe) {
                        System.out.println("existe: " + existe);

                        if (!existe) {
                            popularBanco(context);
                        }
                    }

                    @Override
                    public void onError(String erro) {

                    }
                });
    }
}