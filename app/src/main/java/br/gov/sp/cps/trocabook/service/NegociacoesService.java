package br.gov.sp.cps.trocabook.service;

import java.util.List;

import br.gov.sp.cps.trocabook.model.Livro;
import br.gov.sp.cps.trocabook.repository.NegociacoesRepository;

public class NegociacoesService {

    private NegociacoesRepository repository;

    public interface Callback<T> {
        void onSuccess(T dados);
        void onError(String erro);
    }

    public NegociacoesService() {
        repository = new NegociacoesRepository();
    }

    public void buscarNegociacoesUsuario(String userId, Callback<List<Livro>> callback) {

        if (userId == null) {
            callback.onError("Usuário inválido");
            return;
        }

        repository.buscarNegociacoesPorUsuario(userId, new NegociacoesRepository.Callback<List<Livro>>() {
            @Override
            public void onSuccess(List<Livro> lista) {
                callback.onSuccess(lista);
            }

            @Override
            public void onError(String erro) {
                callback.onError(erro);
            }
        });
    }
}