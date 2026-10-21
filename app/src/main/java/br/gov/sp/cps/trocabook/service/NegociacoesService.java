package br.gov.sp.cps.trocabook.service;

import com.google.firebase.Timestamp;

import java.util.List;
import java.util.UUID;

import br.gov.sp.cps.trocabook.model.Anuncio;
import br.gov.sp.cps.trocabook.model.Negociacao;
import br.gov.sp.cps.trocabook.model.Usuario;
import br.gov.sp.cps.trocabook.repository.NegociacaoRepository;

public class NegociacoesService {

    private final NegociacaoRepository negociacaoRepository;
    private final AnuncioService anuncioService;
    private final UsuarioService usuarioService;

    public interface Callback<T> {
        void onSuccess(T dados);
        void onError(String erro);
    }

    public NegociacoesService() {
        this.negociacaoRepository =
                new NegociacaoRepository();

        this.anuncioService =
                new AnuncioService();

        this.usuarioService =
                new UsuarioService();
    }

    // =========================================================
    // CRIAÇÃO DA PROPOSTA
    // =========================================================

    public void criar(
            String anuncioId,
            String uidInteressado,
            Negociacao.TipoNegociacao tipoNegociacao,
            String anuncioOferecidoId,
            String descricaoOferta,
            Callback<Negociacao> callback
    ) {

        if (anuncioId == null
                || anuncioId.trim().isEmpty()) {

            callback.onError("Anúncio inválido");
            return;
        }

        if (uidInteressado == null
                || uidInteressado.trim().isEmpty()) {

            callback.onError("Usuário interessado inválido");
            return;
        }

        if (tipoNegociacao == null) {
            callback.onError(
                    "Tipo de negociação inválido"
            );
            return;
        }

        negociacaoRepository.buscarPorAnuncioEComprador(
                anuncioId,
                uidInteressado,
                new NegociacaoRepository.Callback<Negociacao>() {

                    @Override
                    public void onSucesso(
                            Negociacao negociacaoExistente
                    ) {

                        if (negociacaoExistente != null
                                && negociacaoExistente.getStatus()
                                != Negociacao.StatusNegociacao.RECUSADA
                                && negociacaoExistente.getStatus()
                                != Negociacao.StatusNegociacao.CANCELADA) {

                            callback.onError(
                                    "Já existe uma negociação para este anúncio"
                            );
                            return;
                        }

                        buscarAnuncioParaCriacao(
                                anuncioId,
                                uidInteressado,
                                tipoNegociacao,
                                anuncioOferecidoId,
                                descricaoOferta,
                                negociacaoExistente,
                                callback
                        );
                    }

                    @Override
                    public void onErro(Exception erro) {
                        callback.onError(
                                mensagemErro(erro)
                        );
                    }
                }
        );
    }

    private void buscarAnuncioParaCriacao(
            String anuncioId,
            String uidInteressado,
            Negociacao.TipoNegociacao tipoNegociacao,
            String anuncioOferecidoId,
            String descricaoOferta,
            Negociacao negociacaoExistente,
            Callback<Negociacao> callback
    ) {

        /*
         * buscarAnuncioPorId já possui semântica pública:
         * somente anúncios ATIVO.
         */
        anuncioService.buscarAnuncioPorId(
                anuncioId,
                new AnuncioService.Callback<Anuncio>() {

                    @Override
                    public void onSuccess(Anuncio anuncio) {

                        if (anuncio == null) {
                            callback.onError(
                                    "Anúncio não encontrado ou já finalizado"
                            );
                            return;
                        }

                        if (uidInteressado.equals(
                                anuncio.getUidUsuario()
                        )) {

                            callback.onError(
                                    "O anunciante não pode negociar o próprio anúncio"
                            );
                            return;
                        }

                        if (!validarTipoNegociacao(
                                anuncio,
                                tipoNegociacao,
                                callback
                        )) {
                            return;
                        }

                        validarEPrepararOferta(
                                anuncio,
                                uidInteressado,
                                tipoNegociacao,
                                anuncioOferecidoId,
                                descricaoOferta,
                                new Callback<DadosOferta>() {

                                    @Override
                                    public void onSuccess(
                                            DadosOferta dadosOferta
                                    ) {

                                        prepararNegociacao(
                                                anuncio,
                                                uidInteressado,
                                                tipoNegociacao,
                                                dadosOferta,
                                                negociacaoExistente,
                                                callback
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
                    public void onError(String mensagem) {
                        callback.onError(mensagem);
                    }
                }
        );
    }

    private void prepararNegociacao(
            Anuncio anuncio,
            String uidInteressado,
            Negociacao.TipoNegociacao tipoNegociacao,
            DadosOferta dadosOferta,
            Negociacao negociacaoExistente,
            Callback<Negociacao> callback
    ) {

        /*
         * RECUSADA/CANCELADA:
         * reutiliza o mesmo documento.
         */
        if (negociacaoExistente != null) {

            negociacaoExistente.setStatus(
                    Negociacao.StatusNegociacao.PENDENTE
            );

            negociacaoExistente.setTipoNegociacao(
                    tipoNegociacao
            );

            negociacaoExistente.setDataNegociacao(
                    Timestamp.now()
            );

            negociacaoExistente.setConfirmacaoAnunciante(
                    false
            );

            negociacaoExistente.setConfirmacaoComprador(
                    false
            );

            atualizarDadosOferta(
                    negociacaoExistente,
                    dadosOferta
            );

            salvar(
                    negociacaoExistente,
                    callback
            );

            return;
        }

        /*
         * Nova negociação:
         * precisamos dos dados do interessado para
         * armazenar o snapshot.
         */
        usuarioService.buscarUsuarioPorId(
                uidInteressado,
                new UsuarioService.ServiceCallback<Usuario>() {

                    @Override
                    public void onSucesso(Usuario usuario) {

                        if (usuario == null) {
                            callback.onError(
                                    "Usuário interessado não encontrado"
                            );
                            return;
                        }

                        Negociacao negociacao =
                                new Negociacao();

                        negociacao.setId(
                                UUID.randomUUID().toString()
                        );

                        negociacao.setUsuarioAnuncianteId(
                                anuncio.getUidUsuario()
                        );

                        negociacao.setUsuarioCompradorId(
                                uidInteressado
                        );

                        negociacao.setAnuncioId(
                                anuncio.getId()
                        );

                        negociacao.setDataNegociacao(
                                Timestamp.now()
                        );

                        negociacao.setTipoNegociacao(
                                tipoNegociacao
                        );

                        negociacao.setStatus(
                                Negociacao.StatusNegociacao.PENDENTE
                        );

                        negociacao.setNmAnunciante(
                                anuncio.getNomeUsuario()
                        );

                        negociacao.setFotoPerfilAnunciante(
                                anuncio.getFotoPerfil()
                        );

                        negociacao.setNmComprador(
                                usuario.getNome()
                        );

                        negociacao.setFotoPerfilComprador(
                                usuario.getFotoPerfil()
                        );

                        negociacao.setTitulo(
                                anuncio.getTitulo()
                        );

                        negociacao.setCapa(
                                anuncio.getCapa()
                        );

                        negociacao.setConfirmacaoAnunciante(
                                false
                        );

                        negociacao.setConfirmacaoComprador(
                                false
                        );

                        atualizarDadosOferta(
                                negociacao,
                                dadosOferta
                        );

                        salvar(
                                negociacao,
                                callback
                        );
                    }

                    @Override
                    public void onErro(String erro) {
                        callback.onError(erro);
                    }
                }
        );
    }

    // =========================================================
    // VALIDAÇÃO DO TIPO
    // =========================================================

    private boolean validarTipoNegociacao(
            Anuncio anuncio,
            Negociacao.TipoNegociacao tipoNegociacao,
            Callback<?> callback
    ) {

        if (tipoNegociacao
                == Negociacao.TipoNegociacao.AMBOS) {

            callback.onError(
                    "A negociação deve ser definida como TROCA ou VENDA"
            );

            return false;
        }

        Anuncio.TipoNegociacao tipoAnuncio =
                anuncio.getTipoNegociacao();

        boolean compativel =
                tipoAnuncio == Anuncio.TipoNegociacao.AMBOS
                        || tipoAnuncio.name()
                        .equals(tipoNegociacao.name());

        if (!compativel) {

            callback.onError(
                    "Tipo de negociação incompatível com o anúncio"
            );

            return false;
        }

        return true;
    }

    // =========================================================
    // OFERTA
    // =========================================================

    private void validarEPrepararOferta(
            Anuncio anuncioPrincipal,
            String uidInteressado,
            Negociacao.TipoNegociacao tipoNegociacao,
            String anuncioOferecidoId,
            String descricaoOferta,
            Callback<DadosOferta> callback
    ) {

        /*
         * Venda não possui livro oferecido.
         */
        if (tipoNegociacao
                == Negociacao.TipoNegociacao.VENDA) {

            callback.onSuccess(
                    new DadosOferta(
                            null,
                            null,
                            null,
                            null
                    )
            );

            return;
        }

        String descricaoNormalizada =
                descricaoOferta == null
                        ? null
                        : descricaoOferta.trim();

        if (descricaoNormalizada != null
                && descricaoNormalizada.length() > 500) {

            callback.onError(
                    "A descrição da oferta deve possuir no máximo 500 caracteres."
            );

            return;
        }

        boolean possuiAnuncioOferecido =
                anuncioOferecidoId != null
                        && !anuncioOferecidoId
                        .trim()
                        .isEmpty();

        /*
         * TROCA com livro que ainda não está anunciado.
         */
        if (!possuiAnuncioOferecido) {

            if (descricaoNormalizada == null
                    || descricaoNormalizada.isEmpty()) {

                callback.onError(
                        "Informe o livro que deseja oferecer para a troca."
                );

                return;
            }

            callback.onSuccess(
                    new DadosOferta(
                            null,
                            null,
                            null,
                            descricaoNormalizada
                    )
            );

            return;
        }

        /*
         * TROCA utilizando outro anúncio.
         */
        anuncioService.buscarAnuncioPorId(
                anuncioOferecidoId,
                new AnuncioService.Callback<Anuncio>() {

                    @Override
                    public void onSuccess(
                            Anuncio anuncioOferecido
                    ) {

                        if (anuncioOferecido == null) {

                            callback.onError(
                                    "O anúncio oferecido não foi encontrado ou não está ativo."
                            );

                            return;
                        }

                        if (anuncioPrincipal.getId().equals(
                                anuncioOferecido.getId()
                        )) {

                            callback.onError(
                                    "O anúncio não pode ser oferecido em troca por ele mesmo."
                            );

                            return;
                        }

                        if (!uidInteressado.equals(
                                anuncioOferecido.getUidUsuario()
                        )) {

                            callback.onError(
                                    "O usuário não possui permissão para oferecer este anúncio."
                            );

                            return;
                        }

                        boolean aceitaTroca =
                                anuncioOferecido.getTipoNegociacao()
                                        == Anuncio.TipoNegociacao.TROCA
                                        ||
                                        anuncioOferecido.getTipoNegociacao()
                                                == Anuncio.TipoNegociacao.AMBOS;

                        if (!aceitaTroca) {

                            callback.onError(
                                    "O anúncio oferecido não está disponível para troca."
                            );

                            return;
                        }

                        callback.onSuccess(
                                new DadosOferta(
                                        anuncioOferecido.getId(),
                                        anuncioOferecido.getTitulo(),
                                        anuncioOferecido.getCapa(),
                                        descricaoNormalizada
                                )
                        );
                    }

                    @Override
                    public void onError(String mensagem) {
                        callback.onError(mensagem);
                    }
                }
        );
    }

    private void atualizarDadosOferta(
            Negociacao negociacao,
            DadosOferta dadosOferta
    ) {

        negociacao.setAnuncioOferecidoId(
                dadosOferta.anuncioId
        );

        negociacao.setTituloLivroOferecido(
                dadosOferta.titulo
        );

        negociacao.setCapaLivroOferecido(
                dadosOferta.capa
        );

        negociacao.setDescricaoOferta(
                dadosOferta.descricao
        );
    }

    // =========================================================
    // BUSCA POR ANÚNCIO + COMPRADOR
    // =========================================================

    public void buscarPorAnuncioEComprador(
            String anuncioId,
            String usuarioCompradorId,
            Callback<Negociacao> callback
    ) {

        if (anuncioId == null
                || anuncioId.trim().isEmpty()) {

            callback.onError("Anúncio inválido");
            return;
        }

        if (usuarioCompradorId == null
                || usuarioCompradorId.trim().isEmpty()) {

            callback.onError("Usuário inválido");
            return;
        }

        negociacaoRepository.buscarPorAnuncioEComprador(
                anuncioId,
                usuarioCompradorId,
                new NegociacaoRepository.Callback<Negociacao>() {

                    @Override
                    public void onSucesso(Negociacao dados) {
                        callback.onSuccess(dados);
                    }

                    @Override
                    public void onErro(Exception erro) {
                        callback.onError(
                                mensagemErro(erro)
                        );
                    }
                }
        );
    }

    // =========================================================
    // ACEITAR
    // =========================================================

    public void aceitar(
            String uidNegociacao,
            String uidUsuario,
            Callback<Negociacao> callback
    ) {

        if (uidNegociacaoInvalido(uidNegociacao)) {
            callback.onError(
                    "ID da negociação inválido"
            );
            return;
        }

        if (uidUsuario == null
                || uidUsuario.trim().isEmpty()) {

            callback.onError("Usuário inválido");
            return;
        }

        buscarNegociacaoInterna(
                uidNegociacao,
                new Callback<Negociacao>() {

                    @Override
                    public void onSuccess(
                            Negociacao negociacao
                    ) {

                        if (negociacao == null) {

                            callback.onError(
                                    "Negociação não encontrada"
                            );

                            return;
                        }

                        if (!uidUsuario.equals(
                                negociacao.getUsuarioAnuncianteId()
                        )) {

                            callback.onError(
                                    "Somente o anunciante pode aceitar a negociação"
                            );

                            return;
                        }

                        if (negociacao.getStatus()
                                != Negociacao.StatusNegociacao.PENDENTE) {

                            callback.onError(
                                    "A negociação não está pendente"
                            );

                            return;
                        }

                        validarAnuncioPrincipalParaAceite(
                                negociacao,
                                new Callback<Void>() {

                                    @Override
                                    public void onSuccess(Void dados) {

                                        validarAnuncioOferecidoParaAceite(
                                                negociacao,
                                                new Callback<Void>() {

                                                    @Override
                                                    public void onSuccess(
                                                            Void dados
                                                    ) {

                                                        negociacao.setStatus(
                                                                Negociacao.StatusNegociacao.EM_ANDAMENTO
                                                        );

                                                        salvar(
                                                                negociacao,
                                                                callback
                                                        );
                                                    }

                                                    @Override
                                                    public void onError(
                                                            String erro
                                                    ) {
                                                        callback.onError(
                                                                erro
                                                        );
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

                    @Override
                    public void onError(String erro) {
                        callback.onError(erro);
                    }
                }
        );
    }

    // =========================================================
    // RECUSAR
    // =========================================================

    public void recusar(
            String uidNegociacao,
            String uidUsuario,
            Callback<Negociacao> callback
    ) {

        if (uidNegociacaoInvalido(uidNegociacao)) {
            callback.onError(
                    "ID da negociação inválido"
            );
            return;
        }

        if (uidUsuario == null
                || uidUsuario.trim().isEmpty()) {

            callback.onError("Usuário inválido");
            return;
        }

        buscarNegociacaoInterna(
                uidNegociacao,
                new Callback<Negociacao>() {

                    @Override
                    public void onSuccess(
                            Negociacao negociacao
                    ) {

                        if (negociacao == null) {

                            callback.onError(
                                    "Negociação não encontrada"
                            );

                            return;
                        }

                        if (!uidUsuario.equals(
                                negociacao.getUsuarioAnuncianteId()
                        )) {

                            callback.onError(
                                    "Somente o anunciante pode recusar a negociação"
                            );

                            return;
                        }

                        if (negociacao.getStatus()
                                != Negociacao.StatusNegociacao.PENDENTE) {

                            callback.onError(
                                    "A negociação não está pendente"
                            );

                            return;
                        }

                        negociacao.setStatus(
                                Negociacao.StatusNegociacao.RECUSADA
                        );

                        salvar(
                                negociacao,
                                callback
                        );
                    }

                    @Override
                    public void onError(String erro) {
                        callback.onError(erro);
                    }
                }
        );
    }

    // =========================================================
    // MÉTODOS BÁSICOS
    // =========================================================

    public void salvar(
            Negociacao negociacao,
            Callback<Negociacao> callback
    ) {

        if (negociacao == null) {
            callback.onError("Negociação inválida");
            return;
        }

        if (negociacao.getId() == null
                || negociacao.getId().isEmpty()) {

            negociacao.setId(
                    UUID.randomUUID().toString()
            );
        }

        negociacaoRepository.salvar(
                negociacao,
                new NegociacaoRepository.Callback<Negociacao>() {

                    @Override
                    public void onSucesso(Negociacao dados) {
                        callback.onSuccess(dados);
                    }

                    @Override
                    public void onErro(Exception erro) {
                        callback.onError(
                                mensagemErro(erro)
                        );
                    }
                }
        );
    }

    public void buscarPorUid(
            String uid,
            Callback<Negociacao> callback
    ) {

        if (uid == null || uid.trim().isEmpty()) {
            callback.onError(
                    "ID da negociação inválido"
            );
            return;
        }

        buscarNegociacaoInterna(
                uid,
                callback
        );
    }

    public void cancelar(
            String uidNegociacao,
            String uidUsuario,
            Callback<Negociacao> callback
    ) {
        if (uidNegociacaoInvalido(uidNegociacao)) {
            callback.onError(
                    "ID da negociação inválido"
            );
            return;
        }

        if (uidUsuario == null
                || uidUsuario.trim().isEmpty()) {

            callback.onError("Usuário inválido");
            return;
        }

        buscarNegociacaoInterna(
                uidNegociacao,
                new Callback<Negociacao>() {

                    @Override
                    public void onSuccess(
                            Negociacao negociacao
                    ) {

                        if (negociacao == null) {

                            callback.onError(
                                    "Negociação não encontrada"
                            );

                            return;
                        }

                        if (negociacao.getStatus()
                                != Negociacao.StatusNegociacao.EM_ANDAMENTO) {

                            callback.onError(
                                    "Somente negociações em andamento podem ser canceladas"
                            );

                            return;
                        }

                        boolean usuarioEhAnunciante =
                                uidUsuario.equals(
                                        negociacao
                                                .getUsuarioAnuncianteId()
                                );

                        boolean usuarioEhComprador =
                                uidUsuario.equals(
                                        negociacao
                                                .getUsuarioCompradorId()
                                );

                        if (!usuarioEhAnunciante
                                && !usuarioEhComprador) {

                            callback.onError(
                                    "Usuário não autorizado a cancelar esta negociação"
                            );

                            return;
                        }

                        negociacao.setStatus(
                                Negociacao.StatusNegociacao.CANCELADA
                        );

                        salvar(
                                negociacao,
                                callback
                        );
                    }

                    @Override
                    public void onError(String erro) {
                        callback.onError(erro);
                    }
                }
        );
    }

    public void confirmar(
            String uidNegociacao,
            String uidUsuario,
            Callback<Negociacao> callback
    ) {

        if (uidNegociacaoInvalido(uidNegociacao)) {
            callback.onError(
                    "ID da negociação inválido"
            );
            return;
        }

        if (uidUsuario == null
                || uidUsuario.trim().isEmpty()) {

            callback.onError("Usuário inválido");
            return;
        }

        buscarNegociacaoInterna(
                uidNegociacao,
                new Callback<Negociacao>() {

                    @Override
                    public void onSuccess(
                            Negociacao negociacao
                    ) {

                        if (negociacao == null) {
                            callback.onError(
                                    "Negociação não encontrada"
                            );
                            return;
                        }

                        if (negociacao.getStatus()
                                != Negociacao.StatusNegociacao.EM_ANDAMENTO) {

                            callback.onError(
                                    "A negociação não está em andamento"
                            );
                            return;
                        }

                        boolean usuarioEhAnunciante =
                                uidUsuario.equals(
                                        negociacao.getUsuarioAnuncianteId()
                                );

                        boolean usuarioEhComprador =
                                uidUsuario.equals(
                                        negociacao.getUsuarioCompradorId()
                                );

                        if (!usuarioEhAnunciante
                                && !usuarioEhComprador) {

                            callback.onError(
                                    "Usuário não autorizado a confirmar esta negociação"
                            );
                            return;
                        }

                        if (usuarioEhAnunciante) {

                            if (negociacao.isConfirmacaoAnunciante()) {

                                callback.onError(
                                        "O anunciante já confirmou esta negociação"
                                );
                                return;
                            }

                            negociacao.setConfirmacaoAnunciante(true);

                        } else {

                            if (negociacao.isConfirmacaoComprador()) {

                                callback.onError(
                                        "O comprador já confirmou esta negociação"
                                );
                                return;
                            }

                            negociacao.setConfirmacaoComprador(true);
                        }

                        /*
                         * Se ambos confirmaram, concluímos todo
                         * o processo da negociação.
                         */
                        if (negociacao.isConfirmacaoAnunciante()
                                && negociacao.isConfirmacaoComprador()) {

                            finalizarNegociacao(
                                    negociacao,
                                    callback
                            );

                            return;
                        }

                        /*
                         * Apenas um dos participantes confirmou.
                         */
                        salvar(
                                negociacao,
                                callback
                        );
                    }

                    @Override
                    public void onError(String erro) {
                        callback.onError(erro);
                    }
                }
        );
    }

    public void buscarPorUidParaUsuario(
            String uidNegociacao,
            String uidUsuario,
            Callback<Negociacao> callback
    ) {

        if (uidNegociacaoInvalido(uidNegociacao)) {
            callback.onError(
                    "ID da negociação inválido"
            );
            return;
        }

        if (uidUsuario == null
                || uidUsuario.trim().isEmpty()) {

            callback.onError("Usuário inválido");
            return;
        }

        buscarNegociacaoInterna(
                uidNegociacao,
                new Callback<Negociacao>() {

                    @Override
                    public void onSuccess(
                            Negociacao negociacao
                    ) {

                        if (negociacao == null) {

                            callback.onError(
                                    "Negociação não encontrada"
                            );

                            return;
                        }

                        boolean usuarioEhAnunciante =
                                uidUsuario.equals(
                                        negociacao
                                                .getUsuarioAnuncianteId()
                                );

                        boolean usuarioEhComprador =
                                uidUsuario.equals(
                                        negociacao
                                                .getUsuarioCompradorId()
                                );

                        if (!usuarioEhAnunciante
                                && !usuarioEhComprador) {

                            callback.onError(
                                    "Usuário não autorizado a acessar esta negociação"
                            );

                            return;
                        }

                        if (negociacao.getStatus()
                                == Negociacao.StatusNegociacao.PENDENTE) {

                            callback.onError(
                                    "A proposta ainda não foi aceita"
                            );

                            return;
                        }

                        if (negociacao.getStatus()
                                == Negociacao.StatusNegociacao.RECUSADA) {

                            callback.onError(
                                    "Esta proposta foi recusada"
                            );

                            return;
                        }

                        if (negociacao.getStatus()
                                == Negociacao.StatusNegociacao.CANCELADA) {

                            callback.onError(
                                    "Esta negociação foi cancelada"
                            );

                            return;
                        }

                        callback.onSuccess(
                                negociacao
                        );
                    }

                    @Override
                    public void onError(String erro) {
                        callback.onError(erro);
                    }
                }
        );
    }

    public void listarFinalizadasPorUsuario(
            String uidUsuario,
            Callback<List<Negociacao>> callback
    ) {

        if (uidUsuario == null
                || uidUsuario.trim().isEmpty()) {

            callback.onError("Usuário inválido");
            return;
        }

        negociacaoRepository.buscarFinalizadasPorUsuario(
                uidUsuario,
                new NegociacaoRepository.Callback<List<Negociacao>>() {

                    @Override
                    public void onSucesso(
                            List<Negociacao> dados
                    ) {
                        callback.onSuccess(dados);
                    }

                    @Override
                    public void onErro(Exception erro) {
                        callback.onError(
                                mensagemErro(erro)
                        );
                    }
                }
        );
    }

    public void listarFinalizadasPorUsuarioETipo(
            String uidUsuario,
            Negociacao.TipoNegociacao tipoNegociacao,
            Callback<List<Negociacao>> callback
    ) {

        if (uidUsuario == null
                || uidUsuario.trim().isEmpty()) {

            callback.onError("Usuário inválido");
            return;
        }

        if (tipoNegociacao == null) {

            callback.onError(
                    "Tipo de negociação inválido"
            );
            return;
        }

        if (tipoNegociacao
                == Negociacao.TipoNegociacao.AMBOS) {

            callback.onError(
                    "O histórico deve ser filtrado por TROCA ou VENDA"
            );
            return;
        }

        negociacaoRepository.buscarFinalizadasPorUsuarioETipo(
                uidUsuario,
                tipoNegociacao,
                new NegociacaoRepository.Callback<List<Negociacao>>() {

                    @Override
                    public void onSucesso(
                            List<Negociacao> dados
                    ) {
                        callback.onSuccess(dados);
                    }

                    @Override
                    public void onErro(Exception erro) {
                        callback.onError(
                                mensagemErro(erro)
                        );
                    }
                }
        );
    }

    private void buscarNegociacaoInterna(
            String uid,
            Callback<Negociacao> callback
    ) {

        negociacaoRepository.buscarPorUid(
                uid,
                new NegociacaoRepository.Callback<Negociacao>() {

                    @Override
                    public void onSucesso(Negociacao dados) {
                        callback.onSuccess(dados);
                    }

                    @Override
                    public void onErro(Exception erro) {
                        callback.onError(
                                mensagemErro(erro)
                        );
                    }
                }
        );
    }

    private String mensagemErro(Exception erro) {

        if (erro == null
                || erro.getMessage() == null
                || erro.getMessage().trim().isEmpty()) {

            return "Erro ao processar negociação";
        }

        return erro.getMessage();
    }

    private static class DadosOferta {

        private final String anuncioId;
        private final String titulo;
        private final String capa;
        private final String descricao;

        private DadosOferta(
                String anuncioId,
                String titulo,
                String capa,
                String descricao
        ) {
            this.anuncioId = anuncioId;
            this.titulo = titulo;
            this.capa = capa;
            this.descricao = descricao;
        }
    }

    private boolean uidNegociacaoInvalido(
            String uidNegociacao
    ) {
        return uidNegociacao == null
                || uidNegociacao.trim().isEmpty();
    }

    private void validarAnuncioPrincipalParaAceite(
            Negociacao negociacao,
            Callback<Void> callback
    ) {

        anuncioService.buscarAnuncioPorId(
                negociacao.getAnuncioId(),
                new AnuncioService.Callback<Anuncio>() {

                    @Override
                    public void onSuccess(Anuncio anuncio) {

                        if (anuncio == null) {

                            callback.onError(
                                    "Este anúncio não está mais disponível."
                            );

                            return;
                        }

                        possuiNegociacaoEmAndamento(
                                negociacao.getAnuncioId(),
                                negociacao.getId(),
                                new Callback<Boolean>() {

                                    @Override
                                    public void onSuccess(Boolean possui) {

                                        if (Boolean.TRUE.equals(possui)) {

                                            callback.onError(
                                                    "Este anúncio já está comprometido em outra negociação."
                                            );

                                            return;
                                        }

                                        callback.onSuccess(null);
                                    }

                                    @Override
                                    public void onError(String erro) {
                                        callback.onError(erro);
                                    }
                                }
                        );
                    }

                    @Override
                    public void onError(String mensagem) {
                        callback.onError(mensagem);
                    }
                }
        );
    }

    private void validarAnuncioOferecidoParaAceite(
            Negociacao negociacao,
            Callback<Void> callback
    ) {

        if (negociacao.getTipoNegociacao()
                != Negociacao.TipoNegociacao.TROCA) {

            callback.onSuccess(null);
            return;
        }

        String anuncioOferecidoId =
                negociacao.getAnuncioOferecidoId();

        /*
         * Troca com livro não anunciado.
         */
        if (anuncioOferecidoId == null
                || anuncioOferecidoId.trim().isEmpty()) {

            callback.onSuccess(null);
            return;
        }

        anuncioService.buscarAnuncioPorId(
                anuncioOferecidoId,
                new AnuncioService.Callback<Anuncio>() {

                    @Override
                    public void onSuccess(
                            Anuncio anuncioOferecido
                    ) {

                        if (anuncioOferecido == null) {

                            callback.onError(
                                    "O livro oferecido não está mais disponível."
                            );

                            return;
                        }

                        /*
                         * O anúncio oferecido precisa continuar
                         * pertencendo ao comprador.
                         */
                        if (!negociacao
                                .getUsuarioCompradorId()
                                .equals(
                                        anuncioOferecido
                                                .getUidUsuario()
                                )) {

                            callback.onError(
                                    "O anúncio oferecido não pertence mais ao usuário que realizou a proposta."
                            );

                            return;
                        }

                        boolean aceitaTroca =
                                anuncioOferecido.getTipoNegociacao()
                                        == Anuncio.TipoNegociacao.TROCA
                                        ||
                                        anuncioOferecido.getTipoNegociacao()
                                                == Anuncio.TipoNegociacao.AMBOS;

                        if (!aceitaTroca) {

                            callback.onError(
                                    "O anúncio oferecido não está mais disponível para troca."
                            );

                            return;
                        }

                        possuiNegociacaoEmAndamento(
                                anuncioOferecidoId,
                                negociacao.getId(),
                                new Callback<Boolean>() {

                                    @Override
                                    public void onSuccess(
                                            Boolean possui
                                    ) {

                                        if (Boolean.TRUE.equals(
                                                possui
                                        )) {

                                            callback.onError(
                                                    "O livro oferecido já está comprometido em outra negociação."
                                            );

                                            return;
                                        }

                                        callback.onSuccess(null);
                                    }

                                    @Override
                                    public void onError(
                                            String erro
                                    ) {
                                        callback.onError(erro);
                                    }
                                }
                        );
                    }

                    @Override
                    public void onError(String mensagem) {
                        callback.onError(mensagem);
                    }
                }
        );
    }

    private void possuiNegociacaoEmAndamento(
            String anuncioId,
            String negociacaoIgnoradaId,
            Callback<Boolean> callback
    ) {

        negociacaoRepository.buscarPorAnuncioId(
                anuncioId,
                new NegociacaoRepository.Callback<List<Negociacao>>() {

                    @Override
                    public void onSucesso(
                            List<Negociacao> negociacoes
                    ) {

                        if (existeOutraEmAndamento(
                                negociacoes,
                                negociacaoIgnoradaId
                        )) {

                            callback.onSuccess(true);
                            return;
                        }

                        /*
                         * Não apareceu como anúncio principal.
                         * Agora verificamos se está sendo utilizado
                         * como anúncio oferecido em outra troca.
                         */
                        negociacaoRepository.buscarPorAnuncioOferecidoId(
                                anuncioId,
                                new NegociacaoRepository.Callback<List<Negociacao>>() {

                                    @Override
                                    public void onSucesso(
                                            List<Negociacao> negociacoesOferecidas
                                    ) {

                                        callback.onSuccess(
                                                existeOutraEmAndamento(
                                                        negociacoesOferecidas,
                                                        negociacaoIgnoradaId
                                                )
                                        );
                                    }

                                    @Override
                                    public void onErro(Exception erro) {
                                        callback.onError(
                                                mensagemErro(erro)
                                        );
                                    }
                                }
                        );
                    }

                    @Override
                    public void onErro(Exception erro) {
                        callback.onError(
                                mensagemErro(erro)
                        );
                    }
                }
        );
    }

    private void finalizarNegociacao(
            Negociacao negociacao,
            Callback<Negociacao> callback
    ) {

        negociacao.setStatus(
                Negociacao.StatusNegociacao.FINALIZADA
        );

        anuncioService.finalizarAnuncio(
                negociacao.getAnuncioId(),
                new AnuncioService.Callback<Anuncio>() {

                    @Override
                    public void onSuccess(
                            Anuncio anuncioFinalizado
                    ) {

                        if (possuiAnuncioOferecido(negociacao)) {

                            anuncioService.finalizarAnuncio(
                                    negociacao.getAnuncioOferecidoId(),
                                    new AnuncioService.Callback<Anuncio>() {

                                        @Override
                                        public void onSuccess(
                                                Anuncio anuncioOferecidoFinalizado
                                        ) {

                                            finalizarPersistenciaNegociacao(
                                                    negociacao,
                                                    callback
                                            );
                                        }

                                        @Override
                                        public void onError(String erro) {
                                            callback.onError(erro);
                                        }
                                    }
                            );

                            return;
                        }

                        finalizarPersistenciaNegociacao(
                                negociacao,
                                callback
                        );
                    }

                    @Override
                    public void onError(String erro) {
                        callback.onError(erro);
                    }
                }
        );
    }

    private boolean possuiAnuncioOferecido(
            Negociacao negociacao
    ) {

        return negociacao.getAnuncioOferecidoId() != null
                && !negociacao.getAnuncioOferecidoId()
                .trim()


                .isEmpty();
    }

    private void finalizarPersistenciaNegociacao(
            Negociacao negociacao,
            Callback<Negociacao> callback
    ) {

        salvar(
                negociacao,
                new Callback<Negociacao>() {

                    @Override
                    public void onSuccess(
                            Negociacao negociacaoFinalizada
                    ) {

                        cancelarNegociacoesPendentesDosAnuncios(
                                negociacaoFinalizada,
                                new Callback<Void>() {

                                    @Override
                                    public void onSuccess(Void dados) {

                                        callback.onSuccess(
                                                negociacaoFinalizada
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

    private void cancelarNegociacoesPendentesDosAnuncios(
            Negociacao negociacaoFinalizada,
            Callback<Void> callback
    ) {

        cancelarNegociacoesPendentesDoAnuncio(
                negociacaoFinalizada.getAnuncioId(),
                negociacaoFinalizada.getId(),
                new Callback<Void>() {

                    @Override
                    public void onSuccess(Void dados) {

                        if (!possuiAnuncioOferecido(
                                negociacaoFinalizada
                        )) {

                            callback.onSuccess(null);
                            return;
                        }

                        cancelarNegociacoesPendentesDoAnuncio(
                                negociacaoFinalizada
                                        .getAnuncioOferecidoId(),
                                negociacaoFinalizada.getId(),
                                callback
                        );
                    }

                    @Override
                    public void onError(String erro) {
                        callback.onError(erro);
                    }
                }
        );
    }

    private void cancelarNegociacoesPendentesDoAnuncio(
            String anuncioId,
            String negociacaoFinalizadaId,
            Callback<Void> callback
    ) {

        negociacaoRepository.buscarPorAnuncioId(
                anuncioId,
                new NegociacaoRepository.Callback<List<Negociacao>>() {

                    @Override
                    public void onSucesso(
                            List<Negociacao> comoPrincipal
                    ) {

                        negociacaoRepository.buscarPorAnuncioOferecidoId(
                                anuncioId,
                                new NegociacaoRepository.Callback<List<Negociacao>>() {

                                    @Override
                                    public void onSucesso(
                                            List<Negociacao> comoOferecido
                                    ) {

                                        List<Negociacao> pendentes =
                                                new java.util.ArrayList<>();

                                        adicionarPendentesSemDuplicar(
                                                pendentes,
                                                comoPrincipal,
                                                negociacaoFinalizadaId
                                        );

                                        adicionarPendentesSemDuplicar(
                                                pendentes,
                                                comoOferecido,
                                                negociacaoFinalizadaId
                                        );

                                        cancelarListaPendentes(
                                                pendentes,
                                                0,
                                                callback
                                        );
                                    }

                                    @Override
                                    public void onErro(Exception erro) {
                                        callback.onError(
                                                mensagemErro(erro)
                                        );
                                    }
                                }
                        );
                    }

                    @Override
                    public void onErro(Exception erro) {
                        callback.onError(
                                mensagemErro(erro)
                        );
                    }
                }
        );
    }

    private void adicionarPendentesSemDuplicar(
            List<Negociacao> destino,
            List<Negociacao> origem,
            String negociacaoFinalizadaId
    ) {

        if (origem == null) {
            return;
        }

        for (Negociacao negociacao : origem) {

            if (negociacao == null) {
                continue;
            }

            if (negociacaoFinalizadaId.equals(
                    negociacao.getId()
            )) {
                continue;
            }

            if (negociacao.getStatus()
                    != Negociacao.StatusNegociacao.PENDENTE) {
                continue;
            }

            boolean jaAdicionada = false;

            for (Negociacao existente : destino) {

                if (existente.getId().equals(
                        negociacao.getId()
                )) {

                    jaAdicionada = true;
                    break;
                }
            }

            if (!jaAdicionada) {
                destino.add(negociacao);
            }
        }
    }

    private void cancelarListaPendentes(
            List<Negociacao> negociacoes,
            int indice,
            Callback<Void> callback
    ) {

        if (negociacoes == null
                || indice >= negociacoes.size()) {

            callback.onSuccess(null);
            return;
        }

        Negociacao negociacao =
                negociacoes.get(indice);

        negociacao.setStatus(
                Negociacao.StatusNegociacao.CANCELADA
        );

        salvar(
                negociacao,
                new Callback<Negociacao>() {

                    @Override
                    public void onSuccess(
                            Negociacao negociacaoCancelada
                    ) {

                        cancelarListaPendentes(
                                negociacoes,
                                indice + 1,
                                callback
                        );
                    }

                    @Override
                    public void onError(String erro) {
                        callback.onError(erro);
                    }
                }
        );
    }

    private boolean existeOutraEmAndamento(
            List<Negociacao> negociacoes,
            String negociacaoIgnoradaId
    ) {

        if (negociacoes == null
                || negociacoes.isEmpty()) {

            return false;
        }

        for (Negociacao negociacao : negociacoes) {

            if (negociacao == null) {
                continue;
            }

            /*
             * Ignora a própria negociação que está
             * sendo aceita.
             */
            if (negociacaoIgnoradaId != null
                    && negociacaoIgnoradaId.equals(
                    negociacao.getId()
            )) {

                continue;
            }

            if (negociacao.getStatus()
                    == Negociacao.StatusNegociacao.EM_ANDAMENTO) {

                return true;
            }
        }

        return false;
    }



    public void listarPorUsuarioAnunciante(
            String uidAnunciante,
            Callback<List<Negociacao>> callback
    ) {

        if (uidAnunciante == null || uidAnunciante.isEmpty()) {
            callback.onError("Usuário anunciante inválido");
            return;
        }

        negociacaoRepository.buscarPorUidUsuarioAnunciante(
                uidAnunciante,
                new NegociacaoRepository.Callback<List<Negociacao>>() {

                    @Override
                    public void onSucesso(List<Negociacao> dados) {
                        callback.onSuccess(dados);
                    }

                    @Override
                    public void onErro(Exception erro) {
                        callback.onError(erro.getMessage());
                    }
                }
        );
    }

    public void listarPorUsuarioComprador(
            String uidComprador,
            Callback<List<Negociacao>> callback
    ) {

        if (uidComprador == null || uidComprador.isEmpty()) {
            callback.onError("Usuário comprador inválido");
            return;
        }

        negociacaoRepository.buscarPorUidUsuarioComprador(
                uidComprador,
                new NegociacaoRepository.Callback<List<Negociacao>>() {

                    @Override
                    public void onSucesso(List<Negociacao> dados) {
                        callback.onSuccess(dados);
                    }

                    @Override
                    public void onErro(Exception erro) {
                        callback.onError(erro.getMessage());
                    }
                }
        );
    }

    public void listarPorUsuarioAnuncianteETipo(
            String uidAnunciante,
            Negociacao.TipoNegociacao tipoNegociacao,
            Callback<List<Negociacao>> callback
    ) {

        if (uidAnunciante == null || uidAnunciante.isEmpty()) {
            callback.onError("Usuário anunciante inválido");
            return;
        }

        if (tipoNegociacao == null) {
            callback.onError("Tipo de negociação inválido");
            return;
        }

        negociacaoRepository.buscarPorUidAnuncianteETipoNegociacao(
                uidAnunciante,
                tipoNegociacao,
                new NegociacaoRepository.Callback<List<Negociacao>>() {

                    @Override
                    public void onSucesso(List<Negociacao> dados) {
                        callback.onSuccess(dados);
                    }

                    @Override
                    public void onErro(Exception erro) {
                        callback.onError(erro.getMessage());
                    }
                }
        );
    }

    public void listarPorUsuarioCompradorETipo(
            String uidComprador,
            Negociacao.TipoNegociacao tipoNegociacao,
            Callback<List<Negociacao>> callback
    ) {

        if (uidComprador == null || uidComprador.isEmpty()) {
            callback.onError("Usuário comprador inválido");
            return;
        }

        if (tipoNegociacao == null) {
            callback.onError("Tipo de negociação inválido");
            return;
        }

        negociacaoRepository.buscarPorUidCompradorETipoNegociacao(
                uidComprador,
                tipoNegociacao,
                new NegociacaoRepository.Callback<List<Negociacao>>() {

                    @Override
                    public void onSucesso(List<Negociacao> dados) {
                        callback.onSuccess(dados);
                    }

                    @Override
                    public void onErro(Exception erro) {
                        callback.onError(erro.getMessage());
                    }
                }
        );
    }

    public void contarNegociacoesPorUsuarioETipo(
            String uidAnunciante,
            Negociacao.TipoNegociacao tipoNegociacao,
            Callback<Long> callback
    ) {

        if (uidAnunciante == null || uidAnunciante.isEmpty()) {
            callback.onError("Usuário anunciante inválido");
            return;
        }

        if (tipoNegociacao == null) {
            callback.onError("Tipo de negociação inválido");
            return;
        }

        negociacaoRepository.contarNegociacoesPorUsuarioETipo(
                uidAnunciante,
                tipoNegociacao,
                new NegociacaoRepository.Callback<Long>() {

                    @Override
                    public void onSucesso(Long dados) {
                        callback.onSuccess(dados);
                    }

                    @Override
                    public void onErro(Exception erro) {
                        callback.onError(erro.getMessage());
                    }
                }
        );
    }

    public void buscarTodos(Callback<List<Negociacao>> callback){
        negociacaoRepository.buscarTodos(new NegociacaoRepository.Callback<List<Negociacao>>() {
            @Override
            public void onSucesso(List<Negociacao> dados) {
                callback.onSuccess(dados);
            }

            @Override
            public void onErro(Exception e) {
                callback.onError(e.getMessage());
            }
        });
    }
}