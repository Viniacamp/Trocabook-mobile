package br.gov.sp.cps.trocabook.model.dto;

import java.util.List;

public class LivroBuscaOutput {

    private String googleBooksId;
    private String titulo;
    private List<String> autores;
    private String publicadora;
    private String dataPublicacao;
    private String urlImagem;
    private String lingua;
    private List<String> categorias;

    public LivroBuscaOutput() {
    }

    public LivroBuscaOutput(
            String googleBooksId,
            String titulo,
            List<String> autores,
            String publicadora,
            String dataPublicacao,
            String urlImagem,
            String lingua,
            List<String> categorias
    ) {
        this.googleBooksId = googleBooksId;
        this.titulo = titulo;
        this.autores = autores;
        this.publicadora = publicadora;
        this.dataPublicacao = dataPublicacao;
        this.urlImagem = urlImagem;
        this.lingua = lingua;
        this.categorias = categorias;
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

    public List<String> getAutores() {
        return autores;
    }

    public void setAutores(List<String> autores) {
        this.autores = autores;
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

    public String getLingua() {
        return lingua;
    }

    public void setLingua(String lingua) {
        this.lingua = lingua;
    }

    public List<String> getCategorias() {
        return categorias;
    }

    public void setCategorias(List<String> categorias) {
        this.categorias = categorias;
    }
}