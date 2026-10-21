package br.gov.sp.cps.trocabook.repository;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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

    public void listarTodosPorStatus(Anuncio.StatusAnuncio statusAnuncio,
                                     Callback<List<Anuncio>> callback) {
        Query query = db.collection(COLECAO);

        if (statusAnuncio != null){
            query = query.whereEqualTo("status", statusAnuncio.name());
        }
        query.get().addOnSuccessListener(snapshot -> {

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
            Anuncio.StatusAnuncio status,
            Callback<List<Anuncio>> callback
    ) {

        Query query = db.collection(COLECAO)
                .whereEqualTo(
                        "tipoNegociacao",
                        tipoNegociacao.name()
                );

        if (status != null) {
            query = query.whereEqualTo(
                    "status",
                    status.name()
            );
        }

        query.get()
                .addOnSuccessListener(snapshot -> {

                    List<Anuncio> lista = new ArrayList<>();

                    snapshot.getDocuments().forEach(documento -> {
                        Anuncio anuncio =
                                documento.toObject(Anuncio.class);

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
            Anuncio.StatusAnuncio status,
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

                    if (status != null
                            && anuncio.getStatus() != status) {

                        callback.onSuccess(null);
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
            Anuncio.StatusAnuncio status,
            Callback<List<Anuncio>> callback
    ) {

        Query query = db.collection(COLECAO)
                .whereEqualTo(
                        "uidUsuario",
                        uidUsuario
                );

        if (status != null) {
            query = query.whereEqualTo(
                    "status",
                    status.name()
            );
        }

        query.get()
                .addOnSuccessListener(snapshot -> {

                    List<Anuncio> lista = new ArrayList<>();

                    snapshot.getDocuments().forEach(documento -> {
                        Anuncio anuncio =
                                documento.toObject(Anuncio.class);

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
            Anuncio.StatusAnuncio status,
            Callback<List<Anuncio>> callback
    ) {

        Query query = db.collection(COLECAO)
                .whereEqualTo(
                        "uidUsuario",
                        uidUsuario
                )
                .whereEqualTo(
                        "tipoNegociacao",
                        tipoNegociacao.name()
                );

        if (status != null) {
            query = query.whereEqualTo(
                    "status",
                    status.name()
            );
        }

        query.get()
                .addOnSuccessListener(snapshot -> {

                    List<Anuncio> lista = new ArrayList<>();

                    snapshot.getDocuments().forEach(documento -> {
                        Anuncio anuncio =
                                documento.toObject(Anuncio.class);

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
            Anuncio.StatusAnuncio status,
            Callback<List<Anuncio>> callback
    ) {

        Query query = db.collection(COLECAO)
                .whereEqualTo(
                        "uidLivro",
                        uidLivro
                );

        if (status != null) {
            query = query.whereEqualTo(
                    "status",
                    status.name()
            );
        }

        query.get()
                .addOnSuccessListener(snapshot -> {

                    List<Anuncio> lista = new ArrayList<>();

                    snapshot.getDocuments().forEach(documento -> {
                        Anuncio anuncio =
                                documento.toObject(Anuncio.class);

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
            Anuncio.StatusAnuncio status,
            Callback<List<Anuncio>> callback
    ) {

        Query query = db.collection(COLECAO);

        if (status != null) {
            query = query.whereEqualTo(
                    "status",
                    status.name()
            );
        }

        String tituloNormalizado = titulo
                .trim()
                .toLowerCase(Locale.ROOT);

        query = query
                .orderBy("tituloBusca")
                .startAt(tituloNormalizado)
                .endAt(tituloNormalizado + "\uf8ff");

        query.get()
                .addOnSuccessListener(snapshot -> {

                    List<Anuncio> lista = new ArrayList<>();

                    snapshot.getDocuments().forEach(documento -> {
                        Anuncio anuncio =
                                documento.toObject(Anuncio.class);

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