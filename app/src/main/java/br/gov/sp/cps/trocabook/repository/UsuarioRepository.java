package br.gov.sp.cps.trocabook.repository;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

import br.gov.sp.cps.trocabook.model.Usuario;

public class UsuarioRepository {

    private static final String COLECAO = "usuarios";

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public interface Callback<T> {
        void onSucesso(T dados);
        void onErro(Exception e);
    }

    public void cadastrar(Usuario usuario, Callback<Usuario> callback) {

        if (usuario.getId() == null || usuario.getId().isEmpty()) {
            callback.onErro(
                    new IllegalArgumentException("O ID do usuário não pode ser vazio")
            );
            return;
        }

        db.collection(COLECAO)
                .document(usuario.getId())
                .set(usuario)
                .addOnSuccessListener(unused ->
                        callback.onSucesso(usuario)
                )
                .addOnFailureListener(callback::onErro);
    }

    public void buscarPorUid(String uid, Callback<Usuario> callback) {

        db.collection(COLECAO)
                .document(uid)
                .get()
                .addOnSuccessListener(documento -> {

                    if (!documento.exists()) {
                        callback.onSucesso(null);
                        return;
                    }

                    Usuario usuario = documento.toObject(Usuario.class);

                    if (usuario != null) {
                        usuario.setId(documento.getId());
                    }

                    callback.onSucesso(usuario);
                })
                .addOnFailureListener(callback::onErro);
    }

    public void buscarTodos(Callback<List<Usuario>> callback) {

        db.collection(COLECAO)
                .get()
                .addOnSuccessListener(query -> {

                    List<Usuario> lista = new ArrayList<>();

                    for (DocumentSnapshot documento : query.getDocuments()) {

                        Usuario usuario = documento.toObject(Usuario.class);

                        if (usuario != null) {
                            usuario.setId(documento.getId());
                            lista.add(usuario);
                        }
                    }

                    callback.onSucesso(lista);
                })
                .addOnFailureListener(callback::onErro);
    }

    public void buscarPorUids(
            List<String> uids,
            Callback<List<Usuario>> callback
    ) {

        List<Usuario> usuarios = new ArrayList<>();

        if (uids == null || uids.isEmpty()) {
            callback.onSucesso(usuarios);
            return;
        }

        buscarProximoUid(uids, 0, usuarios, callback);
    }

    private void buscarProximoUid(
            List<String> uids,
            int indice,
            List<Usuario> usuarios,
            Callback<List<Usuario>> callback
    ) {

        if (indice >= uids.size()) {
            callback.onSucesso(usuarios);
            return;
        }

        String uid = uids.get(indice);

        buscarPorUid(uid, new Callback<Usuario>() {

            @Override
            public void onSucesso(Usuario usuario) {

                if (usuario != null) {
                    usuarios.add(usuario);
                }

                buscarProximoUid(
                        uids,
                        indice + 1,
                        usuarios,
                        callback
                );
            }

            @Override
            public void onErro(Exception e) {
                callback.onErro(e);
            }
        });
    }

    public void atualizar(Usuario usuario, Callback<Usuario> callback) {

        if (usuario.getId() == null || usuario.getId().isEmpty()) {
            callback.onErro(
                    new IllegalArgumentException("O ID do usuário não pode ser vazio")
            );
            return;
        }

        buscarPorUid(usuario.getId(), new Callback<Usuario>() {

            @Override
            public void onSucesso(Usuario usuarioExistente) {

                if (usuarioExistente == null) {
                    callback.onSucesso(null);
                    return;
                }

                db.collection(COLECAO)
                        .document(usuario.getId())
                        .set(usuario)
                        .addOnSuccessListener(unused ->
                                callback.onSucesso(usuario)
                        )
                        .addOnFailureListener(callback::onErro);
            }

            @Override
            public void onErro(Exception e) {
                callback.onErro(e);
            }
        });
    }

    public void deletar(String uid, Callback<Void> callback) {

        db.collection(COLECAO)
                .document(uid)
                .delete()
                .addOnSuccessListener(unused ->
                        callback.onSucesso(null)
                )
                .addOnFailureListener(callback::onErro);
    }

    public void buscarPorEmail(
            String email,
            Callback<Usuario> callback
    ) {

        db.collection(COLECAO)
                .whereEqualTo("emailPrincipal", email)
                .limit(1)
                .get()
                .addOnSuccessListener(query -> {

                    if (query.isEmpty()) {
                        callback.onSucesso(null);
                        return;
                    }

                    DocumentSnapshot documento =
                            query.getDocuments().get(0);

                    Usuario usuario =
                            documento.toObject(Usuario.class);

                    if (usuario != null) {
                        usuario.setId(documento.getId());
                    }

                    callback.onSucesso(usuario);
                })
                .addOnFailureListener(callback::onErro);
    }

    public void buscarPorCpf(
            String cpf,
            Callback<Usuario> callback
    ) {

        db.collection(COLECAO)
                .whereEqualTo("cpf", cpf)
                .limit(1)
                .get()
                .addOnSuccessListener(query -> {

                    if (query.isEmpty()) {
                        callback.onSucesso(null);
                        return;
                    }

                    DocumentSnapshot documento =
                            query.getDocuments().get(0);

                    Usuario usuario =
                            documento.toObject(Usuario.class);

                    if (usuario != null) {
                        usuario.setId(documento.getId());
                    }

                    callback.onSucesso(usuario);
                })
                .addOnFailureListener(callback::onErro);
    }

    public void buscaTop6Avaliacao(
            Callback<List<Usuario>> callback
    ) {

        db.collection(COLECAO)
                .orderBy(
                        "avaliacao",
                        com.google.firebase.firestore.Query.Direction.DESCENDING
                )
                .limit(6)
                .get()
                .addOnSuccessListener(query -> {

                    List<Usuario> lista = new ArrayList<>();

                    for (DocumentSnapshot documento :
                            query.getDocuments()) {

                        Usuario usuario =
                                documento.toObject(Usuario.class);

                        if (usuario != null) {
                            usuario.setId(documento.getId());
                            lista.add(usuario);
                        }
                    }

                    callback.onSucesso(lista);
                })
                .addOnFailureListener(callback::onErro);
    }
}