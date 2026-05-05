package br.gov.sp.cps.trocabook.repository;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Map;

public class UsuarioRepository {

    private FirebaseFirestore db;

    public interface Callback<T> {
        void onSuccess(T data);
        void onError(String erro);
    }

    public UsuarioRepository() {
        db = FirebaseFirestore.getInstance();
    }

    public void salvarUsuario(String userId, Map<String, Object> dados, Callback<Void> callback) {

        db.collection("usuarios")
                .document(userId)
                .set(dados)
                .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public void usuarioExiste(String userId, Callback<Boolean> callback) {

        db.collection("usuarios")
                .document(userId)
                .get()
                .addOnSuccessListener(doc -> {
                    callback.onSuccess(doc.exists());
                })
                .addOnFailureListener(e -> {
                    callback.onError("Erro no Firestore");
                });
    }
}