package br.gov.sp.cps.trocabook.service;

import android.net.Uri;

import java.util.ArrayList;
import java.util.List;

import br.gov.sp.cps.trocabook.model.Livro;
import br.gov.sp.cps.trocabook.repository.LivroRepository;

public class LivroService {

    private LivroRepository repository = new LivroRepository();

    public interface Callback<T> {
        void onSucesso(T livros);
        void onErro(Exception e);
    }

    public void buscarLivros(String titulo, Callback<List<Livro>> callback) {

        repository.buscarNoFirebase(titulo, new LivroRepository.Callback<List<Livro>>() {
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

    private void buscarNaApi(String titulo, Callback<List<Livro>> callback) {

        repository.buscarNaApi(titulo, new LivroRepository.Callback<List<Livro>>() {
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

    public void buscarCategorias(Callback<List<String>> callback){
        repository.buscarCategoriasNoFirebase(new LivroRepository.Callback<List<List<String>>>() {
            @Override
            public void onSucesso(List<List<String>> categorias) {
                List<String> categoriasUnicas = new ArrayList<>();
                for (List<String> lista : categorias) {
                    for (String categoria : lista) {
                        if (!categoriasUnicas.contains(categoria)){
                            categoriasUnicas.add(categoria);
                        }
                    }
                }
                callback.onSucesso(categoriasUnicas);
            }
            @Override
            public void onErro(Exception e) {
                callback.onErro(e);
            }
        });
    }

    public void salvarLivro(String titulo, List<String> autores, List<String> categorias, Uri imageUri, FileService fileService, Callback<Livro> callback){
        fileService.salvarImagem(imageUri, new FileService.Callback<String>() {
            @Override
            public void onSuccess(String resultado) {
                Livro livro = new Livro();
                livro.setTitulo(titulo);
                livro.setAutores(autores);
                livro.setCategorias(categorias);
                livro.setCapa(resultado);
                repository.salvarLivro(livro, new LivroRepository.Callback<Livro>() {
                    @Override
                    public void onSucesso(Livro livro) {
                        callback.onSucesso(livro);
                    }
                    @Override
                    public void onErro(Exception e) {
                        callback.onErro(e);
                    }
                });



            }

            @Override
            public void onError(String erro) {
            }
        });

    }
}