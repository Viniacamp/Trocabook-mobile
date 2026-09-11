package br.gov.sp.cps.trocabook.service.googlebooks;

import br.gov.sp.cps.trocabook.model.dto.GoogleBooksResponse;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface GoogleAPIBooksService {

    @GET("volumes")
    Call<GoogleBooksResponse> buscarTitulo(
            @Query("q") String titulo,
            @Query("key") String apiKey
    );
}