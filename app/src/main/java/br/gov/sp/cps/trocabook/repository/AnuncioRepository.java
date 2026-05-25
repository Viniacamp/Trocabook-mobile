package br.gov.sp.cps.trocabook.repository;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import br.gov.sp.cps.trocabook.model.Anuncio;
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

    public void buscarAnunciosPorUsuario(
            String userId,
            Callback<List<Anuncio>> callback
    ) {

        db.collection("anuncios")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(snapshot -> {

                    List<Anuncio> lista = new ArrayList<>();

                    snapshot.forEach(doc -> {

                        Anuncio anuncio =
                                doc.toObject(Anuncio.class);

                        anuncio.setId(doc.getId());

                        lista.add(anuncio);
                    });

                    callback.onSuccess(lista);
                })
                .addOnFailureListener(e ->
                        callback.onError("Erro ao buscar anúncios"));
    }

    public void buscarTodosOsAnuncios(
            Callback<List<Anuncio>> callback
    ) {

        db.collection("anuncios")
                .get()
                .addOnSuccessListener(snapshot -> {

                    List<Anuncio> lista = new ArrayList<>();

                    snapshot.forEach(doc -> {

                        Anuncio anuncio =
                                doc.toObject(Anuncio.class);

                        anuncio.setId(doc.getId());

                        lista.add(anuncio);
                    });

                    callback.onSuccess(lista);
                })

                .addOnFailureListener(e -> {
                    callback.onError("Erro ao buscar anúncios");
                });
    }

    public void buscarAnuncioPorId(String anuncioId, Callback<Anuncio> callback) {
        db.collection("anuncios")
                .document(anuncioId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        Anuncio anuncio = doc.toObject(Anuncio.class);
                        anuncio.setId(doc.getId());
                        callback.onSuccess(anuncio);
                    } else {
                        callback.onError("Anúncio não encontrado");
                    }
                })
                .addOnFailureListener(e ->
                        callback.onError("Erro ao buscar anúncio"));

    }
}