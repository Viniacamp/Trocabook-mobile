package br.gov.sp.cps.trocabook.repository;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
            Callback<Negociacao> callback
    ) {

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
            Callback<Negociacao> callback
    ) {

        db.collection(COLECAO)
                .document(uid)
                .get()
                .addOnSuccessListener(documento -> {

                    if (!documento.exists()) {
                        callback.onSucesso(null);
                        return;
                    }

                    Negociacao negociacao =
                            converterDocumento(documento);

                    callback.onSucesso(negociacao);
                })
                .addOnFailureListener(callback::onErro);
    }

    public void buscarPorUidUsuarioAnunciante(
            String uidAnunciante,
            Callback<List<Negociacao>> callback
    ) {

        db.collection(COLECAO)
                .whereEqualTo(
                        "usuarioAnuncianteId",
                        uidAnunciante
                )
                .get()
                .addOnSuccessListener(query ->
                        callback.onSucesso(
                                converterDocumentos(
                                        query.getDocuments()
                                )
                        )
                )
                .addOnFailureListener(callback::onErro);
    }

    public void buscarPorUidUsuarioComprador(
            String uidComprador,
            Callback<List<Negociacao>> callback
    ) {

        db.collection(COLECAO)
                .whereEqualTo(
                        "usuarioCompradorId",
                        uidComprador
                )
                .get()
                .addOnSuccessListener(query ->
                        callback.onSucesso(
                                converterDocumentos(
                                        query.getDocuments()
                                )
                        )
                )
                .addOnFailureListener(callback::onErro);
    }

    public void buscarPorUidAnuncianteETipoNegociacao(
            String uidAnunciante,
            Negociacao.TipoNegociacao tipoNegociacao,
            Callback<List<Negociacao>> callback
    ) {

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
                .addOnSuccessListener(query ->
                        callback.onSucesso(
                                converterDocumentos(
                                        query.getDocuments()
                                )
                        )
                )
                .addOnFailureListener(callback::onErro);
    }

    public void buscarPorUidCompradorETipoNegociacao(
            String uidComprador,
            Negociacao.TipoNegociacao tipoNegociacao,
            Callback<List<Negociacao>> callback
    ) {

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
                .addOnSuccessListener(query ->
                        callback.onSucesso(
                                converterDocumentos(
                                        query.getDocuments()
                                )
                        )
                )
                .addOnFailureListener(callback::onErro);
    }

    public void buscarPorAnuncioId(
            String anuncioId,
            Callback<List<Negociacao>> callback
    ) {

        db.collection(COLECAO)
                .whereEqualTo(
                        "anuncioId",
                        anuncioId
                )
                .get()
                .addOnSuccessListener(query ->
                        callback.onSucesso(
                                converterDocumentos(
                                        query.getDocuments()
                                )
                        )
                )
                .addOnFailureListener(callback::onErro);
    }

    public void buscarPorAnuncioEComprador(
            String anuncioId,
            String usuarioCompradorId,
            Callback<Negociacao> callback
    ) {

        db.collection(COLECAO)
                .whereEqualTo(
                        "anuncioId",
                        anuncioId
                )
                .whereEqualTo(
                        "usuarioCompradorId",
                        usuarioCompradorId
                )
                .limit(1)
                .get()
                .addOnSuccessListener(query -> {

                    if (query.isEmpty()) {
                        callback.onSucesso(null);
                        return;
                    }

                    Negociacao negociacao =
                            converterDocumento(
                                    query.getDocuments().get(0)
                            );

                    callback.onSucesso(negociacao);
                })
                .addOnFailureListener(callback::onErro);
    }

    public void buscarPorAnuncioOferecidoId(
            String anuncioOferecidoId,
            Callback<List<Negociacao>> callback
    ) {

        db.collection(COLECAO)
                .whereEqualTo(
                        "anuncioOferecidoId",
                        anuncioOferecidoId
                )
                .get()
                .addOnSuccessListener(query ->
                        callback.onSucesso(
                                converterDocumentos(
                                        query.getDocuments()
                                )
                        )
                )
                .addOnFailureListener(callback::onErro);
    }

    public void contarNegociacoesPorUsuarioETipo(
            String uidUsuario,
            Negociacao.TipoNegociacao tipoNegociacao,
            Callback<Long> callback
    ) {

        db.collection(COLECAO)
                .whereEqualTo(
                        "usuarioAnuncianteId",
                        uidUsuario
                )
                .whereEqualTo(
                        "tipoNegociacao",
                        tipoNegociacao.name()
                )
                .whereEqualTo(
                        "status",
                        Negociacao.StatusNegociacao
                                .FINALIZADA
                                .name()
                )
                .get()
                .addOnSuccessListener(negociacoesAnunciante ->

                        db.collection(COLECAO)
                                .whereEqualTo(
                                        "usuarioCompradorId",
                                        uidUsuario
                                )
                                .whereEqualTo(
                                        "tipoNegociacao",
                                        tipoNegociacao.name()
                                )
                                .whereEqualTo(
                                        "status",
                                        Negociacao.StatusNegociacao
                                                .FINALIZADA
                                                .name()
                                )
                                .get()
                                .addOnSuccessListener(
                                        negociacoesComprador -> {

                                            long total =
                                                    negociacoesAnunciante.size()
                                                            + negociacoesComprador.size();

                                            callback.onSucesso(total);
                                        }
                                )
                                .addOnFailureListener(
                                        callback::onErro
                                )
                )
                .addOnFailureListener(callback::onErro);
    }

    public void buscarFinalizadasPorUsuario(
            String uidUsuario,
            Callback<List<Negociacao>> callback
    ) {

        db.collection(COLECAO)
                .whereEqualTo(
                        "usuarioAnuncianteId",
                        uidUsuario
                )
                .whereEqualTo(
                        "status",
                        Negociacao.StatusNegociacao
                                .FINALIZADA
                                .name()
                )
                .get()
                .addOnSuccessListener(comoAnunciante ->

                        db.collection(COLECAO)
                                .whereEqualTo(
                                        "usuarioCompradorId",
                                        uidUsuario
                                )
                                .whereEqualTo(
                                        "status",
                                        Negociacao.StatusNegociacao
                                                .FINALIZADA
                                                .name()
                                )
                                .get()
                                .addOnSuccessListener(comoComprador -> {

                                    Map<String, Negociacao> negociacoes =
                                            new LinkedHashMap<>();

                                    adicionarAoMapa(
                                            negociacoes,
                                            comoAnunciante.getDocuments()
                                    );

                                    adicionarAoMapa(
                                            negociacoes,
                                            comoComprador.getDocuments()
                                    );

                                    callback.onSucesso(
                                            new ArrayList<>(
                                                    negociacoes.values()
                                            )
                                    );
                                })
                                .addOnFailureListener(
                                        callback::onErro
                                )
                )
                .addOnFailureListener(callback::onErro);
    }

    public void buscarFinalizadasPorUsuarioETipo(
            String uidUsuario,
            Negociacao.TipoNegociacao tipoNegociacao,
            Callback<List<Negociacao>> callback
    ) {

        db.collection(COLECAO)
                .whereEqualTo(
                        "usuarioAnuncianteId",
                        uidUsuario
                )
                .whereEqualTo(
                        "tipoNegociacao",
                        tipoNegociacao.name()
                )
                .whereEqualTo(
                        "status",
                        Negociacao.StatusNegociacao
                                .FINALIZADA
                                .name()
                )
                .get()
                .addOnSuccessListener(comoAnunciante ->

                        db.collection(COLECAO)
                                .whereEqualTo(
                                        "usuarioCompradorId",
                                        uidUsuario
                                )
                                .whereEqualTo(
                                        "tipoNegociacao",
                                        tipoNegociacao.name()
                                )
                                .whereEqualTo(
                                        "status",
                                        Negociacao.StatusNegociacao
                                                .FINALIZADA
                                                .name()
                                )
                                .get()
                                .addOnSuccessListener(comoComprador -> {

                                    Map<String, Negociacao> negociacoes =
                                            new LinkedHashMap<>();

                                    adicionarAoMapa(
                                            negociacoes,
                                            comoAnunciante.getDocuments()
                                    );

                                    adicionarAoMapa(
                                            negociacoes,
                                            comoComprador.getDocuments()
                                    );

                                    callback.onSucesso(
                                            new ArrayList<>(
                                                    negociacoes.values()
                                            )
                                    );
                                })
                                .addOnFailureListener(
                                        callback::onErro
                                )
                )
                .addOnFailureListener(callback::onErro);
    }

    /*
     * Manter temporariamente enquanto existirem telas antigas
     * que ainda dependem desta consulta.
     *
     * Depois da refatoração de MeusLivrosActivity,
     * devemos verificar se ainda existe algum uso.
     */
    public void buscarTodos(
            Callback<List<Negociacao>> callback
    ) {

        db.collection(COLECAO)
                .get()
                .addOnSuccessListener(query ->
                        callback.onSucesso(
                                converterDocumentos(
                                        query.getDocuments()
                                )
                        )
                )
                .addOnFailureListener(callback::onErro);
    }

    private Negociacao converterDocumento(
            DocumentSnapshot documento
    ) {

        Negociacao negociacao =
                documento.toObject(Negociacao.class);

        if (negociacao != null) {
            negociacao.setId(documento.getId());
        }

        return negociacao;
    }

    private List<Negociacao> converterDocumentos(
            List<DocumentSnapshot> documentos
    ) {

        List<Negociacao> lista =
                new ArrayList<>();

        for (DocumentSnapshot documento : documentos) {

            Negociacao negociacao =
                    converterDocumento(documento);

            if (negociacao != null) {
                lista.add(negociacao);
            }
        }

        return lista;
    }

    private void adicionarAoMapa(
            Map<String, Negociacao> negociacoes,
            List<DocumentSnapshot> documentos
    ) {

        for (DocumentSnapshot documento : documentos) {

            Negociacao negociacao =
                    converterDocumento(documento);

            if (negociacao != null) {

                negociacoes.put(
                        negociacao.getId(),
                        negociacao
                );
            }
        }
    }
}