package br.gov.sp.cps.trocabook.service;

import java.util.List;
import java.util.UUID;

import br.gov.sp.cps.trocabook.model.Negociacao;
import br.gov.sp.cps.trocabook.repository.NegociacaoRepository;

public class NegociacoesService {

    private final NegociacaoRepository negociacaoRepository;

    public interface Callback<T> {
        void onSuccess(T dados);
        void onError(String erro);
    }

    public NegociacoesService() {
        this.negociacaoRepository = new NegociacaoRepository();
    }

    public void salvar(
            Negociacao negociacao,
            Callback<Negociacao> callback
    ) {

        if (negociacao == null) {
            callback.onError("Negociação inválida");
            return;
        }

        if (negociacao.getId() == null || negociacao.getId().isEmpty()) {
            negociacao.setId(UUID.randomUUID().toString());
        }

        negociacaoRepository.salvar(
                negociacao,
                new NegociacaoRepository.Callback<Negociacao>() {

                    @Override
                    public void onSucesso(Negociacao dados) {
                        callback.onSuccess(dados);
                    }

                    @Override
                    public void onErro(Exception erro) {
                        callback.onError(erro.getMessage());
                    }
                }
        );
    }

    public void buscarPorUid(
            String uid,
            Callback<Negociacao> callback
    ) {

        if (uid == null || uid.isEmpty()) {
            callback.onError("ID da negociação inválido");
            return;
        }

        negociacaoRepository.buscarPorUid(
                uid,
                new NegociacaoRepository.Callback<Negociacao>() {

                    @Override
                    public void onSucesso(Negociacao dados) {
                        callback.onSuccess(dados);
                    }

                    @Override
                    public void onErro(Exception erro) {
                        callback.onError(erro.getMessage());
                    }
                }
        );
    }

    public void listarPorUsuarioAnunciante(
            String uidAnunciante,
            Callback<List<Negociacao>> callback
    ) {

        if (uidAnunciante == null || uidAnunciante.isEmpty()) {
            callback.onError("Usuário anunciante inválido");
            return;
        }

        negociacaoRepository.buscarPorUidUsuarioAnunciante(
                uidAnunciante,
                new NegociacaoRepository.Callback<List<Negociacao>>() {

                    @Override
                    public void onSucesso(List<Negociacao> dados) {
                        callback.onSuccess(dados);
                    }

                    @Override
                    public void onErro(Exception erro) {
                        callback.onError(erro.getMessage());
                    }
                }
        );
    }

    public void listarPorUsuarioComprador(
            String uidComprador,
            Callback<List<Negociacao>> callback
    ) {

        if (uidComprador == null || uidComprador.isEmpty()) {
            callback.onError("Usuário comprador inválido");
            return;
        }

        negociacaoRepository.buscarPorUidUsuarioComprador(
                uidComprador,
                new NegociacaoRepository.Callback<List<Negociacao>>() {

                    @Override
                    public void onSucesso(List<Negociacao> dados) {
                        callback.onSuccess(dados);
                    }

                    @Override
                    public void onErro(Exception erro) {
                        callback.onError(erro.getMessage());
                    }
                }
        );
    }

    public void listarPorUsuarioAnuncianteETipo(
            String uidAnunciante,
            Negociacao.TipoNegociacao tipoNegociacao,
            Callback<List<Negociacao>> callback
    ) {

        if (uidAnunciante == null || uidAnunciante.isEmpty()) {
            callback.onError("Usuário anunciante inválido");
            return;
        }

        if (tipoNegociacao == null) {
            callback.onError("Tipo de negociação inválido");
            return;
        }

        negociacaoRepository.buscarPorUidAnuncianteETipoNegociacao(
                uidAnunciante,
                tipoNegociacao,
                new NegociacaoRepository.Callback<List<Negociacao>>() {

                    @Override
                    public void onSucesso(List<Negociacao> dados) {
                        callback.onSuccess(dados);
                    }

                    @Override
                    public void onErro(Exception erro) {
                        callback.onError(erro.getMessage());
                    }
                }
        );
    }

    public void listarPorUsuarioCompradorETipo(
            String uidComprador,
            Negociacao.TipoNegociacao tipoNegociacao,
            Callback<List<Negociacao>> callback
    ) {

        if (uidComprador == null || uidComprador.isEmpty()) {
            callback.onError("Usuário comprador inválido");
            return;
        }

        if (tipoNegociacao == null) {
            callback.onError("Tipo de negociação inválido");
            return;
        }

        negociacaoRepository.buscarPorUidCompradorETipoNegociacao(
                uidComprador,
                tipoNegociacao,
                new NegociacaoRepository.Callback<List<Negociacao>>() {

                    @Override
                    public void onSucesso(List<Negociacao> dados) {
                        callback.onSuccess(dados);
                    }

                    @Override
                    public void onErro(Exception erro) {
                        callback.onError(erro.getMessage());
                    }
                }
        );
    }

    public void contarNegociacoesPorUsuarioETipo(
            String uidAnunciante,
            Negociacao.TipoNegociacao tipoNegociacao,
            Callback<Long> callback
    ) {

        if (uidAnunciante == null || uidAnunciante.isEmpty()) {
            callback.onError("Usuário anunciante inválido");
            return;
        }

        if (tipoNegociacao == null) {
            callback.onError("Tipo de negociação inválido");
            return;
        }

        negociacaoRepository.contarNegociacoesPorUsuarioETipo(
                uidAnunciante,
                tipoNegociacao,
                new NegociacaoRepository.Callback<Long>() {

                    @Override
                    public void onSucesso(Long dados) {
                        callback.onSuccess(dados);
                    }

                    @Override
                    public void onErro(Exception erro) {
                        callback.onError(erro.getMessage());
                    }
                }
        );
    }

    public void buscarTodos(Callback<List<Negociacao>> callback){
        negociacaoRepository.buscarTodos(new NegociacaoRepository.Callback<List<Negociacao>>() {
            @Override
            public void onSucesso(List<Negociacao> dados) {
                callback.onSuccess(dados);
            }

            @Override
            public void onErro(Exception e) {
                callback.onError(e.getMessage());
            }
        });
    }
}