package br.gov.sp.cps.trocabook.service;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class IaServiceTest {

    @Test
    public void normalizarTextoRemoveAcentosEConverteParaMinusculas() {
        IaService service = new IaService("");

        assertEquals("ficcao e aventura", service.normalizarTexto("Ficção e Aventura"));
    }

    @Test
    public void removerStopWordsMantemSomenteTermosRelevantes() {
        IaService service = new IaService("");

        assertEquals(
                List.of("suspense", "misterio"),
                service.removerStopWords("Quero um livro de suspense e mistério")
        );
    }

    @Test
    public void usaFallbackLocalQuandoApiKeyEstaVazia() {
        IaService service = new IaService("");

        String descricao = service.gerarDescricaoAnuncio(
                "A Casa das Sete Mulheres",
                "Letícia Wierzchowski",
                "Fiction",
                "TROCA",
                "Capa em bom estado"
        );

        assertTrue(descricao.contains("\"A Casa das Sete Mulheres\""));
        assertTrue(descricao.contains("Ficção"));
        assertTrue(descricao.contains("Disponível para troca"));
        assertFalse(descricao.contains("**"));
        assertTrue(descricao.split("\\R\\s*\\R").length <= 2);
    }

    @Test
    public void fallbackLimitaDescricaoAte380Caracteres() {
        IaService service = new IaService("");
        StringBuilder observacoes = new StringBuilder();
        for (int i = 0; i < 80; i++) {
            observacoes.append("detalhe informado ");
        }

        String descricao = service.gerarDescricaoAnuncio(
                "Livro", null, null, "VENDA", observacoes.toString()
        );

        assertTrue(descricao.length() <= 380);
        assertTrue(descricao.split("\\R\\s*\\R").length <= 2);
    }

    @Test
    public void retornaConsultaOriginalComoFallbackDeTitulo() {
        IaService service = new IaService("");

        assertEquals(
                List.of("Dom Casmurro"),
                service.identificarTitulosParaAnuncio("  Dom Casmurro ")
        );
    }
}
