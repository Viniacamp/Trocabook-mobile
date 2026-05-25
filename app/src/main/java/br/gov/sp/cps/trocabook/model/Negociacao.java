package br.gov.sp.cps.trocabook.model;

import java.io.Serializable;

import br.gov.sp.cps.trocabook.model.utils.ItemComCapa;

public class Negociacao implements Serializable, ItemComCapa {
    private String id;
    private String userIdRemetente;

    private String userIdDestinatario;
    private String anuncioId;

    private String capa;

    public Negociacao(){}

    public Negociacao(String anuncioId, String userIdDestinatario, String userIdRemetente, String id, String capa) {
        this.anuncioId = anuncioId;
        this.userIdDestinatario = userIdDestinatario;
        this.userIdRemetente = userIdRemetente;
        this.id = id;
        this.capa = capa;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserIdRemetente() {
        return userIdRemetente;
    }

    public void setUserIdRemetente(String userIdRemetente) {
        this.userIdRemetente = userIdRemetente;
    }

    public String getUserIdDestinatario() {
        return userIdDestinatario;
    }

    public void setUserIdDestinatario(String userIdDestinatario) {
        this.userIdDestinatario = userIdDestinatario;
    }

    public String getAnuncioId() {
        return anuncioId;
    }

    public void setAnuncioId(String anuncioId) {
        this.anuncioId = anuncioId;
    }

    @Override
    public String getCapa(){
        return this.capa;
    }
}
