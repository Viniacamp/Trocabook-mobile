package br.gov.sp.cps.trocabook.repository;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

import br.gov.sp.cps.trocabook.model.Livro;
import br.gov.sp.cps.trocabook.model.Negociacao;

public class NegociacoesRepository {

    private FirebaseFirestore db;

    public interface Callback<T> {
        void onSuccess(T dados);

        void onError(String erro);
    }

    public NegociacoesRepository() {
        db = FirebaseFirestore.getInstance();
    }

    public void criarNegociacao(
            String userId,
            String livroId,
            String titulo,
            String capa,
            String tipo,
            Callback<Void> callback
    ) {

        Map<String, Object> negociacao = new HashMap<>();

        negociacao.put("userId", userId);
        negociacao.put("livroId", livroId);
        negociacao.put("titulo", titulo);
        negociacao.put("capa", capa);
        negociacao.put("tipoNegociacao", tipo);

        db.collection("negociacoes")
                .add(negociacao)
                .addOnSuccessListener(doc ->
                        callback.onSuccess(null))
                .addOnFailureListener(e ->
                        callback.onError("Erro ao criar negociação"));
    }

    public void buscarNegociacoesPorUsuario(String userId, Callback<List<Livro>> callback) {

        db.collection("negociacoes")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(snapshot -> {

                    List<Livro> lista = new ArrayList<>();

                    snapshot.forEach(doc -> {
                        lista.add(doc.toObject(Livro.class));
                    });

                    callback.onSuccess(lista);
                })
                .addOnFailureListener(e ->
                        callback.onError("Erro ao buscar negociações")
                );
    }

    public void buscarTodasNegociacoes(Callback<List<Livro>> callback) {
        db.collection("negociacoes")
                .get()
                .addOnSuccessListener(snapshot -> {
                    List<Livro> lista = new ArrayList<>();
                    snapshot.forEach(doc -> {
                        lista.add(doc.toObject(Livro.class));
                    });
                    callback.onSuccess(lista);
                })
                .addOnFailureListener(e -> {
                    callback.onError("Erro ao buscar negociações");
                });

    }

    public void contarNumeroTipoNegociacaoUsuario(String userId, String tipoNegociacao, Callback<Integer> callback){
        db.collection("negociacoes")
                .whereEqualTo("userId", userId)
                .whereEqualTo("tipoNegociacao", tipoNegociacao)
                .get()
                .addOnSuccessListener(snapshot -> {
                    callback.onSuccess(snapshot.size());
                })
                .addOnFailureListener(e -> {
                    callback.onError("Erro ao buscar negociações");
                });
    }
}

