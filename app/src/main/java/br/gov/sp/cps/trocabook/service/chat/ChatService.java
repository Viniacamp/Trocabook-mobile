package br.gov.sp.cps.trocabook.service.chat;

import java.util.List;

import br.gov.sp.cps.trocabook.model.dto.ApiResponse;
import br.gov.sp.cps.trocabook.model.dto.AtualizarMensagemDTO;
import br.gov.sp.cps.trocabook.model.dto.MensagemDTO;
import retrofit2.Call;
import retrofit2.Response;

public class ChatService {
    private ChatApi api;

    public interface Callback<T> {
        void onSuccess(T dados);
        void onError(String erro);
    }

    public ChatService(){
        this.api = RetrofitClient.getInstance().create(ChatApi.class);
    }



    public void buscarMensagens(
            String remetente,
            String destinatario,
            String anuncio,
            Callback<List<MensagemDTO>> callback
    ) {

        api.buscarMensagens(
                remetente,
                destinatario,
                anuncio
        ).enqueue(new retrofit2.Callback<ApiResponse<List<MensagemDTO>>>() {

            @Override
            public void onResponse(
                    Call<ApiResponse<List<MensagemDTO>>> call,
                    Response<ApiResponse<List<MensagemDTO>>> response
            ) {

                if (response.isSuccessful()
                        && response.body() != null) {

                    callback.onSuccess(
                            response.body().getData()
                    );

                } else {
                    callback.onError("Erro na API");
                }
            }

            @Override
            public void onFailure(
                    Call<ApiResponse<List<MensagemDTO>>> call,
                    Throwable t
            ) {
                callback.onError(t.getMessage());
            }
        });
    }

    public void salvarMensagem(MensagemDTO mensagemDTO,  Callback<MensagemDTO> callback){
        api.enviarMensagem(mensagemDTO).
                enqueue(new retrofit2.Callback<ApiResponse<MensagemDTO>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<MensagemDTO>> call, Response<ApiResponse<MensagemDTO>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            callback.onSuccess(response.body().getData());
                        } else {
                            callback.onError("Erro na API");
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<MensagemDTO>> call, Throwable t) {
                        callback.onError(t.getMessage());
                    }
                });

    }

    public void atualizarMensagem(
            String id,
            AtualizarMensagemDTO mensagemDTO,
            Callback<MensagemDTO> callback
    ) {

        api.atualizarMensagem(id, mensagemDTO)
                .enqueue(new retrofit2.Callback<ApiResponse<MensagemDTO>>() {

                    @Override
                    public void onResponse(
                            Call<ApiResponse<MensagemDTO>> call,
                            Response<ApiResponse<MensagemDTO>> response
                    ) {

                        if (response.isSuccessful()
                                && response.body() != null) {

                            callback.onSuccess(
                                    response.body().getData()
                            );

                        } else {
                            callback.onError("Erro na API");
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<ApiResponse<MensagemDTO>> call,
                            Throwable t
                    ) {

                        callback.onError(t.getMessage());
                    }
                });
    }

    public void deletarMensagem(String id, Callback<Void> callback){
        api.deletarMensagem(id)
                .enqueue(new retrofit2.Callback<ApiResponse<Void>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                        if (response.isSuccessful()) {
                            callback.onSuccess(null);
                            } else {
                            callback.onError("Erro na API");
                        }
                    }
                    @Override
                    public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                        callback.onError(t.getMessage());
                    }
                });
    }


}
