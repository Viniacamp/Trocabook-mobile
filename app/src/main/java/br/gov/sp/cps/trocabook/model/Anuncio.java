package br.gov.sp.cps.trocabook.model;

import java.io.Serializable;

import br.gov.sp.cps.trocabook.model.utils.ItemComCapa;

public class Anuncio implements Serializable, ItemComCapa {
    private String id;
    private String userId;
    private String livroId;
    private String titulo;
    private String capa;
    private String descricao;
    private String tipoNegociacao;

    public Anuncio(){}

    public Anuncio(String id, String userId, String livroId, String titulo, String capa, String descricao, String tipoNegociacao) {
        this.id = id;
        this.userId = userId;
        this.livroId = livroId;
        this.titulo = titulo;
        this.capa = capa;
        this.descricao = descricao;
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

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getTipoNegociacao() {
        return tipoNegociacao;
    }

    public void setTipoNegociacao(String tipoNegociacao) {
        this.tipoNegociacao = tipoNegociacao;
    }
}
