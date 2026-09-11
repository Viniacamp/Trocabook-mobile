package br.gov.sp.cps.trocabook.model;

import java.io.Serializable;

public class Usuario implements Serializable {

    private String id;
    private String nome;
    private String cpf;
    private String emailPrincipal;
    private String emailRecuperacao;
    private String fotoPerfil;
    private String genero;
    private String nascimento;
    private String rg;
    private String telefone;
    private double avaliacao;

    public Usuario() {
    }

    public Usuario(
            String id,
            String nome,
            String cpf,
            String emailPrincipal,
            String emailRecuperacao,
            String fotoPerfil,
            String genero,
            String nascimento,
            String rg,
            String telefone,
            double avaliacao
    ) {
        this.id = id;
        this.nome = nome;
        this.cpf = cpf;
        this.emailPrincipal = emailPrincipal;
        this.emailRecuperacao = emailRecuperacao;
        this.fotoPerfil = fotoPerfil;
        this.genero = genero;
        this.nascimento = nascimento;
        this.rg = rg;
        this.telefone = telefone;
        this.avaliacao = avaliacao;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public String getEmailPrincipal() {
        return emailPrincipal;
    }

    public void setEmailPrincipal(String emailPrincipal) {
        this.emailPrincipal = emailPrincipal;
    }

    public String getEmailRecuperacao() {
        return emailRecuperacao;
    }

    public void setEmailRecuperacao(String emailRecuperacao) {
        this.emailRecuperacao = emailRecuperacao;
    }

    public String getFotoPerfil() {
        return fotoPerfil;
    }

    public void setFotoPerfil(String fotoPerfil) {
        this.fotoPerfil = fotoPerfil;
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

    public double getAvaliacao() {
        return avaliacao;
    }

    public void setAvaliacao(double avaliacao) {
        this.avaliacao = avaliacao;
    }
}