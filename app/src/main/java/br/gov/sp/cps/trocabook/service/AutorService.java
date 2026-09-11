package br.gov.sp.cps.trocabook.service;

import java.util.List;

import br.gov.sp.cps.trocabook.model.Autor;
import br.gov.sp.cps.trocabook.repository.AutorRepository;

public class AutorService {

    private final AutorRepository autorRepository;

    public interface Callback<T> {
        void onSucesso(T dados);
        void onErro(Exception e);
    }

    public AutorService() {
        this.autorRepository = new AutorRepository();
    }

    public void buscarAutorPorId(
            String id,
            Callback<Autor> callback) {

        if (id == null || id.trim().isEmpty()) {
            callback.onErro(
                    new IllegalArgumentException(
                            "O ID do autor não pode ser vazio"
                    )
            );
            return;
        }

        autorRepository.buscarPorUid(
                id,
                new AutorRepository.Callback<Autor>() {

                    @Override
                    public void onSucesso(Autor autor) {
                        callback.onSucesso(autor);
                    }

                    @Override
                    public void onErro(Exception e) {
                        callback.onErro(e);
                    }
                }
        );
    }

    public void buscarAutorPorNome(
            String nome,
            Callback<Autor> callback) {

        if (nome == null || nome.trim().isEmpty()) {
            callback.onErro(
                    new IllegalArgumentException(
                            "O nome do autor não pode ser vazio"
                    )
            );
            return;
        }

        autorRepository.buscarPorNome(
                nome,
                new AutorRepository.Callback<Autor>() {

                    @Override
                    public void onSucesso(Autor autor) {
                        callback.onSucesso(autor);
                    }

                    @Override
                    public void onErro(Exception e) {
                        callback.onErro(e);
                    }
                }
        );
    }

    public void buscarTodos(
            Callback<List<Autor>> callback) {

        autorRepository.buscarTodos(
                new AutorRepository.Callback<List<Autor>>() {

                    @Override
                    public void onSucesso(List<Autor> autores) {
                        callback.onSucesso(autores);
                    }

                    @Override
                    public void onErro(Exception e) {
                        callback.onErro(e);
                    }
                }
        );
    }
}