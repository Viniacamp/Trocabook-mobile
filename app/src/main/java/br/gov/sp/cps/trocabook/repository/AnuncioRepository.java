package br.gov.sp.cps.trocabook.repository;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

import br.gov.sp.cps.trocabook.model.Anuncio;

public class AnuncioRepository {

    private static final String COLECAO = "anuncios";

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public interface Callback<T> {
        void onSuccess(T dados);
        void onError(String erro);
    }

    public void salvar(Anuncio anuncio, Callback<Anuncio> callback) {

        if (anuncio.getId() == null || anuncio.getId().isEmpty()) {
            callback.onError("O ID do anúncio não pode ser vazio");
            return;
        }

        db.collection(COLECAO)
                .document(anuncio.getId())
                .set(anuncio)
                .addOnSuccessListener(unused ->
                        callback.onSuccess(anuncio)
                )
                .addOnFailureListener(e ->
                        callback.onError("Erro ao salvar anúncio: " + e.getMessage())
                );
    }

    public void listarTodos(Callback<List<Anuncio>> callback) {

        db.collection(COLECAO)
                .get()
                .addOnSuccessListener(snapshot -> {

                    List<Anuncio> lista = new ArrayList<>();

                    snapshot.getDocuments().forEach(documento -> {
                        Anuncio anuncio = documento.toObject(Anuncio.class);

                        if (anuncio != null) {
                            anuncio.setId(documento.getId());
                            lista.add(anuncio);
                        }
                    });

                    callback.onSuccess(lista);
                })
                .addOnFailureListener(e ->
                        callback.onError("Erro ao buscar todos os anúncios: " + e.getMessage())
                );
    }

    public void listarTodosPorTipoNegociacao(
            Anuncio.TipoNegociacao tipoNegociacao,
            Callback<List<Anuncio>> callback) {

        db.collection(COLECAO)
                .whereEqualTo("tipoNegociacao", tipoNegociacao.name())
                .get()
                .addOnSuccessListener(snapshot -> {

                    List<Anuncio> lista = new ArrayList<>();

                    snapshot.getDocuments().forEach(documento -> {
                        Anuncio anuncio = documento.toObject(Anuncio.class);

                        if (anuncio != null) {
                            anuncio.setId(documento.getId());
                            lista.add(anuncio);
                        }
                    });

                    callback.onSuccess(lista);
                })
                .addOnFailureListener(e ->
                        callback.onError(
                                "Erro ao buscar anúncios por tipo de negociação: "
                                        + e.getMessage()
                        )
                );
    }

    public void buscarPorUid(
            String uid,
            Callback<Anuncio> callback) {

        db.collection(COLECAO)
                .document(uid)
                .get()
                .addOnSuccessListener(documento -> {

                    if (!documento.exists()) {
                        callback.onError("Anúncio não encontrado");
                        return;
                    }

                    Anuncio anuncio = documento.toObject(Anuncio.class);

                    if (anuncio == null) {
                        callback.onError("Não foi possível converter o anúncio");
                        return;
                    }

                    anuncio.setId(documento.getId());
                    callback.onSuccess(anuncio);
                })
                .addOnFailureListener(e ->
                        callback.onError("Erro ao buscar anúncio: " + e.getMessage())
                );
    }

    public void buscarPorUidUsuario(
            String uidUsuario,
            Callback<List<Anuncio>> callback) {

        db.collection(COLECAO)
                .whereEqualTo("uidUsuario", uidUsuario)
                .get()
                .addOnSuccessListener(snapshot -> {

                    List<Anuncio> lista = new ArrayList<>();

                    snapshot.getDocuments().forEach(documento -> {
                        Anuncio anuncio = documento.toObject(Anuncio.class);

                        if (anuncio != null) {
                            anuncio.setId(documento.getId());
                            lista.add(anuncio);
                        }
                    });

                    callback.onSuccess(lista);
                })
                .addOnFailureListener(e ->
                        callback.onError(
                                "Erro ao buscar anúncios por usuário: "
                                        + e.getMessage()
                        )
                );
    }

    public void buscarPorUidUsuarioETipoNegociacao(
            String uidUsuario,
            Anuncio.TipoNegociacao tipoNegociacao,
            Callback<List<Anuncio>> callback) {

        db.collection(COLECAO)
                .whereEqualTo("uidUsuario", uidUsuario)
                .whereEqualTo("tipoNegociacao", tipoNegociacao.name())
                .get()
                .addOnSuccessListener(snapshot -> {

                    List<Anuncio> lista = new ArrayList<>();

                    snapshot.getDocuments().forEach(documento -> {
                        Anuncio anuncio = documento.toObject(Anuncio.class);

                        if (anuncio != null) {
                            anuncio.setId(documento.getId());
                            lista.add(anuncio);
                        }
                    });

                    callback.onSuccess(lista);
                })
                .addOnFailureListener(e ->
                        callback.onError(
                                "Erro ao buscar anúncios por usuário e tipo de negociação: "
                                        + e.getMessage()
                        )
                );
    }

    public void buscarPorUidLivro(
            String uidLivro,
            Callback<List<Anuncio>> callback) {

        db.collection(COLECAO)
                .whereEqualTo("uidLivro", uidLivro)
                .get()
                .addOnSuccessListener(snapshot -> {

                    List<Anuncio> lista = new ArrayList<>();

                    snapshot.getDocuments().forEach(documento -> {
                        Anuncio anuncio = documento.toObject(Anuncio.class);

                        if (anuncio != null) {
                            anuncio.setId(documento.getId());
                            lista.add(anuncio);
                        }
                    });

                    callback.onSuccess(lista);
                })
                .addOnFailureListener(e ->
                        callback.onError(
                                "Erro ao buscar anúncios por livro: "
                                        + e.getMessage()
                        )
                );
    }

    public void buscarPorTitulo(
            String titulo,
            Callback<List<Anuncio>> callback) {

        db.collection(COLECAO)
                .orderBy("titulo")
                .startAt(titulo)
                .endAt(titulo + "\uf8ff")
                .get()
                .addOnSuccessListener(snapshot -> {

                    List<Anuncio> lista = new ArrayList<>();

                    snapshot.getDocuments().forEach(documento -> {
                        Anuncio anuncio = documento.toObject(Anuncio.class);

                        if (anuncio != null) {
                            anuncio.setId(documento.getId());
                            lista.add(anuncio);
                        }
                    });

                    callback.onSuccess(lista);
                })
                .addOnFailureListener(e ->
                        callback.onError(
                                "Erro ao buscar anúncios por título: "
                                        + e.getMessage()
                        )
                );
    }

    public void atualizar(
            Anuncio anuncio,
            Callback<Anuncio> callback) {

        if (anuncio.getId() == null || anuncio.getId().isEmpty()) {
            callback.onError("O ID do anúncio não pode ser vazio");
            return;
        }

        db.collection(COLECAO)
                .document(anuncio.getId())
                .get()
                .addOnSuccessListener(documento -> {

                    if (!documento.exists()) {
                        callback.onError("Anúncio não encontrado");
                        return;
                    }

                    db.collection(COLECAO)
                            .document(anuncio.getId())
                            .set(anuncio)
                            .addOnSuccessListener(unused ->
                                    callback.onSuccess(anuncio)
                            )
                            .addOnFailureListener(e ->
                                    callback.onError(
                                            "Erro ao atualizar anúncio: "
                                                    + e.getMessage()
                                    )
                            );
                })
                .addOnFailureListener(e ->
                        callback.onError(
                                "Erro ao verificar anúncio: "
                                        + e.getMessage()
                        )
                );
    }

    public void deletar(
            String uid,
            Callback<Void> callback) {

        if (uid == null || uid.isEmpty()) {
            callback.onError("O ID do anúncio não pode ser vazio");
            return;
        }

        db.collection(COLECAO)
                .document(uid)
                .delete()
                .addOnSuccessListener(unused ->
                        callback.onSuccess(null)
                )
                .addOnFailureListener(e ->
                        callback.onError(
                                "Erro ao deletar anúncio: "
                                        + e.getMessage()
                        )
                );
    }
}