package br.gov.sp.cps.trocabook.service.googlebooks;

import br.gov.sp.cps.trocabook.BuildConfig;
import br.gov.sp.cps.trocabook.model.dto.GoogleBooksResponse;
import retrofit2.Call;
import retrofit2.Response;

public class GoogleBooksService {

    private final GoogleAPIBooksService api;

    public GoogleBooksService() {
        api = GoogleBooksRetrofitClient
                .getInstance()
                .create(GoogleAPIBooksService.class);
    }

    public void buscarTitulo(
            String titulo,
            Callback<GoogleBooksResponse> callback
    ) {

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

                    callback.onSucesso(response.body());

                } else {
                    callback.onErro("Erro ao consultar Google Books");
                }
            }

            @Override
            public void onFailure(
                    Call<GoogleBooksResponse> call,
                    Throwable t
            ) {
                callback.onErro(t.getMessage());
            }
        });
    }

    public interface Callback<T> {
        void onSucesso(T dados);
        void onErro(String erro);
    }
}
