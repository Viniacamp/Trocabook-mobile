package br.gov.sp.cps.trocabook.service;

import java.util.List;
import java.util.UUID;

import br.gov.sp.cps.trocabook.model.Anuncio;
import br.gov.sp.cps.trocabook.model.Livro;
import br.gov.sp.cps.trocabook.model.Usuario;
import br.gov.sp.cps.trocabook.repository.AnuncioRepository;
import br.gov.sp.cps.trocabook.repository.UsuarioRepository;

public class AnuncioService {

    private final AnuncioRepository anuncioRepository;
    private final UsuarioRepository usuarioRepository;

    public AnuncioService() {
        this.anuncioRepository = new AnuncioRepository();
        this.usuarioRepository = new UsuarioRepository();
    }

    public interface Callback<T> {
        void onSuccess(T dados);
        void onError(String mensagem);
    }

    public void criarAnuncio(
            Livro livro,
            String descricao,
            String tipo,
            String uidUsuario,
            Callback<Anuncio> callback
    ) {

        if (livro == null || livro.getId() == null || livro.getId().isEmpty()) {
            callback.onError("Livro inválido");
            return;
        }

        if (tipo == null || tipo.trim().isEmpty()) {
            callback.onError("Tipo de negociação inválido");
            return;
        }

        if (uidUsuario == null || uidUsuario.trim().isEmpty()) {
            callback.onError("Usuário inválido");
            return;
        }

        Anuncio.TipoNegociacao tipoNegociacao;

        try {
            tipoNegociacao = Anuncio.TipoNegociacao.valueOf(
                    tipo.trim().toUpperCase()
            );
        } catch (IllegalArgumentException e) {
            callback.onError("Tipo de negociação inválido");
            return;
        }

        final String descricaoFinal =
                descricao == null || descricao.trim().isEmpty()
                        ? "Anunciante não informou uma descrição"
                        : descricao.trim();

        usuarioRepository.buscarPorUid(
                uidUsuario,
                new UsuarioRepository.Callback<Usuario>() {

                    @Override
                    public void onSucesso(Usuario usuario) {

                        if (usuario == null) {
                            callback.onError("Usuário não encontrado");
                            return;
                        }

                        Anuncio anuncio = new Anuncio(
                                UUID.randomUUID().toString(),
                                uidUsuario,
                                livro.getId(),
                                usuario.getNome(),
                                tipoNegociacao,
                                livro.getTitulo(),
                                usuario.getFotoPerfil(),
                                livro.getCapa(),
                                livro.getIdsAutores(),
                                livro.getIdsCategorias(),
                                descricaoFinal,
                                Anuncio.StatusAnuncio.ATIVO
                        );


                        anuncioRepository.salvar(
                                anuncio,
                                new AnuncioRepository.Callback<Anuncio>() {

                                    @Override
                                    public void onSuccess(Anuncio anuncioSalvo) {
                                        callback.onSuccess(anuncioSalvo);
                                    }

                                    @Override
                                    public void onError(String erro) {
                                        callback.onError(erro);
                                    }
                                }
                        );
                    }

                    @Override
                    public void onErro(Exception e) {
                        callback.onError(
                                e.getMessage() != null
                                        ? e.getMessage()
                                        : "Erro ao buscar usuário"
                        );
                    }
                }
        );
    }

    public void buscarAnuncioPorId(
            String anuncioId,
            Callback<Anuncio> callback
    ) {

        if (anuncioId == null || anuncioId.trim().isEmpty()) {
            callback.onError("Anúncio inválido");
            return;
        }

        anuncioRepository.buscarPorUid(
                anuncioId,
                Anuncio.StatusAnuncio.ATIVO,
                new AnuncioRepository.Callback<Anuncio>() {

                    @Override
                    public void onSuccess(Anuncio anuncio) {
                        callback.onSuccess(anuncio);
                    }

                    @Override
                    public void onError(String erro) {
                        callback.onError(erro);
                    }
                }
        );
    }

    public void buscarTodosOsAnuncios(
            Callback<List<Anuncio>> callback
    ) {

        anuncioRepository.listarTodosPorStatus(
                Anuncio.StatusAnuncio.ATIVO,
                new AnuncioRepository.Callback<List<Anuncio>>() {

                    @Override
                    public void onSuccess(List<Anuncio> anuncios) {
                        callback.onSuccess(anuncios);
                    }

                    @Override
                    public void onError(String erro) {
                        callback.onError(erro);
                    }
                }
        );
    }

    public void buscarAnunciosPorTipo(
            Anuncio.TipoNegociacao tipoNegociacao,
            Callback<List<Anuncio>> callback
    ) {

        if (tipoNegociacao == null) {
            callback.onError("Tipo de negociação inválido");
            return;
        }

        anuncioRepository.listarTodosPorTipoNegociacao(
                tipoNegociacao,
                Anuncio.StatusAnuncio.ATIVO,
                new AnuncioRepository.Callback<List<Anuncio>>() {

                    @Override
                    public void onSuccess(List<Anuncio> anuncios) {
                        callback.onSuccess(anuncios);
                    }

                    @Override
                    public void onError(String erro) {
                        callback.onError(erro);
                    }
                }
        );
    }

    public void buscarAnunciosUsuario(
            String uidUsuario,
            Callback<List<Anuncio>> callback
    ) {

        if (uidUsuario == null || uidUsuario.trim().isEmpty()) {
            callback.onError("Usuário inválido");
            return;
        }

        anuncioRepository.buscarPorUidUsuario(
                uidUsuario,
                Anuncio.StatusAnuncio.ATIVO,
                new AnuncioRepository.Callback<List<Anuncio>>() {

                    @Override
                    public void onSuccess(List<Anuncio> anuncios) {
                        callback.onSuccess(anuncios);
                    }

                    @Override
                    public void onError(String erro) {
                        callback.onError(erro);
                    }
                }
        );
    }

    public void buscarAnunciosUsuarioPorTipo(
            String uidUsuario,
            Anuncio.TipoNegociacao tipoNegociacao,
            Callback<List<Anuncio>> callback
    ) {

        if (uidUsuario == null || uidUsuario.trim().isEmpty()) {
            callback.onError("Usuário inválido");
            return;
        }

        if (tipoNegociacao == null) {
            callback.onError("Tipo de negociação inválido");
            return;
        }

        anuncioRepository.buscarPorUidUsuarioETipoNegociacao(
                uidUsuario,
                tipoNegociacao,
                Anuncio.StatusAnuncio.ATIVO,
                new AnuncioRepository.Callback<List<Anuncio>>() {

                    @Override
                    public void onSuccess(List<Anuncio> anuncios) {
                        callback.onSuccess(anuncios);
                    }

                    @Override
                    public void onError(String erro) {
                        callback.onError(erro);
                    }
                }
        );
    }

    public void buscarAnunciosPorLivro(
            String uidLivro,
            Callback<List<Anuncio>> callback
    ) {

        if (uidLivro == null || uidLivro.trim().isEmpty()) {
            callback.onError("Livro inválido");
            return;
        }

        anuncioRepository.buscarPorUidLivro(
                uidLivro,
                Anuncio.StatusAnuncio.ATIVO,
                new AnuncioRepository.Callback<List<Anuncio>>() {

                    @Override
                    public void onSuccess(List<Anuncio> anuncios) {
                        callback.onSuccess(anuncios);
                    }

                    @Override
                    public void onError(String erro) {
                        callback.onError(erro);
                    }
                }
        );
    }

    public void buscarAnunciosPorTitulo(
            String titulo,
            Callback<List<Anuncio>> callback
    ) {

        if (titulo == null || titulo.trim().isEmpty()) {
            callback.onError("Título inválido");
            return;
        }

        anuncioRepository.buscarPorTitulo(
                titulo.trim(),
                Anuncio.StatusAnuncio.ATIVO,
                new AnuncioRepository.Callback<List<Anuncio>>() {

                    @Override
                    public void onSuccess(List<Anuncio> anuncios) {
                        callback.onSuccess(anuncios);
                    }

                    @Override
                    public void onError(String erro) {
                        callback.onError(erro);
                    }
                }
        );
    }

    public void atualizarAnuncio(
            Anuncio anuncio,
            String uidUsuario,
            Callback<Anuncio> callback
    ) {

        if (anuncio == null
                || anuncio.getId() == null
                || anuncio.getId().trim().isEmpty()) {

            callback.onError("Anúncio inválido");
            return;
        }

        if (uidUsuario == null || uidUsuario.trim().isEmpty()) {
            callback.onError("Usuário inválido");
            return;
        }

        anuncioRepository.buscarPorUid(
                anuncio.getId(),
                Anuncio.StatusAnuncio.ATIVO,
                new AnuncioRepository.Callback<Anuncio>() {

                    @Override
                    public void onSuccess(Anuncio anuncioExistente) {

                        if (anuncioExistente == null) {
                            callback.onError(
                                    "Anúncio não encontrado ou já finalizado"
                            );
                            return;
                        }

                        if (!uidUsuario.equals(
                                anuncioExistente.getUidUsuario()
                        )) {
                            callback.onError(
                                    "Você não pode editar este anúncio"
                            );
                            return;
                        }

                        // Somente os campos editáveis.
                        anuncioExistente.setDescricao(
                                anuncio.getDescricao()
                        );

                        anuncioExistente.setTipoNegociacao(
                                anuncio.getTipoNegociacao()
                        );

                        anuncioRepository.atualizar(
                                anuncioExistente,
                                new AnuncioRepository.Callback<Anuncio>() {

                                    @Override
                                    public void onSuccess(
                                            Anuncio anuncioAtualizado
                                    ) {
                                        callback.onSuccess(
                                                anuncioAtualizado
                                        );
                                    }

                                    @Override
                                    public void onError(String erro) {
                                        callback.onError(erro);
                                    }
                                }
                        );
                    }

                    @Override
                    public void onError(String erro) {
                        callback.onError(erro);
                    }
                }
        );
    }

    public void deletarAnuncio(
            String anuncioId,
            Callback<Void> callback
    ) {

        if (anuncioId == null || anuncioId.trim().isEmpty()) {
            callback.onError("O ID do anúncio não pode ser vazio");
            return;
        }

        anuncioRepository.deletar(
                anuncioId,
                new AnuncioRepository.Callback<Void>() {

                    @Override
                    public void onSuccess(Void resultado) {
                        callback.onSuccess(null);
                    }

                    @Override
                    public void onError(String erro) {
                        callback.onError(erro);
                    }
                }
        );
    }
    public void finalizarAnuncio(
            String anuncioId,
            Callback<Anuncio> callback
    ) {

        if (anuncioId == null || anuncioId.trim().isEmpty()) {
            callback.onError("Anúncio inválido");
            return;
        }

        anuncioRepository.buscarPorUid(
                anuncioId,
                Anuncio.StatusAnuncio.ATIVO,
                new AnuncioRepository.Callback<Anuncio>() {

                    @Override
                    public void onSuccess(Anuncio anuncio) {

                        if (anuncio == null) {
                            callback.onError(
                                    "Anúncio não encontrado ou já finalizado"
                            );
                            return;
                        }

                        anuncio.setStatus(
                                Anuncio.StatusAnuncio.FINALIZADO
                        );

                        anuncioRepository.atualizar(
                                anuncio,
                                new AnuncioRepository.Callback<Anuncio>() {

                                    @Override
                                    public void onSuccess(
                                            Anuncio anuncioAtualizado
                                    ) {
                                        callback.onSuccess(
                                                anuncioAtualizado
                                        );
                                    }

                                    @Override
                                    public void onError(String erro) {
                                        callback.onError(erro);
                                    }
                                }
                        );
                    }

                    @Override
                    public void onError(String erro) {
                        callback.onError(erro);
                    }
                }
        );
    }
}