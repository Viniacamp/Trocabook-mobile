package br.gov.sp.cps.trocabook.repository;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import br.gov.sp.cps.trocabook.model.Usuario;

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

    public void cpfExiste(String CPF, Callback<Boolean> callback) {
        System.out.println("CPF pesquisado: " + CPF);
        db.collection("usuarios")
                .whereEqualTo("cpf", CPF)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    callback.onSuccess(!queryDocumentSnapshots.isEmpty());
                })
                .addOnFailureListener(e -> {
                    callback.onError("Erro no Firestore");
                });
    }

    public void buscarUsuarios(Callback<List<Usuario>> callback) {

        db.collection("usuarios")
                .get()
                .addOnSuccessListener(snapshot -> {

                    List<Usuario> lista = new ArrayList<>();

                    snapshot.forEach(doc -> {

                        Usuario usuario = doc.toObject(Usuario.class);

                        usuario.setId(doc.getId());

                        lista.add(usuario);
                    });

                    callback.onSuccess(lista);
                })
                .addOnFailureListener(e ->
                        callback.onError("Erro ao buscar usuários"));
    }

    public void buscarUsuarioPorId(String userId, Callback<Usuario> callback) {
        db.collection("usuarios")
                .document(userId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        Usuario usuario = doc.toObject(Usuario.class);
                        usuario.setId(doc.getId());
                        callback.onSuccess(usuario);
                    } else {
                        callback.onError("Usuário não encontrado");
                    }
                })
                .addOnFailureListener(e ->
                        callback.onError("Erro ao buscar usuário"));

    }

    public void isVazio(Callback<Integer> callback){
        db.collection("usuarios")
                .get()
                .addOnSuccessListener(snapshot -> {
                    callback.onSuccess(snapshot.size());
                })
                .addOnFailureListener(e ->
                        callback.onError("Erro ao buscar usuários"));

    }
}