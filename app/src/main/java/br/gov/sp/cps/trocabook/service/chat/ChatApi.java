package br.gov.sp.cps.trocabook.service.chat;

import java.util.List;

import br.gov.sp.cps.trocabook.model.dto.ApiResponse;
import br.gov.sp.cps.trocabook.model.dto.AtualizarMensagemDTO;
import br.gov.sp.cps.trocabook.model.dto.MensagemDTO;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ChatApi {
    @POST("mensagens")
    Call<ApiResponse<MensagemDTO>> enviarMensagem(
            @Body MensagemDTO mensagem
    );

    @GET("mensagens")
    Call<ApiResponse<List<MensagemDTO>>> buscarMensagens(
            @Query("remetente") String remetente,
            @Query("destinatario") String destinatario,
            @Query("anuncio") String anuncio
    );

    @GET("mensagens/{id}")
    Call<ApiResponse<MensagemDTO>> buscarMensagemPorId(
            @Path("id") String id
    );

    @PUT("mensagens/{id}")
    Call<ApiResponse<MensagemDTO>> atualizarMensagem(
            @Path("id") String id,
            @Body AtualizarMensagemDTO mensagem
    );

    @DELETE("mensagens/{id}")
    Call<ApiResponse<Void>> deletarMensagem(
            @Path("id") String id
    );
}
