package br.gov.sp.cps.trocabook.service.fileAzure;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class AzureRetrofitClient {

    private static final String BASE_URL = "https://trocabook-web-cme0guh4a8btf7fd.eastus2-01.azurewebsites.net/";

    private static Retrofit retrofit;

    public static Retrofit getInstance() {
        if (retrofit == null) {

            OkHttpClient client = new OkHttpClient.Builder()
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(ScalarsConverterFactory.create()) // <-- ADICIONE ESTA LINHA AQUI
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }

        return retrofit;
    }
}