package br.gov.sp.cps.trocabook.repository;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

import br.gov.sp.cps.trocabook.model.Livro;

public class LivroRepository {

    private static final String COLECAO = "livros";

    private final FirebaseFirestore db =
            FirebaseFirestore.getInstance();

    public interface Callback<T> {
        void onSucesso(T dados);
        void onErro(Exception e);
    }

    public void salvar(
            Livro livro,
            Callback<Livro> callback) {

        if (livro.getId() == null || livro.getId().isEmpty()) {
            callback.onErro(
                    new IllegalArgumentException(
                            "O ID do livro não pode ser vazio"
                    )
            );
            return;
        }

        db.collection(COLECAO)
                .document(livro.getId())
                .set(livro)
                .addOnSuccessListener(unused ->
                        callback.onSucesso(livro)
                )
                .addOnFailureListener(callback::onErro);
    }

    public void buscarPorUid(
            String uid,
            Callback<Livro> callback) {

        db.collection(COLECAO)
                .document(uid)
                .get()
                .addOnSuccessListener(documento -> {

                    if (!documento.exists()) {
                        callback.onSucesso(null);
                        return;
                    }

                    Livro livro =
                            documento.toObject(Livro.class);

                    if (livro != null) {
                        livro.setId(documento.getId());
                    }

                    callback.onSucesso(livro);
                })
                .addOnFailureListener(callback::onErro);
    }

    public void buscarTodos(
            Callback<List<Livro>> callback) {

        db.collection(COLECAO)
                .get()
                .addOnSuccessListener(query -> {

                    List<Livro> lista = new ArrayList<>();

                    for (DocumentSnapshot documento :
                            query.getDocuments()) {

                        Livro livro =
                                documento.toObject(Livro.class);

                        if (livro != null) {
                            livro.setId(documento.getId());
                            lista.add(livro);
                        }
                    }

                    callback.onSucesso(lista);
                })
                .addOnFailureListener(callback::onErro);
    }

    public void buscarPorTitulo(
            String titulo,
            Callback<List<Livro>> callback) {

        db.collection(COLECAO)
                .whereEqualTo("titulo", titulo)
                .get()
                .addOnSuccessListener(query -> {

                    List<Livro> lista = new ArrayList<>();

                    for (DocumentSnapshot documento :
                            query.getDocuments()) {

                        Livro livro =
                                documento.toObject(Livro.class);

                        if (livro != null) {
                            livro.setId(documento.getId());
                            lista.add(livro);
                        }
                    }

                    callback.onSucesso(lista);
                })
                .addOnFailureListener(callback::onErro);
    }

    public void buscarPorGoogleBooksId(
            String googleBooksId,
            Callback<Livro> callback) {

        db.collection(COLECAO)
                .whereEqualTo(
                        "googleBooksId",
                        googleBooksId
                )
                .limit(1)
                .get()
                .addOnSuccessListener(query -> {

                    if (query.isEmpty()) {
                        callback.onSucesso(null);
                        return;
                    }

                    DocumentSnapshot documento =
                            query.getDocuments().get(0);

                    Livro livro =
                            documento.toObject(Livro.class);

                    if (livro != null) {
                        livro.setId(documento.getId());
                    }

                    callback.onSucesso(livro);
                })
                .addOnFailureListener(callback::onErro);
    }
}