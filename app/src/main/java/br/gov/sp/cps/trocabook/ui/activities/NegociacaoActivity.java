package br.gov.sp.cps.trocabook.ui.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseUser;

import br.gov.sp.cps.trocabook.R;
import br.gov.sp.cps.trocabook.model.Negociacao;
import br.gov.sp.cps.trocabook.service.AuthService;
import br.gov.sp.cps.trocabook.service.NegociacoesService;

public class NegociacaoActivity extends AppCompatActivity {

    private String negociacaoId;
    private String usuarioLogadoId;

    private Negociacao negociacaoAtual;

    private AuthService authService;
    private NegociacoesService negociacoesService;

    // Cabeçalho
    private TextView txtStatusNegociacao;

    // Livro negociado
    private ImageView imgLivroNegociado;
    private TextView txtTituloLivroNegociado;

    // Tipo
    private TextView txtIconeTipoNegociacao;
    private TextView txtTipoNegociacao;

    // Livro oferecido
    private MaterialCardView cardLivroOferecido;
    private ImageView imgLivroOferecido;
    private TextView txtTituloLivroOferecido;

    private TextView txtObservacaoLivroOferecido;

    // Livro não anunciado
    private MaterialCardView cardLivroNaoAnunciado;
    private TextView txtDescricaoLivroNaoAnunciado;

    // Venda
    private MaterialCardView cardVenda;

    // Participantes
    private ImageView imgAnunciante;
    private TextView txtNomeAnunciante;
    private TextView txtConfirmacaoAnunciante;

    private ImageView imgInteressado;
    private TextView txtNomeInteressado;
    private TextView txtConfirmacaoInteressado;

    // Controles
    private MaterialCardView layoutControleNegociacao;
    private TextView txtMinhaConfirmacao;
    private MaterialButton btnConfirmarNegociacao;
    private MaterialButton btnCancelarNegociacao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_negociacao);

        configurarInsets();

        inicializarViews();
        inicializarServices();

        if (!validarUsuario()) {
            return;
        }

        if (!obterNegociacaoId()) {
            return;
        }

        configurarEventos();

        carregarNegociacao();
    }

    private void configurarInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(
                findViewById(R.id.main),
                (v, insets) -> {

                    Insets systemBars =
                            insets.getInsets(
                                    WindowInsetsCompat.Type.systemBars()
                            );

                    v.setPadding(
                            systemBars.left,
                            systemBars.top,
                            systemBars.right,
                            systemBars.bottom
                    );

                    return insets;
                }
        );
    }

    private void inicializarViews() {

        txtStatusNegociacao =
                findViewById(R.id.txtStatusNegociacao);

        imgLivroNegociado =
                findViewById(R.id.imgLivroNegociado);

        txtTituloLivroNegociado =
                findViewById(R.id.txtTituloLivroNegociado);

        txtIconeTipoNegociacao =
                findViewById(R.id.txtIconeTipoNegociacao);

        txtTipoNegociacao =
                findViewById(R.id.txtTipoNegociacao);

        cardLivroOferecido =
                findViewById(R.id.cardLivroOferecido);

        imgLivroOferecido =
                findViewById(R.id.imgLivroOferecido);

        txtTituloLivroOferecido =
                findViewById(R.id.txtTituloLivroOferecido);

        txtObservacaoLivroOferecido =
                findViewById(R.id.txtObservacaoLivroOferecido);

        cardLivroNaoAnunciado =
                findViewById(R.id.cardLivroNaoAnunciado);

        txtDescricaoLivroNaoAnunciado =
                findViewById(R.id.txtDescricaoLivroNaoAnunciado);

        cardVenda =
                findViewById(R.id.cardVenda);

        imgAnunciante =
                findViewById(R.id.imgAnunciante);

        txtNomeAnunciante =
                findViewById(R.id.txtNomeAnunciante);

        txtConfirmacaoAnunciante =
                findViewById(R.id.txtConfirmacaoAnunciante);

        imgInteressado =
                findViewById(R.id.imgInteressado);

        txtNomeInteressado =
                findViewById(R.id.txtNomeInteressado);

        txtConfirmacaoInteressado =
                findViewById(R.id.txtConfirmacaoInteressado);

        layoutControleNegociacao =
                findViewById(R.id.layoutControleNegociacao);

        txtMinhaConfirmacao =
                findViewById(R.id.txtMinhaConfirmacao);

        btnConfirmarNegociacao =
                findViewById(R.id.btnConfirmarNegociacao);

        btnCancelarNegociacao =
                findViewById(R.id.btnCancelarNegociacao);
    }

    private void inicializarServices() {
        authService = new AuthService(this);

        negociacoesService =
                new NegociacoesService();
    }

    private boolean validarUsuario() {

        FirebaseUser usuarioAtual =
                authService.getUsuarioAtual();

        if (usuarioAtual == null) {

            Toast.makeText(
                    this,
                    "Usuário não autenticado.",
                    Toast.LENGTH_SHORT
            ).show();

            finish();
            return false;
        }

        usuarioLogadoId = usuarioAtual.getUid();

        return true;
    }

    private boolean obterNegociacaoId() {

        negociacaoId =
                getIntent().getStringExtra("negociacaoId");

        if (negociacaoId == null
                || negociacaoId.trim().isEmpty()) {

            Toast.makeText(
                    this,
                    "Negociação não encontrada.",
                    Toast.LENGTH_SHORT
            ).show();

            finish();
            return false;
        }

        return true;
    }

    private void configurarEventos() {

        findViewById(R.id.btnVoltarNegociacao)
                .setOnClickListener(v -> finish());

        btnConfirmarNegociacao.setOnClickListener(
                v -> confirmarNegociacao()
        );

        btnCancelarNegociacao.setOnClickListener(
                v -> confirmarCancelamento()
        );
    }
    private void confirmarNegociacao() {

        if (negociacaoAtual == null
                || negociacaoAtual.getId() == null) {
            return;
        }

        btnConfirmarNegociacao.setEnabled(false);
        btnCancelarNegociacao.setEnabled(false);

        negociacoesService.confirmar(
                negociacaoAtual.getId(),
                usuarioLogadoId,
                new NegociacoesService.Callback<Negociacao>() {

                    @Override
                    public void onSuccess(Negociacao negociacao) {

                        negociacaoAtual = negociacao;

                        Toast.makeText(
                                NegociacaoActivity.this,
                                "Confirmação registrada.",
                                Toast.LENGTH_SHORT
                        ).show();

                        renderizarNegociacao();
                    }

                    @Override
                    public void onError(String erro) {

                        btnConfirmarNegociacao.setEnabled(true);
                        btnCancelarNegociacao.setEnabled(true);

                        Toast.makeText(
                                NegociacaoActivity.this,
                                erro,
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }
        );
    }

    private void confirmarCancelamento() {

        if (negociacaoAtual == null) {
            return;
        }

        new MaterialAlertDialogBuilder(this)
                .setTitle("Cancelar negociação")
                .setMessage(
                        "Tem certeza de que deseja cancelar esta negociação? " +
                                "Depois disso, ela não poderá mais ser continuada."
                )
                .setNegativeButton(
                        "Voltar",
                        null
                )
                .setPositiveButton(
                        "Cancelar negociação",
                        (dialog, which) ->
                                cancelarNegociacao()
                )
                .show();
    }

    private void cancelarNegociacao() {

        if (negociacaoAtual == null
                || negociacaoAtual.getId() == null) {
            return;
        }

        btnConfirmarNegociacao.setEnabled(false);
        btnCancelarNegociacao.setEnabled(false);

        negociacoesService.cancelar(
                negociacaoAtual.getId(),
                usuarioLogadoId,
                new NegociacoesService.Callback<Negociacao>() {

                    @Override
                    public void onSuccess(Negociacao negociacao) {

                        negociacaoAtual = negociacao;

                        Toast.makeText(
                                NegociacaoActivity.this,
                                "Negociação cancelada.",
                                Toast.LENGTH_SHORT
                        ).show();

                        renderizarNegociacao();
                    }

                    @Override
                    public void onError(String erro) {

                        btnConfirmarNegociacao.setEnabled(true);
                        btnCancelarNegociacao.setEnabled(true);

                        Toast.makeText(
                                NegociacaoActivity.this,
                                erro,
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }
        );
    }

    private void carregarNegociacao() {

        negociacoesService.buscarPorUidParaUsuario(
                negociacaoId,
                usuarioLogadoId,
                new NegociacoesService.Callback<Negociacao>() {

                    @Override
                    public void onSuccess(Negociacao negociacao) {

                        negociacaoAtual = negociacao;

                        renderizarNegociacao();
                    }

                    @Override
                    public void onError(String erro) {

                        Toast.makeText(
                                NegociacaoActivity.this,
                                erro,
                                Toast.LENGTH_LONG
                        ).show();

                        finish();
                    }
                }
        );
    }

    private void renderizarNegociacao() {

        if (negociacaoAtual == null) {
            return;
        }

        renderizarStatus();
        renderizarLivroNegociado();
        renderizarTipoNegociacao();
        renderizarOferta();
        renderizarParticipantes();
        renderizarControles();
    }

    // =========================================================
    // STATUS
    // =========================================================

    private void renderizarStatus() {

        Negociacao.StatusNegociacao status =
                negociacaoAtual.getStatus();

        if (status == null) {
            txtStatusNegociacao.setText("");
            return;
        }

        switch (status) {

            case EM_ANDAMENTO:
                txtStatusNegociacao.setText("EM ANDAMENTO");
                break;

            case FINALIZADA:
                txtStatusNegociacao.setText("FINALIZADA");
                break;

            case CANCELADA:
                txtStatusNegociacao.setText("CANCELADA");
                break;

            case RECUSADA:
                txtStatusNegociacao.setText("RECUSADA");
                break;

            case PENDENTE:
                txtStatusNegociacao.setText("PENDENTE");
                break;
        }
    }

    // =========================================================
    // LIVRO PRINCIPAL
    // =========================================================

    private void renderizarLivroNegociado() {

        txtTituloLivroNegociado.setText(
                negociacaoAtual.getTitulo()
        );

        carregarImagem(
                negociacaoAtual.getCapa(),
                imgLivroNegociado
        );
    }

    // =========================================================
    // TIPO
    // =========================================================

    private void renderizarTipoNegociacao() {

        Negociacao.TipoNegociacao tipo =
                negociacaoAtual.getTipoNegociacao();

        if (tipo == Negociacao.TipoNegociacao.TROCA) {

            txtIconeTipoNegociacao.setText("⇄");
            txtTipoNegociacao.setText("Troca");

        } else if (tipo == Negociacao.TipoNegociacao.VENDA) {

            txtIconeTipoNegociacao.setText("→");
            txtTipoNegociacao.setText("Venda");
        }
    }

    // =========================================================
    // OFERTA
    // =========================================================

    private void renderizarOferta() {

        cardLivroOferecido.setVisibility(View.GONE);
        cardLivroNaoAnunciado.setVisibility(View.GONE);
        cardVenda.setVisibility(View.GONE);

        if (negociacaoAtual.getTipoNegociacao()
                == Negociacao.TipoNegociacao.VENDA) {

            cardVenda.setVisibility(View.VISIBLE);
            return;
        }

        if (negociacaoAtual.getTipoNegociacao()
                != Negociacao.TipoNegociacao.TROCA) {
            return;
        }

        String anuncioOferecidoId =
                negociacaoAtual.getAnuncioOferecidoId();

        if (anuncioOferecidoId != null
                && !anuncioOferecidoId.trim().isEmpty()) {

            renderizarLivroOferecido();

        } else {

            renderizarLivroNaoAnunciado();
        }
    }

    private void renderizarLivroOferecido() {

        cardLivroOferecido.setVisibility(View.VISIBLE);

        txtTituloLivroOferecido.setText(
                negociacaoAtual.getTituloLivroOferecido()
        );

        carregarImagem(
                negociacaoAtual.getCapaLivroOferecido(),
                imgLivroOferecido
        );

        String observacao =
                negociacaoAtual.getDescricaoOferta();

        if (observacao == null
                || observacao.trim().isEmpty()) {

            txtObservacaoLivroOferecido.setVisibility(View.GONE);

        } else {

            txtObservacaoLivroOferecido.setText(observacao);
            txtObservacaoLivroOferecido.setVisibility(View.VISIBLE);
        }
    }

    private void renderizarLivroNaoAnunciado() {

        cardLivroNaoAnunciado.setVisibility(View.VISIBLE);

        String descricao =
                negociacaoAtual.getDescricaoOferta();

        if (descricao == null
                || descricao.trim().isEmpty()) {

            descricao = "Livro não informado";
        }

        txtDescricaoLivroNaoAnunciado.setText(descricao);
    }

    // =========================================================
    // PARTICIPANTES
    // =========================================================

    private void renderizarParticipantes() {

        txtNomeAnunciante.setText(
                negociacaoAtual.getNmAnunciante()
        );

        txtNomeInteressado.setText(
                negociacaoAtual.getNmComprador()
        );

        carregarImagem(
                negociacaoAtual.getFotoPerfilAnunciante(),
                imgAnunciante
        );

        carregarImagem(
                negociacaoAtual.getFotoPerfilComprador(),
                imgInteressado
        );

        renderizarConfirmacaoParticipante(
                txtConfirmacaoAnunciante,
                negociacaoAtual.isConfirmacaoAnunciante()
        );

        renderizarConfirmacaoParticipante(
                txtConfirmacaoInteressado,
                negociacaoAtual.isConfirmacaoComprador()
        );
    }

    private void renderizarConfirmacaoParticipante(
            TextView textView,
            boolean confirmado
    ) {

        if (confirmado) {
            textView.setText("✓ Confirmado");
        } else {
            textView.setText("○ Aguardando confirmação");
        }
    }

    // =========================================================
    // CONTROLES
    // =========================================================

    private void renderizarControles() {

        if (negociacaoAtual.getStatus()
                != Negociacao.StatusNegociacao.EM_ANDAMENTO) {

            layoutControleNegociacao.setVisibility(View.GONE);
            return;
        }

        layoutControleNegociacao.setVisibility(View.VISIBLE);

        boolean usuarioEhAnunciante =
                usuarioLogadoId.equals(
                        negociacaoAtual.getUsuarioAnuncianteId()
                );

        boolean usuarioJaConfirmou =
                usuarioEhAnunciante
                        ? negociacaoAtual.isConfirmacaoAnunciante()
                        : negociacaoAtual.isConfirmacaoComprador();

        if (usuarioJaConfirmou) {

            btnConfirmarNegociacao.setVisibility(View.GONE);
            txtMinhaConfirmacao.setVisibility(View.VISIBLE);

        } else {

            btnConfirmarNegociacao.setVisibility(View.VISIBLE);
            txtMinhaConfirmacao.setVisibility(View.GONE);

            configurarTextoConfirmacao(
                    usuarioEhAnunciante
            );
        }

        btnCancelarNegociacao.setEnabled(true);
    }

    private void configurarTextoConfirmacao(
            boolean usuarioEhAnunciante
    ) {

        if (negociacaoAtual.getTipoNegociacao()
                == Negociacao.TipoNegociacao.TROCA) {

            btnConfirmarNegociacao.setText(
                    "Confirmar recebimento do livro"
            );

            return;
        }

        if (negociacaoAtual.getTipoNegociacao()
                == Negociacao.TipoNegociacao.VENDA) {

            if (usuarioEhAnunciante) {

                btnConfirmarNegociacao.setText(
                        "Confirmar recebimento do pagamento"
                );

            } else {

                btnConfirmarNegociacao.setText(
                        "Confirmar recebimento do livro"
                );
            }
        }
    }

    // =========================================================
    // IMAGENS
    // =========================================================

    private void carregarImagem(
            String url,
            ImageView imageView
    ) {

        Glide.with(this)
                .load(url)
                .placeholder(R.drawable.placeholder_capa)
                .error(R.drawable.placeholder_capa)
                .into(imageView);
    }
}