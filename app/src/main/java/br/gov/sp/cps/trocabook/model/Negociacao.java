package br.gov.sp.cps.trocabook.model;

public class Negociacao {
    private String id;
    private String userId;
    private String livroId;
    private String titulo;
    private String capa;
    private String tipoNegociacao;

    public Negociacao(){}

    public Negociacao(String id, String userId, String livroId, String titulo, String capa, String tipoNegociacao) {
        this.id = id;
        this.userId = userId;
        this.livroId = livroId;
        this.titulo = titulo;
        this.capa = capa;
        this.tipoNegociacao = tipoNegociacao;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getLivroId() {
        return livroId;
    }

    public void setLivroId(String livroId) {
        this.livroId = livroId;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getCapa() {
        return capa;
    }

    public void setCapa(String capa) {
        this.capa = capa;
    }

    public String getTipoNegociacao() {
        return tipoNegociacao;
    }

    public void setTipoNegociacao(String tipoNegociacao) {
        this.tipoNegociacao = tipoNegociacao;
    }
}
