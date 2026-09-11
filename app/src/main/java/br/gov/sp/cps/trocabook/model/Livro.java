package br.gov.sp.cps.trocabook.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import br.gov.sp.cps.trocabook.model.utils.ItemComCapa;

public class Livro implements Serializable, ItemComCapa {

    private String id;
    private String googleBooksId;
    private String titulo;
    private List<String> idsAutores = new ArrayList<>();
    private List<String> idsCategorias = new ArrayList<>();
    private String publicadora;
    private String dataPublicacao;
    private String urlImagem;

    public Livro() {
    }

    public Livro(
            String id,
            String googleBooksId,
            String titulo,
            List<String> idsAutores,
            List<String> idsCategorias,
            String publicadora,
            String dataPublicacao,
            String urlImagem
    ) {
        this.id = id;
        this.googleBooksId = googleBooksId;
        this.titulo = titulo;
        this.idsAutores = idsAutores != null ? idsAutores : new ArrayList<>();
        this.idsCategorias = idsCategorias != null ? idsCategorias : new ArrayList<>();
        this.publicadora = publicadora;
        this.dataPublicacao = dataPublicacao;
        this.urlImagem = urlImagem;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getGoogleBooksId() {
        return googleBooksId;
    }

    public void setGoogleBooksId(String googleBooksId) {
        this.googleBooksId = googleBooksId;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public List<String> getIdsAutores() {
        return idsAutores;
    }

    public void setIdsAutores(List<String> idsAutores) {
        this.idsAutores = idsAutores != null
                ? idsAutores
                : new ArrayList<>();
    }

    public List<String> getIdsCategorias() {
        return idsCategorias;
    }

    public void setIdsCategorias(List<String> idsCategorias) {
        this.idsCategorias = idsCategorias != null
                ? idsCategorias
                : new ArrayList<>();
    }

    public String getPublicadora() {
        return publicadora;
    }

    public void setPublicadora(String publicadora) {
        this.publicadora = publicadora;
    }

    public String getDataPublicacao() {
        return dataPublicacao;
    }

    public void setDataPublicacao(String dataPublicacao) {
        this.dataPublicacao = dataPublicacao;
    }

    public String getUrlImagem() {
        return urlImagem;
    }

    public void setUrlImagem(String urlImagem) {
        this.urlImagem = urlImagem;
    }

    @Override
    public String getCapa() {
        return urlImagem;
    }
}