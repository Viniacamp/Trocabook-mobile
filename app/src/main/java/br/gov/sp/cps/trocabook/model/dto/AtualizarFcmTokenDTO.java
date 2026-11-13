package br.gov.sp.cps.trocabook.model.dto;

public record AtualizarFcmTokenDTO(
        String uidUsuario,
        String fcmToken
) {}