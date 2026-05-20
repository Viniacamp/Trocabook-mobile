package br.gov.sp.cps.trocabook.model;

public class Negociacao {
    private String id;
    private String userIdRemetente;

    private String userIdDestinatario;
    private String anuncioId;

    public Negociacao(){}

    public Negociacao(String anuncioId, String userIdDestinatario, String userIdRemetente, String id) {
        this.anuncioId = anuncioId;
        this.userIdDestinatario = userIdDestinatario;
        this.userIdRemetente = userIdRemetente;
        this.id = id;
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
}
