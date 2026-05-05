package br.gov.sp.cps.trocabook.service;

import java.util.List;

import br.gov.sp.cps.trocabook.model.Livro;
import br.gov.sp.cps.trocabook.repository.LivroRepository;

public class LivroService {

    private LivroRepository repository = new LivroRepository();

    public interface Callback {
        void onSucesso(List<Livro> livros);
        void onErro(Exception e);
    }

    public void buscarLivros(String titulo, Callback callback) {

        repository.buscarNoFirebase(titulo, new LivroRepository.Callback() {
            @Override
            public void onSucesso(List<Livro> livros) {

                if (!livros.isEmpty()) {
                    callback.onSucesso(livros);
                } else {
                    buscarNaApi(titulo, callback);
                }
            }

            @Override
            public void onErro(Exception e) {
                callback.onErro(e);
            }
        });
    }

    private void buscarNaApi(String titulo, Callback callback) {

        repository.buscarNaApi(titulo, new LivroRepository.Callback() {
            @Override
            public void onSucesso(List<Livro> livros) {
                callback.onSucesso(livros);
            }

            @Override
            public void onErro(Exception e) {
                callback.onErro(e);
            }
        });
    }
}