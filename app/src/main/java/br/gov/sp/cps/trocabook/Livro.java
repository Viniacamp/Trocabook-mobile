package br.gov.sp.cps.trocabook;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Livro implements Serializable {

    private String id;
    private String titulo;
    private List<String> autores = new ArrayList<>();
    private List<String> categorias = new ArrayList<>();
    private String capa;

    public Livro() {}

    public Livro(String id, String titulo, List<String> autores, List<String> categorias, String capa) {
        this.id = id;
        this.titulo = titulo;
        this.autores = autores;
        this.categorias = categorias;
        this.capa = capa;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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
        this.autores = (autores != null) ? autores : new ArrayList<>();
    }

    public List<String> getCategorias() {
        return categorias;
    }

    public void setCategorias(List<String> categorias) {
        this.categorias = (categorias != null) ? categorias : new ArrayList<>();
    }

    public String getCapa() {
        return capa;
    }

    public void setCapa(String capa) {
        this.capa = capa;
    }
}