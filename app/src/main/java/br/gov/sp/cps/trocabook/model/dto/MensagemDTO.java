package br.gov.sp.cps.trocabook.model.dto;



public class MensagemDTO {
    private String id;
    private String uidRemetente;
    private String uidDestinatario;
    private String uidAnuncio;
    private String conteudo;
    private String dataEnvio;


    public String getDataEnvio() {
        return dataEnvio;
    }

    public void setDataEnvio(String dataEnvio) {
        this.dataEnvio = dataEnvio;
    }

    public MensagemDTO() {}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUidRemetente() {
        return uidRemetente;
    }

    public void setUidRemetente(String uidRemetente) {
        this.uidRemetente = uidRemetente;
    }

    public String getUidDestinatario() {
        return uidDestinatario;
    }

    public void setUidDestinatario(String uidDestinatario) {
        this.uidDestinatario = uidDestinatario;
    }

    public String getUidAnuncio() {
        return uidAnuncio;
    }

    public void setUidAnuncio(String uidAnuncio) {
        this.uidAnuncio = uidAnuncio;
    }

    public String getConteudo() {
        return conteudo;
    }

    public void setConteudo(String conteudo) {
        this.conteudo = conteudo;
    }
}
