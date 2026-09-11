package br.gov.sp.cps.trocabook.repository;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

import br.gov.sp.cps.trocabook.model.Negociacao;

public class NegociacaoRepository {

    private static final String COLECAO = "negociacoes";

    private final FirebaseFirestore db =
            FirebaseFirestore.getInstance();

    public interface Callback<T> {
        void onSucesso(T dados);
        void onErro(Exception e);
    }

    public void salvar(
            Negociacao negociacao,
            Callback<Negociacao> callback) {

        if (negociacao.getId() == null
                || negociacao.getId().isEmpty()) {

            callback.onErro(
                    new IllegalArgumentException(
                            "O ID da negociação não pode ser vazio"
                    )
            );
            return;
        }

        db.collection(COLECAO)
                .document(negociacao.getId())
                .set(negociacao)
                .addOnSuccessListener(unused ->
                        callback.onSucesso(negociacao)
                )
                .addOnFailureListener(callback::onErro);
    }

    public void buscarPorUid(
            String uid,
            Callback<Negociacao> callback) {

        db.collection(COLECAO)
                .document(uid)
                .get()
                .addOnSuccessListener(documento -> {

                    if (!documento.exists()) {
                        callback.onSucesso(null);
                        return;
                    }

                    Negociacao negociacao =
                            documento.toObject(Negociacao.class);

                    if (negociacao != null) {
                        negociacao.setId(documento.getId());
                    }

                    callback.onSucesso(negociacao);
                })
                .addOnFailureListener(callback::onErro);
    }

    public void buscarPorUidUsuarioAnunciante(
            String uidAnunciante,
            Callback<List<Negociacao>> callback) {

        db.collection(COLECAO)
                .whereEqualTo(
                        "usuarioAnuncianteId",
                        uidAnunciante
                )
                .get()
                .addOnSuccessListener(query -> {

                    List<Negociacao> lista =
                            converterDocumentos(query.getDocuments());

                    callback.onSucesso(lista);
                })
                .addOnFailureListener(callback::onErro);
    }

    public void buscarPorUidUsuarioComprador(
            String uidComprador,
            Callback<List<Negociacao>> callback) {

        db.collection(COLECAO)
                .whereEqualTo(
                        "usuarioCompradorId",
                        uidComprador
                )
                .get()
                .addOnSuccessListener(query -> {

                    List<Negociacao> lista =
                            converterDocumentos(query.getDocuments());

                    callback.onSucesso(lista);
                })
                .addOnFailureListener(callback::onErro);
    }

    public void buscarPorUidAnuncianteETipoNegociacao(
            String uidAnunciante,
            Negociacao.TipoNegociacao tipoNegociacao,
            Callback<List<Negociacao>> callback) {

        db.collection(COLECAO)
                .whereEqualTo(
                        "usuarioAnuncianteId",
                        uidAnunciante
                )
                .whereEqualTo(
                        "tipoNegociacao",
                        tipoNegociacao.name()
                )
                .get()
                .addOnSuccessListener(query -> {

                    List<Negociacao> lista =
                            converterDocumentos(query.getDocuments());

                    callback.onSucesso(lista);
                })
                .addOnFailureListener(callback::onErro);
    }

    public void buscarPorUidCompradorETipoNegociacao(
            String uidComprador,
            Negociacao.TipoNegociacao tipoNegociacao,
            Callback<List<Negociacao>> callback) {

        db.collection(COLECAO)
                .whereEqualTo(
                        "usuarioCompradorId",
                        uidComprador
                )
                .whereEqualTo(
                        "tipoNegociacao",
                        tipoNegociacao.name()
                )
                .get()
                .addOnSuccessListener(query -> {

                    List<Negociacao> lista =
                            converterDocumentos(query.getDocuments());

                    callback.onSucesso(lista);
                })
                .addOnFailureListener(callback::onErro);
    }

    public void contarNegociacoesPorUsuarioETipo(
            String uidUsuario,
            Negociacao.TipoNegociacao tipoNegociacao,
            Callback<Long> callback) {

        db.collection(COLECAO)
                .whereEqualTo(
                        "usuarioAnuncianteId",
                        uidUsuario
                )
                .whereEqualTo(
                        "tipoNegociacao",
                        tipoNegociacao.name()
                )
                .get()
                .addOnSuccessListener(
                        negociacoesAnunciante -> {

                            db.collection(COLECAO)
                                    .whereEqualTo(
                                            "usuarioCompradorId",
                                            uidUsuario
                                    )
                                    .whereEqualTo(
                                            "tipoNegociacao",
                                            tipoNegociacao.name()
                                    )
                                    .get()
                                    .addOnSuccessListener(
                                            negociacoesComprador -> {

                                                long total =
                                                        negociacoesAnunciante
                                                                .size()
                                                                + negociacoesComprador
                                                                .size();

                                                callback.onSucesso(total);
                                            }
                                    )
                                    .addOnFailureListener(
                                            callback::onErro
                                    );
                        }
                )
                .addOnFailureListener(callback::onErro);
    }

    private List<Negociacao> converterDocumentos(
            List<DocumentSnapshot> documentos) {

        List<Negociacao> lista = new ArrayList<>();

        for (DocumentSnapshot documento : documentos) {

            Negociacao negociacao =
                    documento.toObject(Negociacao.class);

            if (negociacao != null) {
                negociacao.setId(documento.getId());
                lista.add(negociacao);
            }
        }

        return lista;
    }
}