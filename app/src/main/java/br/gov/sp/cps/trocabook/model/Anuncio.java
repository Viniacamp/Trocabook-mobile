package br.gov.sp.cps.trocabook.model;

import java.io.Serializable;
import java.util.List;

import br.gov.sp.cps.trocabook.model.utils.ItemComCapa;

public class Anuncio implements Serializable, ItemComCapa {

    private String id;
    private String uidUsuario;
    private String uidLivro;
    private String nomeUsuario;
    private TipoNegociacao tipoNegociacao;
    private String titulo;

    private String descricao;
    private String fotoPerfil;
    private String capa;
    private List<String> autores;
    private List<String> categorias;

    public Anuncio() {
    }

    public Anuncio(
            String id,
            String uidUsuario,
            String uidLivro,
            String nomeUsuario,
            TipoNegociacao tipoNegociacao,
            String titulo,
            String fotoPerfil,
            String capa,
            List<String> autores,
            List<String> categorias
    ) {
        this.id = id;
        this.uidUsuario = uidUsuario;
        this.uidLivro = uidLivro;
        this.nomeUsuario = nomeUsuario;
        this.tipoNegociacao = tipoNegociacao;
        this.titulo = titulo;
        this.fotoPerfil = fotoPerfil;
        this.capa = capa;
        this.autores = autores;
        this.categorias = categorias;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUidUsuario() {
        return uidUsuario;
    }

    public void setUidUsuario(String uidUsuario) {
        this.uidUsuario = uidUsuario;
    }

    public String getUidLivro() {
        return uidLivro;
    }

    public void setUidLivro(String uidLivro) {
        this.uidLivro = uidLivro;
    }

    public String getNomeUsuario() {
        return nomeUsuario;
    }

    public void setNomeUsuario(String nomeUsuario) {
        this.nomeUsuario = nomeUsuario;
    }

    public TipoNegociacao getTipoNegociacao() {
        return tipoNegociacao;
    }

    public void setTipoNegociacao(TipoNegociacao tipoNegociacao) {
        this.tipoNegociacao = tipoNegociacao;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getFotoPerfil() {
        return fotoPerfil;
    }

    public void setFotoPerfil(String fotoPerfil) {
        this.fotoPerfil = fotoPerfil;
    }

    public String getCapa() {
        return capa;
    }

    public void setCapa(String capa) {
        this.capa = capa;
    }

    public List<String> getAutores() {
        return autores;
    }

    public void setAutores(List<String> autores) {
        this.autores = autores;
    }

    public List<String> getCategorias() {
        return categorias;
    }

    public void setCategorias(List<String> categorias) {
        this.categorias = categorias;
    }

    public enum TipoNegociacao {
        TROCA,
        VENDA,
        AMBOS
    }


}