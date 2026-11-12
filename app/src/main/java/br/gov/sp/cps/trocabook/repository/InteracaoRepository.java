package br.gov.sp.cps.trocabook.repository;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

import br.gov.sp.cps.trocabook.model.Interacao;

public class InteracaoRepository {
    private static final String COLECAO = "interacoes";

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public interface Callback<T> {
        void onSucesso(T dados);
        void onErro(Exception e);
    }

    public void salvar(Interacao interacao, Callback<Interacao> callback){
        if (interacao.getId() == null || interacao.getId().isEmpty()){
            callback.onErro(new IllegalArgumentException("O ID da interação não pode ser vazio"));
            return;
        }

        db.collection(COLECAO)
                .document(interacao.getId())
                .set(interacao)
                .addOnSuccessListener(unused -> callback.onSucesso(interacao))
                .addOnFailureListener(callback::onErro);
    }

    public void listarTodos(Callback<List<Interacao>> callback){
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

    public void buscarPorUidUsuario(String uidUsuario, Callback<List<Interacao>> callback){
        db.collection(COLECAO)
                .whereEqualTo("uidUsuario", uidUsuario)
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

    public void buscarPorUidUsuarioETipo(String uidUsuario, Interacao.TipoInteracao tipoInteracao, Callback<List<Interacao>> callback){
        db.collection(COLECAO)
                .whereEqualTo("uidUsuario", uidUsuario)
                .whereEqualTo("tipoInteracao", tipoInteracao.name())
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

    public void buscarPorUidUsuarioEAnuncioETipo(String uidUsuario, String uidAnuncio, Interacao.TipoInteracao tipoInteracao, Callback<List<Interacao>> callback){
        db.collection(COLECAO)
                .whereEqualTo("uidUsuario", uidUsuario)
                .whereEqualTo("uidAnuncio", uidAnuncio)
                .whereEqualTo("tipoInteracao", tipoInteracao.name())
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

    private Interacao converterDocumento(
            DocumentSnapshot documento
    ) {

        Interacao interacao =
                documento.toObject(Interacao.class);

        if (interacao != null) {
            interacao.setId(documento.getId());
        }

        return interacao;
    }

    private List<Interacao> converterDocumentos(
            List<DocumentSnapshot> documentos
    ) {

        List<Interacao> lista =
                new ArrayList<>();

        for (DocumentSnapshot documento : documentos) {

            Interacao interacao =
                    converterDocumento(documento);

            if (interacao != null) {
                lista.add(interacao);
            }
        }

        return lista;
    }
}
