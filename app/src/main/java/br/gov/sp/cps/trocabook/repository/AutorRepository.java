package br.gov.sp.cps.trocabook.repository;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import br.gov.sp.cps.trocabook.model.Autor;

public class AutorRepository {

    private static final String COLECAO = "autores";

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public interface Callback<T> {
        void onSucesso(T dados);
        void onErro(Exception e);
    }

    public void salvar(
            Autor autor,
            Callback<Autor> callback) {

        if (autor.getId() == null || autor.getId().isEmpty()) {
            callback.onErro(
                    new IllegalArgumentException(
                            "O ID do autor não pode ser vazio"
                    )
            );
            return;
        }

        db.collection(COLECAO)
                .document(autor.getId())
                .set(autor)
                .addOnSuccessListener(unused ->
                        callback.onSucesso(autor)
                )
                .addOnFailureListener(callback::onErro);
    }

    public void buscarPorUid(
            String uid,
            Callback<Autor> callback) {

        db.collection(COLECAO)
                .document(uid)
                .get()
                .addOnSuccessListener(documento -> {

                    if (!documento.exists()) {
                        callback.onSucesso(null);
                        return;
                    }

                    Autor autor =
                            documento.toObject(Autor.class);

                    if (autor != null) {
                        autor.setId(documento.getId());
                    }

                    callback.onSucesso(autor);
                })
                .addOnFailureListener(callback::onErro);
    }

    public void buscarPorNome(
            String nome,
            Callback<Autor> callback) {

        db.collection(COLECAO)
                .whereEqualTo("nome", nome)
                .limit(1)
                .get()
                .addOnSuccessListener(query -> {

                    if (query.isEmpty()) {
                        callback.onSucesso(null);
                        return;
                    }

                    DocumentSnapshot documento =
                            query.getDocuments().get(0);

                    Autor autor =
                            documento.toObject(Autor.class);

                    if (autor != null) {
                        autor.setId(documento.getId());
                    }

                    callback.onSucesso(autor);
                })
                .addOnFailureListener(callback::onErro);
    }
}