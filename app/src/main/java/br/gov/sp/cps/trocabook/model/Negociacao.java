package br.gov.sp.cps.trocabook.model;

import java.io.Serializable;

import br.gov.sp.cps.trocabook.model.utils.ItemComCapa;

public class Negociacao implements Serializable, ItemComCapa {

    private String id;

    private String usuarioAnuncianteId;
    private String usuarioCompradorId;
    private String anuncioId;

    private String dataNegociacao;

    private TipoNegociacao tipoNegociacao;

    private String nmAnunciante;
    private String fotoPerfilAnunciante;

    private String nmComprador;
    private String fotoPerfilComprador;

    private String titulo;
    private String capa;

    public Negociacao() {
    }

    public Negociacao(
            String id,
            String usuarioAnuncianteId,
            String usuarioCompradorId,
            String anuncioId,
            String dataNegociacao,
            TipoNegociacao tipoNegociacao,
            String nmAnunciante,
            String fotoPerfilAnunciante,
            String nmComprador,
            String fotoPerfilComprador,
            String titulo,
            String capa
    ) {
        this.id = id;
        this.usuarioAnuncianteId = usuarioAnuncianteId;
        this.usuarioCompradorId = usuarioCompradorId;
        this.anuncioId = anuncioId;
        this.dataNegociacao = dataNegociacao;
        this.tipoNegociacao = tipoNegociacao;
        this.nmAnunciante = nmAnunciante;
        this.fotoPerfilAnunciante = fotoPerfilAnunciante;
        this.nmComprador = nmComprador;
        this.fotoPerfilComprador = fotoPerfilComprador;
        this.titulo = titulo;
        this.capa = capa;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsuarioAnuncianteId() {
        return usuarioAnuncianteId;
    }

    public void setUsuarioAnuncianteId(String usuarioAnuncianteId) {
        this.usuarioAnuncianteId = usuarioAnuncianteId;
    }

    public String getUsuarioCompradorId() {
        return usuarioCompradorId;
    }

    public void setUsuarioCompradorId(String usuarioCompradorId) {
        this.usuarioCompradorId = usuarioCompradorId;
    }

    public String getAnuncioId() {
        return anuncioId;
    }

    public void setAnuncioId(String anuncioId) {
        this.anuncioId = anuncioId;
    }

    public String getDataNegociacao() {
        return dataNegociacao;
    }

    public void setDataNegociacao(String dataNegociacao) {
        this.dataNegociacao = dataNegociacao;
    }

    public TipoNegociacao getTipoNegociacao() {
        return tipoNegociacao;
    }

    public void setTipoNegociacao(TipoNegociacao tipoNegociacao) {
        this.tipoNegociacao = tipoNegociacao;
    }

    public String getNmAnunciante() {
        return nmAnunciante;
    }

    public void setNmAnunciante(String nmAnunciante) {
        this.nmAnunciante = nmAnunciante;
    }

    public String getFotoPerfilAnunciante() {
        return fotoPerfilAnunciante;
    }

    public void setFotoPerfilAnunciante(String fotoPerfilAnunciante) {
        this.fotoPerfilAnunciante = fotoPerfilAnunciante;
    }

    public String getNmComprador() {
        return nmComprador;
    }

    public void setNmComprador(String nmComprador) {
        this.nmComprador = nmComprador;
    }

    public String getFotoPerfilComprador() {
        return fotoPerfilComprador;
    }

    public void setFotoPerfilComprador(String fotoPerfilComprador) {
        this.fotoPerfilComprador = fotoPerfilComprador;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    @Override
    public String getCapa() {
        return capa;
    }

    public void setCapa(String capa) {
        this.capa = capa;
    }

    public enum TipoNegociacao {
        TROCA,
        VENDA,
        AMBOS
    }
}