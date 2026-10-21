package br.gov.sp.cps.trocabook.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.content.DialogInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import android.os.Handler;
import android.os.Looper;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.app.AlertDialog;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import br.gov.sp.cps.trocabook.R;
import br.gov.sp.cps.trocabook.model.Anuncio;
import br.gov.sp.cps.trocabook.model.Negociacao;
import br.gov.sp.cps.trocabook.model.Usuario;
import br.gov.sp.cps.trocabook.model.dto.MensagemDTO;
import br.gov.sp.cps.trocabook.model.dto.AtualizarMensagemDTO;
import br.gov.sp.cps.trocabook.service.AnuncioService;
import br.gov.sp.cps.trocabook.service.AuthService;
import br.gov.sp.cps.trocabook.service.NegociacoesService;
import br.gov.sp.cps.trocabook.service.UsuarioService;
import br.gov.sp.cps.trocabook.service.chat.ChatService;
import br.gov.sp.cps.trocabook.ui.adapters.AnuncioTrocaAdapter;
import br.gov.sp.cps.trocabook.ui.adapters.ChatAdapter;

public class ChatActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigation;

    private RecyclerView rvMensagens;

    private EditText editMensagem;

    private final Handler handler = new Handler(Looper.getMainLooper());

    private final Runnable atualizarChatRunnable =
            new Runnable() {

                @Override
                public void run() {

                    carregarMensagens();

                    atualizarNegociacao();

                    handler.postDelayed(
                            this,
                            4000
                    );
                }
            };

    private FloatingActionButton btnEnviarMensagem;

    private ChatService chatService;

    private AuthService authService;

    private UsuarioService usuarioService;

    private AnuncioService anuncioService;

    private NegociacoesService negociacoesService;

    private ChatAdapter adapter;

    private List<MensagemDTO> listaMensagens;

    private String anuncioId;

    private String userDestinatario;

    private String userRemetente;

    private Anuncio anuncioAtual;

    private Negociacao negociacaoAtual;

    private boolean usuarioEhAnunciante;

    private ImageView imgUsuarioChat;

    private TextView txtNomeUsuarioChat;

    private ImageView imgCapaAnuncioChat;

    private TextView txtTituloAnuncioChat;

    private TextView txtTipoAnuncioChat;

    private TextView txtStatusNegociacaoChat;

    private LinearLayout layoutAcoesNegociacao;

    private LinearLayout layoutDetalhesProposta;

    private LinearLayout layoutLivroOferecido;

    private TextView txtTipoPropostaRecebida;

    private ImageView imgCapaLivroOferecidoChat;

    private TextView txtTituloLivroOferecidoChat;

    private TextView txtDescricaoOfertaChat;

    private MaterialButton btnAcaoNegociacaoSecundaria;

    private MaterialButton btnAcaoNegociacaoPrincipal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);

        setContentView(R.layout.activity_chat);

        inicializarComponentes();

        FirebaseUser user = authService.getUsuarioAtual();

        if (user == null) {
            finish();
            return;
        }

        receberDadosIntent();

        if (anuncioId == null
                || anuncioId.trim().isEmpty()
                || userDestinatario == null
                || userDestinatario.trim().isEmpty()) {

            finish();
            return;
        }

        configurarMenu();

        carregarUsuarioChat();

        carregarAnuncioENegociacao();

        configurarRecycler();

        carregarMensagens();

        configurarEnvioMensagem();
    }

    private void inicializarComponentes() {

        bottomNavigation =
                findViewById(R.id.bottomNavigation);

        rvMensagens =
                findViewById(R.id.rvMensagens);

        imgUsuarioChat =
                findViewById(R.id.imgUsuarioChat);

        txtNomeUsuarioChat =
                findViewById(R.id.txtNomeUsuarioChat);

        editMensagem =
                findViewById(R.id.editMensagem);

        btnEnviarMensagem =
                findViewById(R.id.btnEnviarMensagem);

        imgCapaAnuncioChat =
                findViewById(R.id.imgCapaAnuncioChat);

        txtTituloAnuncioChat =
                findViewById(R.id.txtTituloAnuncioChat);

        txtTipoAnuncioChat =
                findViewById(R.id.txtTipoAnuncioChat);

        txtStatusNegociacaoChat =
                findViewById(R.id.txtStatusNegociacaoChat);

        layoutAcoesNegociacao =
                findViewById(R.id.layoutAcoesNegociacao);

        btnAcaoNegociacaoSecundaria =
                findViewById(R.id.btnAcaoNegociacaoSecundaria);

        btnAcaoNegociacaoPrincipal =
                findViewById(R.id.btnAcaoNegociacaoPrincipal);

        layoutDetalhesProposta =
                findViewById(
                        R.id.layoutDetalhesProposta
                );

        layoutLivroOferecido =
                findViewById(
                        R.id.layoutLivroOferecido
                );

        txtTipoPropostaRecebida =
                findViewById(
                        R.id.txtTipoPropostaRecebida
                );

        imgCapaLivroOferecidoChat =
                findViewById(
                        R.id.imgCapaLivroOferecidoChat
                );

        txtTituloLivroOferecidoChat =
                findViewById(
                        R.id.txtTituloLivroOferecidoChat
                );

        txtDescricaoOfertaChat =
                findViewById(
                        R.id.txtDescricaoOfertaChat
                );

        chatService =
                new ChatService();

        authService =
                new AuthService(this);

        usuarioService =
                new UsuarioService();

        anuncioService =
                new AnuncioService();

        negociacoesService =
                new NegociacoesService();

        listaMensagens =
                new ArrayList<>();
    }

    private void esconderDetalhesProposta() {

        layoutDetalhesProposta.setVisibility(
                View.GONE
        );

        layoutLivroOferecido.setVisibility(
                View.GONE
        );

        txtDescricaoOfertaChat.setVisibility(
                View.GONE
        );

        txtTipoPropostaRecebida.setText("");

        txtTituloLivroOferecidoChat.setText("");

        txtDescricaoOfertaChat.setText("");

        imgCapaLivroOferecidoChat.setImageDrawable(
                null
        );
    }

    private void carregarAnuncioENegociacao() {

        anuncioService.buscarAnuncioPorId(
                anuncioId,
                new AnuncioService.Callback<Anuncio>() {

                    @Override
                    public void onSuccess(
                            Anuncio anuncio
                    ) {

                        if (anuncio == null) {

                            Toast.makeText(
                                    ChatActivity.this,
                                    "Anúncio não encontrado ou finalizado",
                                    Toast.LENGTH_SHORT
                            ).show();

                            return;
                        }

                        anuncioAtual = anuncio;

                        preencherDadosAnuncio();

                        identificarParticipantesEBuscarNegociacao();
                    }

                    @Override
                    public void onError(
                            String mensagem
                    ) {

                        Toast.makeText(
                                ChatActivity.this,
                                mensagem,
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                }
        );
    }

    private void preencherDadosAnuncio() {

        if (anuncioAtual == null) {
            return;
        }

        txtTituloAnuncioChat.setText(
                anuncioAtual.getTitulo()
        );

        txtTipoAnuncioChat.setText(
                obterTextoTipoAnuncio(
                        anuncioAtual.getTipoNegociacao()
                )
        );

        Glide.with(this)
                .load(anuncioAtual.getCapa())
                .placeholder(R.drawable.placeholder_capa)
                .into(imgCapaAnuncioChat);
    }

    private String obterTextoTipoAnuncio(
            Anuncio.TipoNegociacao tipo
    ) {

        if (tipo == null) {
            return "";
        }

        switch (tipo) {

            case TROCA:
                return "Troca";

            case VENDA:
                return "Venda";

            case AMBOS:
                return "Troca ou venda";

            default:
                return "";
        }
    }

    private void identificarParticipantesEBuscarNegociacao() {

        if (anuncioAtual == null
                || userRemetente == null) {
            return;
        }

        usuarioEhAnunciante =
                userRemetente.equals(
                        anuncioAtual.getUidUsuario()
                );

        String uidComprador;

        if (usuarioEhAnunciante) {

            uidComprador =
                    userDestinatario;

        } else {

            uidComprador =
                    userRemetente;
        }

        buscarNegociacaoAtual(
                uidComprador
        );
    }

    private void buscarNegociacaoAtual(
            String uidComprador
    ) {

        negociacoesService.buscarPorAnuncioEComprador(
                anuncioId,
                uidComprador,
                new NegociacoesService.Callback<Negociacao>() {

                    @Override
                    public void onSuccess(
                            Negociacao negociacao
                    ) {

                        negociacaoAtual =
                                negociacao;

                        renderizarEstadoNegociacao();
                    }

                    @Override
                    public void onError(
                            String erro
                    ) {

                        negociacaoAtual = null;

                        renderizarEstadoNegociacao();
                    }
                }
        );
    }

    private void atualizarNegociacao() {

        if (anuncioAtual == null
                || userRemetente == null
                || userDestinatario == null) {
            return;
        }

        String uidComprador =
                usuarioEhAnunciante
                        ? userDestinatario
                        : userRemetente;

        buscarNegociacaoAtual(
                uidComprador
        );
    }

    private void esconderAcoesNegociacao() {

        layoutAcoesNegociacao.setVisibility(
                View.GONE
        );

        btnAcaoNegociacaoSecundaria.setVisibility(
                View.GONE
        );

        btnAcaoNegociacaoPrincipal.setVisibility(
                View.GONE
        );

        btnAcaoNegociacaoSecundaria.setEnabled(true);
        btnAcaoNegociacaoPrincipal.setEnabled(true);

        btnAcaoNegociacaoSecundaria.setOnClickListener(
                null
        );

        btnAcaoNegociacaoPrincipal.setOnClickListener(
                null
        );
    }

    private void mostrarDetalhesPropostaRecebida() {

        if (negociacaoAtual == null) {
            return;
        }

        layoutDetalhesProposta.setVisibility(
                View.VISIBLE
        );

        Negociacao.TipoNegociacao tipo =
                negociacaoAtual.getTipoNegociacao();

        if (tipo == Negociacao.TipoNegociacao.VENDA) {

            txtTipoPropostaRecebida.setText(
                    "Proposta de compra"
            );

            return;
        }

        if (tipo != Negociacao.TipoNegociacao.TROCA) {
            return;
        }

        txtTipoPropostaRecebida.setText(
                "Livro oferecido"
        );

        String anuncioOferecidoId =
                negociacaoAtual.getAnuncioOferecidoId();

        String titulo =
                negociacaoAtual.getTituloLivroOferecido();

        String capa =
                negociacaoAtual.getCapaLivroOferecido();

        String descricao =
                negociacaoAtual.getDescricaoOferta();

        // TROCA COM LIVRO ANUNCIADO
        if (anuncioOferecidoId != null
                && !anuncioOferecidoId.trim().isEmpty()) {

            layoutLivroOferecido.setVisibility(
                    View.VISIBLE
            );

            txtTituloLivroOferecidoChat.setText(
                    titulo != null && !titulo.trim().isEmpty()
                            ? titulo
                            : "Livro oferecido"
            );

            Glide.with(this)
                    .load(capa)
                    .placeholder(R.drawable.placeholder_capa)
                    .into(imgCapaLivroOferecidoChat);

            if (descricao != null
                    && !descricao.trim().isEmpty()) {

                txtDescricaoOfertaChat.setText(
                        "Observação: " + descricao
                );

                txtDescricaoOfertaChat.setVisibility(
                        View.VISIBLE
                );
            }

            return;
        }

        // TROCA COM LIVRO NÃO ANUNCIADO
        if (descricao != null
                && !descricao.trim().isEmpty()) {

            txtDescricaoOfertaChat.setText(
                    descricao
            );

            txtDescricaoOfertaChat.setVisibility(
                    View.VISIBLE
            );
        }
    }

    private void renderizarEstadoNegociacao() {

        esconderAcoesNegociacao();
        esconderDetalhesProposta();

        txtStatusNegociacaoChat.setVisibility(
                View.GONE
        );

        if (anuncioAtual == null) {
            return;
        }

        if (negociacaoAtual == null) {

            renderizarSemNegociacao();
            return;
        }

        Negociacao.StatusNegociacao status =
                negociacaoAtual.getStatus();

        if (status == null) {

            renderizarSemNegociacao();
            return;
        }

        switch (status) {

            case PENDENTE:
                renderizarNegociacaoPendente();
                break;

            case EM_ANDAMENTO:
                renderizarNegociacaoEmAndamento();
                break;

            case RECUSADA:
            case CANCELADA:
                renderizarNegociacaoEncerrada();
                break;

            case FINALIZADA:
                renderizarNegociacaoFinalizada();
                break;
        }
    }

    private void renderizarSemNegociacao() {

        if (usuarioEhAnunciante) {

            txtStatusNegociacaoChat.setText(
                    "Aguardando proposta"
            );

            txtStatusNegociacaoChat.setVisibility(
                    View.VISIBLE
            );

            return;
        }

        mostrarAcoesNovaProposta();
    }

    private void mostrarAcoesNovaProposta() {

        if (anuncioAtual == null) {
            return;
        }

        layoutAcoesNegociacao.setVisibility(View.VISIBLE);

        Anuncio.TipoNegociacao tipo =
                anuncioAtual.getTipoNegociacao();

        if (tipo == Anuncio.TipoNegociacao.TROCA) {

            btnAcaoNegociacaoPrincipal.setText(
                    "Propor troca"
            );

            btnAcaoNegociacaoPrincipal.setVisibility(
                    View.VISIBLE
            );

            btnAcaoNegociacaoPrincipal.setOnClickListener(
                    v -> iniciarPropostaTroca()
            );

        } else if (tipo == Anuncio.TipoNegociacao.VENDA) {

            btnAcaoNegociacaoPrincipal.setText(
                    "Comprar"
            );

            btnAcaoNegociacaoPrincipal.setVisibility(
                    View.VISIBLE
            );

            btnAcaoNegociacaoPrincipal.setOnClickListener(
                    v -> confirmarPropostaCompra()
            );

        } else if (tipo == Anuncio.TipoNegociacao.AMBOS) {

            btnAcaoNegociacaoSecundaria.setText(
                    "Propor troca"
            );

            btnAcaoNegociacaoSecundaria.setVisibility(
                    View.VISIBLE
            );

            btnAcaoNegociacaoSecundaria.setOnClickListener(
                    v -> iniciarPropostaTroca()
            );

            btnAcaoNegociacaoPrincipal.setText(
                    "Comprar"
            );

            btnAcaoNegociacaoPrincipal.setVisibility(
                    View.VISIBLE
            );

            btnAcaoNegociacaoPrincipal.setOnClickListener(
                    v -> confirmarPropostaCompra()
            );
        }
    }

    private void iniciarPropostaTroca() {

        if (anuncioAtual == null
                || userRemetente == null) {
            return;
        }

        View view =
                LayoutInflater.from(this)
                        .inflate(
                                R.layout.dialog_tipo_proposta_troca,
                                null
                        );

        View cardLivroAnunciado =
                view.findViewById(
                        R.id.cardLivroAnunciado
                );

        View cardLivroNaoAnunciado =
                view.findViewById(
                        R.id.cardLivroNaoAnunciado
                );

        AlertDialog dialog =
                new MaterialAlertDialogBuilder(this)
                        .setTitle("Propor troca")
                        .setView(view)
                        .setNegativeButton(
                                "Cancelar",
                                null
                        )
                        .create();

        cardLivroAnunciado.setOnClickListener(v -> {
            dialog.dismiss();
            carregarLivrosParaTroca();
        });

        cardLivroNaoAnunciado.setOnClickListener(v -> {
            dialog.dismiss();
            abrirDialogLivroNaoAnunciado();
        });

        dialog.show();
    }

    private void carregarLivrosParaTroca() {

        anuncioService.buscarAnunciosUsuario(
                userRemetente,
                new AnuncioService.Callback<List<Anuncio>>() {

                    @Override
                    public void onSuccess(
                            List<Anuncio> anuncios
                    ) {

                        List<Anuncio> anunciosDisponiveis =
                                new ArrayList<>();

                        if (anuncios != null) {

                            for (Anuncio anuncio : anuncios) {

                                if (anuncio == null
                                        || anuncio.getId() == null
                                        || anuncio.getId().equals(anuncioId)) {
                                    continue;
                                }

                                Anuncio.TipoNegociacao tipo =
                                        anuncio.getTipoNegociacao();

                                if (tipo == Anuncio.TipoNegociacao.TROCA
                                        || tipo == Anuncio.TipoNegociacao.AMBOS) {

                                    anunciosDisponiveis.add(
                                            anuncio
                                    );
                                }
                            }
                        }

                        if (anunciosDisponiveis.isEmpty()) {

                            new MaterialAlertDialogBuilder(
                                    ChatActivity.this
                            )
                                    .setTitle("Nenhum anúncio disponível")
                                    .setMessage(
                                            "Você não possui outro livro anunciado disponível para troca. Deseja informar um livro não anunciado?"
                                    )
                                    .setNegativeButton(
                                            "Cancelar",
                                            null
                                    )
                                    .setPositiveButton(
                                            "Informar livro",
                                            (dialog, which) ->
                                                    abrirDialogLivroNaoAnunciado()
                                    )
                                    .show();

                            return;
                        }

                        abrirSelecaoLivroAnunciado(
                                anunciosDisponiveis
                        );
                    }

                    @Override
                    public void onError(
                            String mensagem
                    ) {

                        Toast.makeText(
                                ChatActivity.this,
                                mensagem,
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                }
        );
    }

    private void abrirSelecaoLivroAnunciado(
            List<Anuncio> anuncios
    ) {

        View view =
                LayoutInflater
                        .from(this)
                        .inflate(
                                R.layout.dialog_selecionar_livro_troca,
                                null
                        );

        RecyclerView rvLivrosTroca =
                view.findViewById(
                        R.id.rvLivrosTroca
                );

        AlertDialog dialog =
                new MaterialAlertDialogBuilder(this)
                        .setTitle("Escolha o livro")
                        .setView(view)
                        .setNegativeButton(
                                "Cancelar",
                                null
                        )
                        .create();

        AnuncioTrocaAdapter adapterTroca =
                new AnuncioTrocaAdapter(
                        anuncios,
                        anuncio -> {

                            dialog.dismiss();

                            abrirDialogObservacaoTroca(
                                    anuncio
                            );
                        }
                );

        rvLivrosTroca.setLayoutManager(
                new LinearLayoutManager(this)
        );

        rvLivrosTroca.setAdapter(
                adapterTroca
        );

        dialog.show();
    }

    private void abrirDialogObservacaoTroca(
            Anuncio anuncioOferecido
    ) {

        if (anuncioOferecido == null) {
            return;
        }

        View view =
                LayoutInflater.from(this)
                        .inflate(
                                R.layout.dialog_observacao_troca,
                                null
                        );

        ImageView imgCapa =
                view.findViewById(
                        R.id.imgCapaLivroOferecido
                );

        TextView txtTitulo =
                view.findViewById(
                        R.id.txtTituloLivroOferecido
                );

        EditText input =
                view.findViewById(
                        R.id.inputObservacaoTroca
                );

        txtTitulo.setText(
                anuncioOferecido.getTitulo()
        );

        Glide.with(this)
                .load(anuncioOferecido.getCapa())
                .placeholder(R.drawable.placeholder_capa)
                .into(imgCapa);

        AlertDialog dialog =
                new MaterialAlertDialogBuilder(this)
                        .setTitle("Enviar proposta")
                        .setView(view)
                        .setNegativeButton(
                                "Cancelar",
                                null
                        )
                        .setPositiveButton(
                                "Enviar proposta",
                                null
                        )
                        .create();

        dialog.setOnShowListener(dialogInterface -> {

            dialog.getButton(
                    DialogInterface.BUTTON_POSITIVE
            ).setOnClickListener(v -> {

                String descricao =
                        input.getText()
                                .toString()
                                .trim();

                if (descricao.length() > 500) {

                    input.setError(
                            "A observação deve ter no máximo 500 caracteres"
                    );

                    return;
                }

                dialog.dismiss();

                criarPropostaTroca(
                        anuncioOferecido.getId(),
                        descricao.isEmpty()
                                ? null
                                : descricao
                );
            });
        });

        dialog.show();
    }

    private void abrirDialogLivroNaoAnunciado() {

        EditText input =
                new EditText(this);

        input.setHint(
                "Descreva o livro que deseja oferecer"
        );

        input.setSingleLine(false);
        input.setMaxLines(5);

        int padding =
                (int) (
                        24
                                * getResources()
                                .getDisplayMetrics()
                                .density
                );

        input.setPadding(
                padding,
                padding / 2,
                padding,
                padding / 2
        );

        AlertDialog dialog =
                new MaterialAlertDialogBuilder(this)
                        .setTitle("Livro não anunciado")
                        .setMessage(
                                "Informe qual livro você deseja oferecer na troca."
                        )
                        .setView(input)
                        .setNegativeButton(
                                "Cancelar",
                                null
                        )
                        .setPositiveButton(
                                "Enviar proposta",
                                null
                        )
                        .create();

        dialog.setOnShowListener(dialogInterface -> {

            dialog.getButton(
                    DialogInterface.BUTTON_POSITIVE
            ).setOnClickListener(v -> {

                String descricao =
                        input.getText()
                                .toString()
                                .trim();

                if (descricao.isEmpty()) {

                    input.setError(
                            "Descreva o livro oferecido"
                    );

                    return;
                }

                if (descricao.length() > 500) {

                    input.setError(
                            "A descrição deve ter no máximo 500 caracteres"
                    );

                    return;
                }

                dialog.dismiss();

                criarPropostaTroca(
                        null,
                        descricao
                );
            });
        });

        dialog.show();
    }

    private void criarPropostaTroca(
            String anuncioOferecidoId,
            String descricaoOferta
    ) {

        if (anuncioAtual == null
                || userRemetente == null
                || anuncioId == null) {
            return;
        }

        btnAcaoNegociacaoPrincipal.setEnabled(false);
        btnAcaoNegociacaoSecundaria.setEnabled(false);

        negociacoesService.criar(
                anuncioId,
                userRemetente,
                Negociacao.TipoNegociacao.TROCA,
                anuncioOferecidoId,
                descricaoOferta,
                new NegociacoesService.Callback<Negociacao>() {

                    @Override
                    public void onSuccess(
                            Negociacao negociacao
                    ) {

                        negociacaoAtual =
                                negociacao;

                        renderizarEstadoNegociacao();

                        enviarMensagemSistema(
                                "📚 Proposta de troca enviada para este livro."
                        );
                    }

                    @Override
                    public void onError(
                            String erro
                    ) {

                        btnAcaoNegociacaoPrincipal.setEnabled(
                                true
                        );

                        btnAcaoNegociacaoSecundaria.setEnabled(
                                true
                        );

                        Toast.makeText(
                                ChatActivity.this,
                                erro,
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                }
        );
    }

    private void confirmarPropostaCompra() {

        if (anuncioAtual == null) {
            return;
        }

        new MaterialAlertDialogBuilder(this)
                .setTitle("Comprar livro")
                .setMessage(
                        "Deseja enviar uma proposta de compra para este livro?"
                )
                .setNegativeButton(
                        "Cancelar",
                        null
                )
                .setPositiveButton(
                        "Enviar proposta",
                        (dialog, which) ->
                                criarPropostaCompra()
                )
                .show();
    }

    private void criarPropostaCompra() {

        if (anuncioAtual == null
                || userRemetente == null
                || anuncioId == null) {
            return;
        }

        btnAcaoNegociacaoPrincipal.setEnabled(false);

        negociacoesService.criar(
                anuncioId,
                userRemetente,
                Negociacao.TipoNegociacao.VENDA,
                null,
                null,
                new NegociacoesService.Callback<Negociacao>() {

                    @Override
                    public void onSuccess(
                            Negociacao negociacao
                    ) {

                        negociacaoAtual =
                                negociacao;

                        renderizarEstadoNegociacao();

                        enviarMensagemPropostaCompra();
                    }

                    @Override
                    public void onError(
                            String erro
                    ) {

                        btnAcaoNegociacaoPrincipal.setEnabled(
                                true
                        );

                        Toast.makeText(
                                ChatActivity.this,
                                erro,
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                }
        );
    }

    private void enviarMensagemSistema(
            String conteudo
    ) {

        if (userRemetente == null
                || userDestinatario == null
                || anuncioId == null) {
            return;
        }

        MensagemDTO mensagem =
                new MensagemDTO();

        mensagem.setUidRemetente(
                userRemetente
        );

        mensagem.setUidDestinatario(
                userDestinatario
        );

        mensagem.setUidAnuncio(
                anuncioId
        );

        mensagem.setConteudo(
                conteudo
        );

        chatService.salvarMensagem(
                mensagem,
                new ChatService.Callback<MensagemDTO>() {

                    @Override
                    public void onSuccess(
                            MensagemDTO dados
                    ) {

                        carregarMensagens();
                    }

                    @Override
                    public void onError(
                            String erro
                    ) {

                        /*
                         * A negociação já foi criada.
                         * Portanto NÃO devemos desfazer a
                         * negociação porque a mensagem falhou.
                         */

                        Toast.makeText(
                                ChatActivity.this,
                                "Proposta criada, mas não foi possível enviar a mensagem automática",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                }
        );
    }

    private void enviarMensagemPropostaCompra() {

        enviarMensagemSistema(
                "📚 Proposta de compra enviada para este livro."
        );
    }

    private void renderizarNegociacaoPendente() {

        txtStatusNegociacaoChat.setVisibility(
                View.VISIBLE
        );

        if (usuarioEhAnunciante) {

            txtStatusNegociacaoChat.setText(
                    "Proposta recebida"
            );

            mostrarDetalhesPropostaRecebida();

            layoutAcoesNegociacao.setVisibility(
                    View.VISIBLE
            );

            btnAcaoNegociacaoSecundaria.setText(
                    "Recusar"
            );

            btnAcaoNegociacaoSecundaria.setVisibility(
                    View.VISIBLE
            );

            btnAcaoNegociacaoSecundaria.setOnClickListener(
                    v -> confirmarRecusaProposta()
            );

            btnAcaoNegociacaoPrincipal.setText(
                    "Aceitar"
            );

            btnAcaoNegociacaoPrincipal.setVisibility(
                    View.VISIBLE
            );

            btnAcaoNegociacaoPrincipal.setOnClickListener(
                    v -> confirmarAceiteProposta()
            );

        } else {

            txtStatusNegociacaoChat.setText(
                    "Proposta enviada • aguardando resposta"
            );
        }
    }

    private void confirmarAceiteProposta() {

        if (negociacaoAtual == null
                || negociacaoAtual.getId() == null) {
            return;
        }

        new MaterialAlertDialogBuilder(this)
                .setTitle("Aceitar proposta")
                .setMessage(
                        "Deseja aceitar esta proposta e iniciar a negociação?"
                )
                .setNegativeButton(
                        "Cancelar",
                        null
                )
                .setPositiveButton(
                        "Aceitar",
                        (dialog, which) ->
                                aceitarProposta()
                )
                .show();
    }

    private void aceitarProposta() {

        if (negociacaoAtual == null
                || negociacaoAtual.getId() == null
                || userRemetente == null) {
            return;
        }

        btnAcaoNegociacaoPrincipal.setEnabled(false);
        btnAcaoNegociacaoSecundaria.setEnabled(false);

        negociacoesService.aceitar(
                negociacaoAtual.getId(),
                userRemetente,
                new NegociacoesService.Callback<Negociacao>() {

                    @Override
                    public void onSuccess(
                            Negociacao negociacao
                    ) {

                        negociacaoAtual = negociacao;

                        renderizarEstadoNegociacao();

                        enviarMensagemSistema(
                                "✅ Proposta aceita. A negociação está em andamento."
                        );
                    }

                    @Override
                    public void onError(
                            String erro
                    ) {

                        btnAcaoNegociacaoPrincipal.setEnabled(
                                true
                        );

                        btnAcaoNegociacaoSecundaria.setEnabled(
                                true
                        );

                        Toast.makeText(
                                ChatActivity.this,
                                erro,
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                }
        );
    }

    private void confirmarRecusaProposta() {

        if (negociacaoAtual == null
                || negociacaoAtual.getId() == null) {
            return;
        }

        new MaterialAlertDialogBuilder(this)
                .setTitle("Recusar proposta")
                .setMessage(
                        "Deseja realmente recusar esta proposta?"
                )
                .setNegativeButton(
                        "Cancelar",
                        null
                )
                .setPositiveButton(
                        "Recusar",
                        (dialog, which) ->
                                recusarProposta()
                )
                .show();
    }

    private void recusarProposta() {

        if (negociacaoAtual == null
                || negociacaoAtual.getId() == null
                || userRemetente == null) {
            return;
        }

        btnAcaoNegociacaoPrincipal.setEnabled(false);
        btnAcaoNegociacaoSecundaria.setEnabled(false);

        negociacoesService.recusar(
                negociacaoAtual.getId(),
                userRemetente,
                new NegociacoesService.Callback<Negociacao>() {

                    @Override
                    public void onSuccess(
                            Negociacao negociacao
                    ) {

                        negociacaoAtual = negociacao;

                        renderizarEstadoNegociacao();

                        enviarMensagemSistema(
                                "❌ Proposta recusada pelo anunciante."
                        );
                    }

                    @Override
                    public void onError(
                            String erro
                    ) {

                        btnAcaoNegociacaoPrincipal.setEnabled(
                                true
                        );

                        btnAcaoNegociacaoSecundaria.setEnabled(
                                true
                        );

                        Toast.makeText(
                                ChatActivity.this,
                                erro,
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                }
        );
    }

    private void renderizarNegociacaoEmAndamento() {

        txtStatusNegociacaoChat.setText(
                "Negociação em andamento"
        );

        txtStatusNegociacaoChat.setVisibility(
                View.VISIBLE
        );

        layoutAcoesNegociacao.setVisibility(
                View.VISIBLE
        );

        btnAcaoNegociacaoPrincipal.setText(
                "Ver negociação"
        );

        btnAcaoNegociacaoPrincipal.setOnClickListener(
                v -> abrirNegociacao()
        );

        btnAcaoNegociacaoPrincipal.setVisibility(
                View.VISIBLE
        );
    }

    private void renderizarNegociacaoEncerrada() {

        txtStatusNegociacaoChat.setVisibility(
                View.VISIBLE
        );

        if (usuarioEhAnunciante) {

            txtStatusNegociacaoChat.setText(
                    "Negociação encerrada"
            );

        } else {

            txtStatusNegociacaoChat.setText(
                    "Proposta encerrada"
            );

            mostrarAcoesNovaProposta();
        }
    }

    private void renderizarNegociacaoFinalizada() {

        txtStatusNegociacaoChat.setText(
                "Negociação finalizada"
        );

        txtStatusNegociacaoChat.setVisibility(
                View.VISIBLE
        );

        layoutAcoesNegociacao.setVisibility(
                View.VISIBLE
        );

        btnAcaoNegociacaoPrincipal.setText(
                "Ver detalhes"
        );

        btnAcaoNegociacaoPrincipal.setOnClickListener(
                v -> abrirNegociacao()
        );

        btnAcaoNegociacaoPrincipal.setVisibility(
                View.VISIBLE
        );
    }

    private void abrirNegociacao() {

        if (negociacaoAtual == null) {
            Toast.makeText(
                    this,
                    "Negociação não encontrada.",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }


        String negociacaoId =
                negociacaoAtual.getId();

        if (negociacaoId == null
                || negociacaoId.trim().isEmpty()) {

            Toast.makeText(
                    this,
                    "Não foi possível identificar a negociação.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        Intent intent =
                new Intent(
                        ChatActivity.this,
                        NegociacaoActivity.class
                );

        intent.putExtra(
                "negociacaoId",
                negociacaoId
        );

        startActivity(intent);
    }



    private void receberDadosIntent() {

        anuncioId =
                getIntent().getStringExtra("anuncioId");

        userDestinatario =
                getIntent().getStringExtra(
                        "userDestinatario"
                );

        FirebaseUser user =
                authService.getUsuarioAtual();

        if (user != null) {
            userRemetente =
                    user.getUid();
        }
    }

    private void carregarUsuarioChat() {

        usuarioService.buscarUsuarioPorId(
                userDestinatario,
                new UsuarioService.ServiceCallback<Usuario>() {

                    @Override
                    public void onSucesso(
                            Usuario usuario
                    ) {

                        if (usuario == null) {

                            txtNomeUsuarioChat.setText(
                                    "Usuário"
                            );

                            return;
                        }

                        txtNomeUsuarioChat.setText(
                                usuario.getEmailPrincipal()
                        );

                        Glide.with(
                                        ChatActivity.this
                                )
                                .load(usuario.getFotoPerfil())
                                .placeholder(
                                        R.drawable.placeholder_usuario
                                )
                                .into(imgUsuarioChat);
                    }

                    @Override
                    public void onErro(
                            String erro
                    ) {

                        txtNomeUsuarioChat.setText(
                                "Usuário"
                        );
                    }
                }
        );
    }

    private void configurarRecycler() {

        adapter = new ChatAdapter(
                listaMensagens,
                userRemetente,
                this::abrirOpcoesMensagem
        );

        rvMensagens.setLayoutManager(
                new LinearLayoutManager(this)
        );

        rvMensagens.setAdapter(adapter);
    }

    private void abrirOpcoesMensagem(
            MensagemDTO mensagem
    ) {

        if (mensagem == null
                || mensagem.getId() == null
                || mensagem.getId().trim().isEmpty()) {

            return;
        }

        if (!userRemetente.equals(
                mensagem.getUidRemetente()
        )) {

            return;
        }

        String[] opcoes = {
                "Editar",
                "Excluir"
        };

        new MaterialAlertDialogBuilder(this)
                .setTitle("Mensagem")
                .setItems(
                        opcoes,
                        (dialog, opcao) -> {

                            if (opcao == 0) {

                                validarMensagemParaEditar(
                                        mensagem.getId()
                                );

                            } else if (opcao == 1) {

                                validarMensagemParaExcluir(
                                        mensagem.getId()
                                );
                            }
                        }
                )
                .show();
    }

    private void validarMensagemParaEditar(
            String mensagemId
    ) {

        chatService.buscarMensagemPorId(
                mensagemId,
                new ChatService.Callback<MensagemDTO>() {

                    @Override
                    public void onSuccess(
                            MensagemDTO mensagem
                    ) {

                        if (mensagem == null) {

                            Toast.makeText(
                                    ChatActivity.this,
                                    "Mensagem não encontrada",
                                    Toast.LENGTH_SHORT
                            ).show();

                            return;
                        }

                        if (!userRemetente.equals(
                                mensagem.getUidRemetente()
                        )) {

                            Toast.makeText(
                                    ChatActivity.this,
                                    "Você não pode editar esta mensagem",
                                    Toast.LENGTH_SHORT
                            ).show();

                            return;
                        }

                        abrirDialogEditarMensagem(
                                mensagem
                        );
                    }

                    @Override
                    public void onError(String erro) {

                        Toast.makeText(
                                ChatActivity.this,
                                erro,
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                }
        );
    }

    private void abrirDialogEditarMensagem(
            MensagemDTO mensagem
    ) {

        EditText input =
                new EditText(this);

        input.setText(
                mensagem.getConteudo()
        );

        input.setSelection(
                input.getText().length()
        );

        input.setSingleLine(false);

        input.setMaxLines(4);

        int padding =
                (int) (
                        24
                                * getResources()
                                .getDisplayMetrics()
                                .density
                );

        input.setPadding(
                padding,
                padding / 2,
                padding,
                padding / 2
        );

        AlertDialog dialog =
                new MaterialAlertDialogBuilder(this)
                        .setTitle("Editar mensagem")
                        .setView(input)
                        .setNegativeButton(
                                "Cancelar",
                                null
                        )
                        .setPositiveButton(
                                "Salvar",
                                null
                        )
                        .create();

        dialog.setOnShowListener(dialogInterface -> {

            dialog.getButton(
                    DialogInterface.BUTTON_POSITIVE
            ).setOnClickListener(v -> {

                String novoConteudo =
                        input.getText()
                                .toString()
                                .trim();

                if (novoConteudo.isEmpty()) {

                    input.setError(
                            "Digite uma mensagem"
                    );

                    return;
                }

                atualizarMensagem(
                        mensagem.getId(),
                        novoConteudo,
                        dialog
                );
            });
        });

        dialog.show();
    }

    private void carregarMensagens() {

        chatService.buscarMensagens(
                userRemetente,
                userDestinatario,
                anuncioId,
                new ChatService.Callback<List<MensagemDTO>>() {

                    @Override
                    public void onSuccess(List<MensagemDTO> dados) {

                        if (dados == null) {
                            return;
                        }

                        // Verifica se houve alguma alteração
                        boolean houveAlteracao =
                                dados.size() != listaMensagens.size();

                        if (!houveAlteracao) {

                            for (int i = 0; i < dados.size(); i++) {

                                MensagemDTO atual = listaMensagens.get(i);
                                MensagemDTO nova = dados.get(i);

                                String idAtual = atual.getId();
                                String idNova = nova.getId();

                                if (idAtual == null || idNova == null) {
                                    houveAlteracao = true;
                                    break;
                                }

                                if (!idAtual.equals(idNova)) {
                                    houveAlteracao = true;
                                    break;
                                }

                                String conteudoAtual =
                                        atual.getConteudo();

                                String conteudoNovo =
                                        nova.getConteudo();

                                if (!Objects.equals(conteudoAtual, conteudoNovo)) {

                                    houveAlteracao = true;
                                    break;
                                }
                            }
                        }

                        // Se nada mudou, não atualiza a tela
                        if (!houveAlteracao) {
                            return;
                        }

                        // Guarda se o usuário estava no final da conversa
                        LinearLayoutManager layoutManager =
                                (LinearLayoutManager) rvMensagens.getLayoutManager();

                        boolean estavaNoFinal = true;

                        if (layoutManager != null && !listaMensagens.isEmpty()) {

                            int ultimaPosicao =
                                    layoutManager.findLastCompletelyVisibleItemPosition();

                            estavaNoFinal =
                                    ultimaPosicao >= listaMensagens.size() - 1;
                        }

                        // Atualiza somente quando realmente houve mudança
                        listaMensagens.clear();
                        listaMensagens.addAll(dados);

                        adapter.notifyDataSetChanged();

                        // Se já estava no final, continua mostrando a última mensagem
                        if (estavaNoFinal && !listaMensagens.isEmpty()) {

                            rvMensagens.scrollToPosition(
                                    listaMensagens.size() - 1
                            );
                        }
                    }

                    @Override
                    public void onError(String erro) {
                        // Erro no carregamento das mensagens.
                    }
                }
        );
    }

    private void atualizarMensagem(
            String mensagemId,
            String novoConteudo,
            DialogInterface dialog
    ) {

        AtualizarMensagemDTO dto =
                new AtualizarMensagemDTO();

        dto.setConteudo(
                novoConteudo
        );

        chatService.atualizarMensagem(
                mensagemId,
                dto,
                new ChatService.Callback<MensagemDTO>() {

                    @Override
                    public void onSuccess(
                            MensagemDTO dados
                    ) {

                        dialog.dismiss();

                        carregarMensagens();
                    }

                    @Override
                    public void onError(
                            String erro
                    ) {

                        Toast.makeText(
                                ChatActivity.this,
                                erro,
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                }
        );
    }

    private void configurarEnvioMensagem() {

        btnEnviarMensagem.setOnClickListener(v -> {

            String conteudo =
                    editMensagem
                            .getText()
                            .toString()
                            .trim();

            if (conteudo.isEmpty()) {
                return;
            }

            if (userRemetente == null
                    || userDestinatario == null
                    || anuncioId == null) {
                return;
            }

            MensagemDTO mensagem =
                    new MensagemDTO();

            mensagem.setUidRemetente(
                    userRemetente
            );

            mensagem.setUidDestinatario(
                    userDestinatario
            );

            mensagem.setUidAnuncio(
                    anuncioId
            );

            mensagem.setConteudo(
                    conteudo
            );

            chatService.salvarMensagem(
                    mensagem,
                    new ChatService.Callback<MensagemDTO>() {

                        @Override
                        public void onSuccess(
                                MensagemDTO dados
                        ) {

                            editMensagem.setText("");

                            carregarMensagens();
                        }

                        @Override
                        public void onError(
                                String erro
                        ) {
                            // Erro ao enviar mensagem.
                        }
                    }
            );
        });
    }

    private void validarMensagemParaExcluir(
            String mensagemId
    ) {

        chatService.buscarMensagemPorId(
                mensagemId,
                new ChatService.Callback<MensagemDTO>() {

                    @Override
                    public void onSuccess(
                            MensagemDTO mensagem
                    ) {

                        if (mensagem == null) {

                            Toast.makeText(
                                    ChatActivity.this,
                                    "Mensagem não encontrada",
                                    Toast.LENGTH_SHORT
                            ).show();

                            return;
                        }

                        if (!userRemetente.equals(
                                mensagem.getUidRemetente()
                        )) {

                            Toast.makeText(
                                    ChatActivity.this,
                                    "Você não pode excluir esta mensagem",
                                    Toast.LENGTH_SHORT
                            ).show();

                            return;
                        }

                        confirmarExclusaoMensagem(
                                mensagem
                        );
                    }

                    @Override
                    public void onError(
                            String erro
                    ) {

                        Toast.makeText(
                                ChatActivity.this,
                                erro,
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                }
        );
    }

    private void confirmarExclusaoMensagem(
            MensagemDTO mensagem
    ) {

        new MaterialAlertDialogBuilder(this)
                .setTitle("Excluir mensagem")
                .setMessage(
                        "Deseja realmente excluir esta mensagem?"
                )
                .setNegativeButton(
                        "Cancelar",
                        null
                )
                .setPositiveButton(
                        "Excluir",
                        (dialog, which) ->
                                excluirMensagem(
                                        mensagem.getId()
                                )
                )
                .show();
    }

    private void excluirMensagem(
            String mensagemId
    ) {

        chatService.deletarMensagem(
                mensagemId,
                new ChatService.Callback<Void>() {

                    @Override
                    public void onSuccess(
                            Void dados
                    ) {

                        carregarMensagens();
                    }

                    @Override
                    public void onError(
                            String erro
                    ) {

                        Toast.makeText(
                                ChatActivity.this,
                                erro,
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                }
        );
    }

    private void configurarMenu() {

        bottomNavigation.setSelectedItemId(
                R.id.menu_chat
        );

        bottomNavigation.setOnItemSelectedListener(
                item -> {

                    int id =
                            item.getItemId();

                    Class<?> tela = null;

                    if (id == R.id.menu_home) {

                        tela =
                                MainActivity.class;

                    } else if (
                            id == R.id.menu_anuncio
                    ) {

                        tela =
                                BuscaActivity.class;

                    } else if (
                            id == R.id.menu_livros
                    ) {

                        tela =
                                MeusLivrosActivity.class;

                    } else if (
                            id == R.id.menu_chat
                    ) {

                        return true;

                    } else if (
                            id == R.id.menu_perfil
                    ) {

                        return true;
                    }

                    if (tela != null) {

                        Intent intent =
                                new Intent(
                                        this,
                                        tela
                                );

                        intent.setFlags(
                                Intent.FLAG_ACTIVITY_CLEAR_TOP
                                        | Intent.FLAG_ACTIVITY_SINGLE_TOP
                        );

                        startActivity(intent);
                    }

                    return true;
                }
        );
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (bottomNavigation != null) {
            bottomNavigation
                    .getMenu()
                    .findItem(R.id.menu_chat)
                    .setChecked(true);
        }

        iniciarAtualizacaoChat();
    }

    @Override
    protected void onPause() {
        super.onPause();

        pararAtualizacaoChat();
    }

    private void iniciarAtualizacaoChat() {

        handler.removeCallbacks(
                atualizarChatRunnable
        );

        handler.postDelayed(
                atualizarChatRunnable,
                4000
        );
    }

    private void pararAtualizacaoChat() {

        handler.removeCallbacks(
                atualizarChatRunnable
        );
    }
}