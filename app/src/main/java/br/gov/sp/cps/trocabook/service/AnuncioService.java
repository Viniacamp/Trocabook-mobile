package br.gov.sp.cps.trocabook.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import br.gov.sp.cps.trocabook.model.Livro;
import br.gov.sp.cps.trocabook.repository.AnuncioRepository;

public class AnuncioService {

    private final AnuncioRepository repository = new AnuncioRepository();

    public interface Callback<T> {
        void onSuccess(T dados);
        void onError(String mensagem);
    }

    public void criarAnuncio(Livro livro, String descricao, String tipo, String userId, Callback callback) {

        if (livro == null) {
            callback.onError("Livro inválido");
            return;
        }

        if (tipo == null || tipo.isEmpty()) {
            callback.onError("Tipo de negociação inválido");
            return;
        }

        if (descricao == null || descricao.trim().isEmpty()) {
            descricao = "Anunciante não informou uma descrição";
        }

        Map<String, Object> anuncio = new HashMap<>();
        anuncio.put("livroId", livro.getId());
        anuncio.put("titulo", livro.getTitulo());
        anuncio.put("capa", livro.getCapa());
        anuncio.put("descricao", descricao);
        anuncio.put("tipoNegociacao", tipo);
        anuncio.put("userId", userId);

        repository.salvarAnuncio(anuncio, new AnuncioRepository.Callback<Void>() {
            @Override
            public void onSuccess(Void resultado) {
                callback.onSuccess(null);
            }

            @Override
            public void onError(String erro) {
                callback.onError(erro);
            }
        });
    }

    public void buscarAnunciosUsuario(String userId, Callback<List<Livro>> callback) {
        if (userId == null) {
            callback.onError("Usuário inválido");
            return;
        }

        repository.buscarAnunciosPorUsuario(userId, new AnuncioRepository.Callback<List<Livro>>() {
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