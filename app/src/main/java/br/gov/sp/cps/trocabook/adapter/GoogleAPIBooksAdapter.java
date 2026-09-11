package br.gov.sp.cps.trocabook.adapter;

import java.util.ArrayList;
import java.util.List;

import br.gov.sp.cps.trocabook.model.dto.GoogleBooksResponse;
import br.gov.sp.cps.trocabook.model.dto.LivroBuscaOutput;

public class GoogleAPIBooksAdapter {

    public List<LivroBuscaOutput> adaptar(GoogleBooksResponse response) {

        List<LivroBuscaOutput> livros = new ArrayList<>();

        if (response == null || response.getItems() == null) {
            return livros;
        }

        for (GoogleBooksResponse.Items item : response.getItems()) {

            if (item == null || item.getVolumeInfo() == null) {
                continue;
            }

            livros.add(adaptarItem(item));
        }

        return livros;
    }

    private LivroBuscaOutput adaptarItem(
            GoogleBooksResponse.Items item
    ) {

        GoogleBooksResponse.VolumeInfo volumeInfo =
                item.getVolumeInfo();

        List<String> autores = volumeInfo.getAuthors() == null
                ? List.of()
                : volumeInfo.getAuthors();

        List<String> categorias = volumeInfo.getCategories() == null
                ? List.of()
                : volumeInfo.getCategories();

        String capa = null;

        if (volumeInfo.getImageLinks() != null) {
            capa = garantirHttps(
                    volumeInfo.getImageLinks().getThumbnail()
            );
        }

        String idioma = volumeInfo.getLanguage() == null
                ? "en"
                : volumeInfo.getLanguage();

        return new LivroBuscaOutput(
                item.getId(),
                volumeInfo.getTitle(),
                autores,
                volumeInfo.getPublisher(),
                volumeInfo.getPublishedDate(),
                capa,
                idioma,
                categorias
        );
    }

    private String garantirHttps(String url) {

        if (url != null && url.startsWith("http:")) {
            return "https:" + url.substring(5);
        }

        return url;
    }
}