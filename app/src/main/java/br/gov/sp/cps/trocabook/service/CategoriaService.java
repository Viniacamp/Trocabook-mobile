package br.gov.sp.cps.trocabook.service;

import java.util.List;

import br.gov.sp.cps.trocabook.model.Categoria;
import br.gov.sp.cps.trocabook.repository.CategoriaRepository;

public class CategoriaService {

    private final CategoriaRepository categoriaRepository;

    public interface Callback<T> {
        void onSucesso(T dados);
        void onErro(Exception e);
    }

    public CategoriaService() {
        this.categoriaRepository = new CategoriaRepository();
    }

    public void buscarCategoriaPorId(
            String id,
            Callback<Categoria> callback) {

        if (id == null || id.trim().isEmpty()) {
            callback.onErro(
                    new IllegalArgumentException(
                            "O ID da categoria não pode ser vazio"
                    )
            );
            return;
        }

        categoriaRepository.buscarPorUid(
                id,
                new CategoriaRepository.Callback<Categoria>() {

                    @Override
                    public void onSucesso(Categoria categoria) {
                        callback.onSucesso(categoria);
                    }

                    @Override
                    public void onErro(Exception e) {
                        callback.onErro(e);
                    }
                }
        );
    }

    public void buscarCategoriaPorNome(
            String nome,
            Callback<Categoria> callback) {

        if (nome == null || nome.trim().isEmpty()) {
            callback.onErro(
                    new IllegalArgumentException(
                            "O nome da categoria não pode ser vazio"
                    )
            );
            return;
        }

        categoriaRepository.buscarPorNome(
                nome,
                new CategoriaRepository.Callback<Categoria>() {

                    @Override
                    public void onSucesso(Categoria categoria) {
                        callback.onSucesso(categoria);
                    }

                    @Override
                    public void onErro(Exception e) {
                        callback.onErro(e);
                    }
                }
        );
    }

    public void buscarTodas(
            Callback<List<Categoria>> callback) {

        categoriaRepository.buscarTodos(
                new CategoriaRepository.Callback<List<Categoria>>() {

                    @Override
                    public void onSucesso(List<Categoria> categorias) {
                        callback.onSucesso(categorias);
                    }

                    @Override
                    public void onErro(Exception e) {
                        callback.onErro(e);
                    }
                }
        );
    }
}