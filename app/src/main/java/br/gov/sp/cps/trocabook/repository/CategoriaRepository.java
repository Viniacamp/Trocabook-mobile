package br.gov.sp.cps.trocabook.repository;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

import br.gov.sp.cps.trocabook.model.Categoria;

public class CategoriaRepository {

    private static final String COLECAO = "categorias";

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public interface Callback<T> {
        void onSucesso(T dados);
        void onErro(Exception e);
    }

    public void salvar(
            Categoria categoria,
            Callback<Categoria> callback) {

        if (categoria.getId() == null || categoria.getId().isEmpty()) {
            callback.onErro(
                    new IllegalArgumentException(
                            "O ID da categoria não pode ser vazio"
                    )
            );
            return;
        }

        db.collection(COLECAO)
                .document(categoria.getId())
                .set(categoria)
                .addOnSuccessListener(unused ->
                        callback.onSucesso(categoria)
                )
                .addOnFailureListener(callback::onErro);
    }

    public void buscarPorUid(
            String uid,
            Callback<Categoria> callback) {

        db.collection(COLECAO)
                .document(uid)
                .get()
                .addOnSuccessListener(documento -> {

                    if (!documento.exists()) {
                        callback.onSucesso(null);
                        return;
                    }

                    Categoria categoria =
                            documento.toObject(Categoria.class);

                    if (categoria != null) {
                        categoria.setId(documento.getId());
                    }

                    callback.onSucesso(categoria);
                })
                .addOnFailureListener(callback::onErro);
    }

    public void buscarPorNome(
            String nome,
            Callback<Categoria> callback) {

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

                    Categoria categoria =
                            documento.toObject(Categoria.class);

                    if (categoria != null) {
                        categoria.setId(documento.getId());
                    }

                    callback.onSucesso(categoria);
                })
                .addOnFailureListener(callback::onErro);
    }

    public void buscarTodos(Callback<List<Categoria>> callback){
        db.collection(COLECAO)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Categoria> categorias = new ArrayList<>();

                    for (DocumentSnapshot documento : queryDocumentSnapshots.getDocuments()){

                        Categoria categoria = documento.toObject(Categoria.class);

                        if (categoria != null){
                            categoria.setId(documento.getId());
                            categorias.add(categoria);
                        }
                    }

                    callback.onSucesso(categorias);
                })
                .addOnFailureListener(callback::onErro);
    }
}