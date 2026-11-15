package br.gov.sp.cps.trocabook.util;

public class ImageUtils {

    private static final String BASE_URL_AZURE = "https://trocabook-web-cme0guh4a8btf7fd.eastus2-01.azurewebsites.net/";

    public static String obterUrlImagem(String caminhoOuUrl) {
        if (caminhoOuUrl == null || caminhoOuUrl.trim().isEmpty()) {
            return null;
        }


        if (caminhoOuUrl.startsWith("http://") ||
                caminhoOuUrl.startsWith("https://") ||
                caminhoOuUrl.startsWith("file://")) {
            return caminhoOuUrl;
        }


        if (caminhoOuUrl.startsWith("/")) {

            if (caminhoOuUrl.startsWith("/uploads/")) {

                return BASE_URL_AZURE + caminhoOuUrl.substring(1);
            }

            return caminhoOuUrl;
        }


        return BASE_URL_AZURE + caminhoOuUrl;
    }
}