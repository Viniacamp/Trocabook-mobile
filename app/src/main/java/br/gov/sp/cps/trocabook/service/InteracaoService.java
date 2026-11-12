package br.gov.sp.cps.trocabook.service;

import com.google.firebase.Timestamp;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

import br.gov.sp.cps.trocabook.model.Interacao;
import br.gov.sp.cps.trocabook.repository.InteracaoRepository;

public class InteracaoService {

    private final InteracaoRepository interacaoRepository;

    public InteracaoService() {
        this.interacaoRepository = new InteracaoRepository();
    }

    public interface Callback<T> {
        void onSuccess(T dados);
        void onError(String erro);
    }

    public void registrarPesquisa(String uidUsuario, String termoPesquisa, Callback<Interacao> callback){
        if(uidUsuario == null || uidUsuario.trim().isEmpty()){
            callback.onError("Usuário inválido");
            return;
        }

        if(termoPesquisa == null || termoPesquisa.trim().isEmpty()){
            callback.onError("Termo de pesquisa inválido");
            return;
        }

        String termoNormalizado = termoPesquisa
                .trim()
                .toLowerCase(Locale.ROOT);


        Interacao interacao = new Interacao(
                UUID.randomUUID().toString(),
                uidUsuario,
                Interacao.TipoInteracao.PESQUISA,
                null,
                null,
                termoNormalizado,
                Interacao.OrigemInteracao.MOBILE,
                Timestamp.now()
        );

        salvar(interacao, callback);

    }

    public void registrarVisualizacao(String uidUsuario, String uidLivro, String uidAnuncio, Callback<Interacao> callback){
        if (uidUsuario == null || uidUsuario.trim().isEmpty()){
            callback.onError("Usuário inválido");
        }

        if (uidLivro == null || uidLivro.trim().isEmpty()){
            callback.onError("Livro inválido");
        }

        if (uidAnuncio == null || uidAnuncio.trim().isEmpty()){
            callback.onError("Anúncio inválido");
        }

        Interacao interacao = new Interacao(
                UUID.randomUUID().toString(),
                uidUsuario,
                Interacao.TipoInteracao.VISUALIZACAO,
                uidLivro,
                uidAnuncio,
                null,
                Interacao.OrigemInteracao.MOBILE,
                Timestamp.now()
        );

        salvar(interacao, callback);



    }

    public void registrarInicioConversa(String uidUsuario, String uidLivro, String uidAnuncio, Callback<Interacao> callback){
        if (uidUsuario == null || uidUsuario.trim().isEmpty()){
            callback.onError("Usuário inválido");
        }

        if (uidLivro == null || uidLivro.trim().isEmpty()){
            callback.onError("Livro inválido");
        }

        if (uidAnuncio == null || uidAnuncio.trim().isEmpty()){
            callback.onError("Anúncio inválido");
        }

        final boolean[] inicioConversaJaRegistrado = new boolean[1];
        interacaoRepository.buscarPorUidUsuarioEAnuncioETipo(
                uidUsuario,
                uidAnuncio,
                Interacao.TipoInteracao.INICIO_CONVERSA,
                new InteracaoRepository.Callback<List<Interacao>>() {
                    @Override
                    public void onSucesso(List<Interacao> dados) {
                        inicioConversaJaRegistrado[0] = !dados.isEmpty();
                    }

                    @Override
                    public void onErro(Exception e) {

                    }
                }

        );

        if (inicioConversaJaRegistrado[0]){
            callback.onSuccess(null);
        }

        else {
            Interacao interacao = new Interacao(
                    UUID.randomUUID().toString(),
                    uidUsuario,
                    Interacao.TipoInteracao.INICIO_CONVERSA,
                    uidLivro,
                    uidAnuncio,
                    null,
                    Interacao.OrigemInteracao.MOBILE,
                    Timestamp.now()
            );

            salvar(interacao, callback);
        }



    }




    private void salvar(Interacao interacao, Callback<Interacao> callback) {
        interacaoRepository.salvar(interacao, new InteracaoRepository.Callback<Interacao>() {
            @Override
            public void onSucesso(Interacao dados) {
                callback.onSuccess(dados);
            }

            @Override
            public void onErro(Exception e) {
                callback.onError(e.getMessage());
            }
        });
    }



}
