package br.gov.sp.cps.trocabook.model;

public class Usuario {

    private String id;
    private String nome;
    private String foto_perfil;

    public Usuario() {}

    public Usuario(String id, String nome, String foto_perfil) {
        this.id = id;
        this.nome = nome;
        this.foto_perfil = foto_perfil;
    }

    public String getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getFoto_perfil() {
        return foto_perfil;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public void setFoto_perfil(String foto_perfil) {
        this.foto_perfil = foto_perfil;
    }
}