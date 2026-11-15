package br.gov.sp.cps.trocabook.util;

public class ChatState {
    private static String anuncioAtivoId = null;
    private static String usuarioAtivoId = null;

    public static void setChatAtivo(String anuncioId, String usuarioId) {
        anuncioAtivoId = anuncioId;
        usuarioAtivoId = usuarioId;
    }

    public static void limparChatAtivo() {
        anuncioAtivoId = null;
        usuarioAtivoId = null;
    }

    public static boolean estaChatAberto(String anuncioId, String usuarioId) {
        return anuncioAtivoId != null && anuncioAtivoId.equals(anuncioId)
                && usuarioAtivoId != null && usuarioAtivoId.equals(usuarioId);
    }
}