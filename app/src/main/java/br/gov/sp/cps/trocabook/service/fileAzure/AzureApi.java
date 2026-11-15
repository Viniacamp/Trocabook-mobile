package br.gov.sp.cps.trocabook.service.fileAzure;

import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;

public interface AzureApi {

    @Multipart
    @POST("uploads/usuario")
    Call<String> uploadFotoUsuario(
            @Part MultipartBody.Part file
    );

    @Multipart
    @POST("uploads/livro")
    Call<String> uploadLivro(
            @Part MultipartBody.Part file
    );

    @GET("uploads/{tipo}/{filename}")
    Call<ResponseBody> getFile(
            @Path("tipo") String tipo,
            @Path("filename") String filename
    );
}