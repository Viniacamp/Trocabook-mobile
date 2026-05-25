package br.gov.sp.cps.trocabook.model;

import java.io.Serializable;

public class Usuario implements Serializable {

    private String id;

    private String cpf;
    private String email_principal;
    private String email_recuperacao;
    private String foto_perfil;
    private String genero;
    private String nascimento;
    private String rg;
    private String telefone;

    public Usuario() {
    }

    public Usuario(String telefone, String rg, String nascimento, String genero, String foto_perfil, String email_recuperacao, String email_principal, String cpf, String id) {
        this.telefone = telefone;
        this.rg = rg;
        this.nascimento = nascimento;
        this.genero = genero;
        this.foto_perfil = foto_perfil;
        this.email_recuperacao = email_recuperacao;
        this.email_principal = email_principal;
        this.cpf = cpf;
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public String getEmail_principal() {
        return email_principal;
    }

    public void setEmail_principal(String email_principal) {
        this.email_principal = email_principal;
    }

    public String getEmail_recuperacao() {
        return email_recuperacao;
    }

    public void setEmail_recuperacao(String email_recuperacao) {
        this.email_recuperacao = email_recuperacao;
    }

    public String getFoto_perfil() {
        return foto_perfil;
    }

    public void setFoto_perfil(String foto_perfil) {
        this.foto_perfil = foto_perfil;
    }

    public String getGenero() {
        return genero;
    }

    public void setGenero(String genero) {
        this.genero = genero;
    }

    public String getNascimento() {
        return nascimento;
    }

    public void setNascimento(String nascimento) {
        this.nascimento = nascimento;
    }

    public String getRg() {
        return rg;
    }

    public void setRg(String rg) {
        this.rg = rg;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }
}