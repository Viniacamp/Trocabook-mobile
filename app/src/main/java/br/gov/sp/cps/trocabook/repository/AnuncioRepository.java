package br.gov.sp.cps.trocabook.repository;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;
import java.util.Map;

import br.gov.sp.cps.trocabook.model.Livro;

public class AnuncioRepository {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public interface Callback<T> {
        void onSuccess(T dados);
        void onError(String erro);
    }

    public void salvarAnuncio(Map<String, Object> anuncio, Callback<Void> callback) {
        db.collection("anuncios")
                .add(anuncio)
                .addOnSuccessListener(doc -> callback.onSuccess(null))
                .addOnFailureListener(e ->
                        callback.onError("Erro ao salvar anúncio"));
    }

    public void buscarAnunciosPorUsuario(String userId, Callback<List<Livro>> callback) {
        db.collection("anuncios")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(snapshot -> {
                    List<Livro> lista = snapshot.toObjects(Livro.class);
                    callback.onSuccess(lista);
                })
                .addOnFailureListener( e->
                        callback.onError("Erro ao buscar anúncios"));
    }
}