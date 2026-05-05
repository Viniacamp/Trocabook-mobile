package br.gov.sp.cps.trocabook.repository;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;
import java.util.ArrayList;

import br.gov.sp.cps.trocabook.model.Livro;

public class NegociacoesRepository {

    private FirebaseFirestore db;

    public interface Callback<T> {
        void onSuccess(T dados);
        void onError(String erro);
    }

    public NegociacoesRepository() {
        db = FirebaseFirestore.getInstance();
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
}