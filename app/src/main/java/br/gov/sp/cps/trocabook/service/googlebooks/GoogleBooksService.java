package br.gov.sp.cps.trocabook.service.googlebooks;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.io.IOException;

import br.gov.sp.cps.trocabook.BuildConfig;
import br.gov.sp.cps.trocabook.model.dto.GoogleBooksResponse;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;

public class GoogleBooksService {

    private static final String TAG = "GOOGLE_BOOKS";

    private static final int MAX_TENTATIVAS = 3;

    private final GoogleAPIBooksService api;
    private final Handler handler;

    public GoogleBooksService() {

        api = GoogleBooksRetrofitClient
                .getInstance()
                .create(GoogleAPIBooksService.class);

        handler = new Handler(Looper.getMainLooper());
    }

    public void buscarTitulo(
            String titulo,
            Callback<GoogleBooksResponse> callback
    ) {

        buscarTitulo(
                titulo,
                callback,
                1
        );
    }

    private void buscarTitulo(
            String titulo,
            Callback<GoogleBooksResponse> callback,
            int tentativa
    ) {

        Log.d(
                TAG,
                "Buscando livro | Tentativa: "
                        + tentativa
                        + "/"
                        + MAX_TENTATIVAS
        );

        api.buscarTitulo(
                titulo,
                BuildConfig.GOOGLE_API_KEY
        ).enqueue(new retrofit2.Callback<GoogleBooksResponse>() {

            @Override
            public void onResponse(
                    Call<GoogleBooksResponse> call,
                    Response<GoogleBooksResponse> response
            ) {

                if (response.isSuccessful()
                        && response.body() != null) {

                    Log.d(
                            TAG,
                            "Google Books respondeu com sucesso. HTTP "
                                    + response.code()
                    );

                    callback.onSucesso(response.body());

                    return;
                }

                int codigo = response.code();

                logarErro(response);

                if (deveTentarNovamente(codigo)
                        && tentativa < MAX_TENTATIVAS) {

                    agendarNovaTentativa(
                            titulo,
                            callback,
                            tentativa
                    );

                    return;
                }

                callback.onErro(
                        "Erro ao consultar Google Books. HTTP "
                                + codigo
                );
            }

            @Override
            public void onFailure(
                    Call<GoogleBooksResponse> call,
                    Throwable t
            ) {

                Log.e(
                        TAG,
                        "Falha de comunicação"
                                + " | Tentativa: " + tentativa
                                + " | Exceção: "
                                + t.getClass().getName()
                                + " | Mensagem: "
                                + t.getMessage(),
                        t
                );

                /*
                 * Falhas de comunicação também podem ser transitórias.
                 */
                if (tentativa < MAX_TENTATIVAS) {

                    agendarNovaTentativa(
                            titulo,
                            callback,
                            tentativa
                    );

                    return;
                }

                callback.onErro(
                        "Falha ao acessar Google Books: "
                                + t.getClass().getSimpleName()
                );
            }
        });
    }

    private boolean deveTentarNovamente(int codigoHttp) {

        return codigoHttp == 429
                || codigoHttp == 500
                || codigoHttp == 502
                || codigoHttp == 503
                || codigoHttp == 504;
    }

    private void agendarNovaTentativa(
            String titulo,
            Callback<GoogleBooksResponse> callback,
            int tentativaAtual
    ) {

        long delay = tentativaAtual * 1000L;

        Log.w(
                TAG,
                "Nova tentativa em "
                        + delay
                        + " ms"
        );

        handler.postDelayed(
                () -> buscarTitulo(
                        titulo,
                        callback,
                        tentativaAtual + 1
                ),
                delay
        );
    }

    private void logarErro(
            Response<GoogleBooksResponse> response
    ) {

        String errorBody = null;

        ResponseBody body = response.errorBody();

        if (body != null) {

            try {

                errorBody = body.string();

            } catch (IOException e) {

                Log.e(
                        TAG,
                        "Erro ao ler errorBody",
                        e
                );
            }
        }

        Log.e(
                TAG,
                "Erro Google Books"
                        + " | HTTP: " + response.code()
                        + " | Message: " + response.message()
                        + " | ErrorBody: " + errorBody
        );
    }

    public interface Callback<T> {

        void onSucesso(T dados);

        void onErro(String erro);
    }
}