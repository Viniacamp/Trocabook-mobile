package br.gov.sp.cps.trocabook.model.dto;

import br.gov.sp.cps.trocabook.model.Usuario;

public class ConversaDTO {
    private MensagemDTO ultimaMensagem;

    private Usuario usuario;

    private String outroUsuarioId;

    public ConversaDTO(){}

    public ConversaDTO(MensagemDTO ultimaMensagem, Usuario usuario, String outroUsuarioId) {
        this.ultimaMensagem = ultimaMensagem;
        this.usuario = usuario;
        this.outroUsuarioId = outroUsuarioId;
    }

    public MensagemDTO getUltimaMensagem() {
        return ultimaMensagem;
    }

    public void setUltimaMensagem(MensagemDTO ultimaMensagem) {
        this.ultimaMensagem = ultimaMensagem;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public String getOutroUsuarioId() {
        return outroUsuarioId;
    }

    public void setOutroUsuarioId(String outroUsuarioId) {
        this.outroUsuarioId = outroUsuarioId;
    }
}
