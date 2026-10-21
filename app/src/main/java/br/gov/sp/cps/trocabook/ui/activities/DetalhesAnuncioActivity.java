package br.gov.sp.cps.trocabook.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseUser;

import br.gov.sp.cps.trocabook.R;
import br.gov.sp.cps.trocabook.model.Anuncio;
import br.gov.sp.cps.trocabook.model.Usuario;
import br.gov.sp.cps.trocabook.service.AnuncioService;
import br.gov.sp.cps.trocabook.service.AuthService;
import br.gov.sp.cps.trocabook.service.UsuarioService;

public class DetalhesAnuncioActivity extends AppCompatActivity {

    private ImageView imageCapa;
    private ImageView imageUsuario;
    private ImageView buttonEditar;

    private TextView textTitulo;
    private TextView textDescricao;
    private TextView textUsuario;
    private TextView textTipoNegociacao;

    private Button buttonNegociar;

    private AuthService authService;
    private AnuncioService anuncioService;
    private UsuarioService usuarioService;

    private BottomNavigationView bottomNavigation;

    private Anuncio anuncioAtual;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_detalhes_anuncio);

        bottomNavigation = findViewById(R.id.bottomNavigation);

        inicializarComponentes();

        FirebaseUser user = authService.getUsuarioAtual();

        if (user == null) {
            finish();
            return;
        }

        configurarMenu();

        String anuncioId = getIntent().getStringExtra("anuncioId");

        if (anuncioId == null || anuncioId.trim().isEmpty()) {
            Toast.makeText(
                    this,
                    "Anúncio inválido",
                    Toast.LENGTH_SHORT
            ).show();

            finish();
            return;
        }

        carregarAnuncio(anuncioId);

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

    private void carregarAnuncio(String anuncioId) {

        anuncioService.buscarAnuncioPorId(
                anuncioId,
                new AnuncioService.Callback<Anuncio>() {

                    @Override
                    public void onSuccess(Anuncio dados) {

                        if (dados == null) {
                            Toast.makeText(
                                    DetalhesAnuncioActivity.this,
                                    "Anúncio não encontrado",
                                    Toast.LENGTH_SHORT
                            ).show();

                            finish();
                            return;
                        }

                        anuncioAtual = dados;

                        configurarUsuario(
                                dados.getUidUsuario()
                        );

                        configurarInformacoesLivro(
                                dados.getCapa(),
                                dados.getTitulo(),
                                dados.getDescricao(),
                                dados.getTipoNegociacao()
                        );

                        configurarAcoes(dados);
                    }

                    @Override
                    public void onError(String mensagem) {

                        Toast.makeText(
                                DetalhesAnuncioActivity.this,
                                mensagem,
                                Toast.LENGTH_SHORT
                        ).show();

                        finish();
                    }
                }
        );
    }

    private void configurarAcoes(Anuncio anuncio) {

        FirebaseUser usuarioAtual =
                authService.getUsuarioAtual();

        if (usuarioAtual == null) {
            finish();
            return;
        }

        boolean proprioAnuncio =
                usuarioAtual.getUid()
                        .equals(anuncio.getUidUsuario());

        if (proprioAnuncio) {

            buttonNegociar.setVisibility(
                    android.view.View.GONE
            );

            buttonEditar.setVisibility(
                    android.view.View.VISIBLE
            );

            buttonEditar.setOnClickListener(
                    v -> abrirModalEdicao()
            );

        } else {

            buttonEditar.setVisibility(
                    android.view.View.GONE
            );

            buttonNegociar.setVisibility(
                    android.view.View.VISIBLE
            );

            buttonNegociar.setOnClickListener(v -> {

                Intent intent = new Intent(
                        DetalhesAnuncioActivity.this,
                        ChatActivity.class
                );

                intent.putExtra(
                        "anuncioId",
                        anuncio.getId()
                );

                intent.putExtra(
                        "userDestinatario",
                        anuncio.getUidUsuario()
                );

                startActivity(intent);
            });
        }
    }

    private void abrirModalEdicao() {

        if (anuncioAtual == null) {
            return;
        }

        View view = getLayoutInflater()
                .inflate(
                        R.layout.dialog_editar_anuncio,
                        null
                );

        TextInputEditText editDescricao =
                view.findViewById(R.id.editDescricao);

        Spinner spinnerTipo =
                view.findViewById(
                        R.id.spinnerTipoNegociacao
                );

        editDescricao.setText(
                anuncioAtual.getDescricao()
        );

        String[] tipos = {
                "Troca",
                "Venda",
                "Ambos"
        };

        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_spinner_item,
                        tipos
                );

        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        spinnerTipo.setAdapter(adapter);

        switch (anuncioAtual.getTipoNegociacao()) {
            case TROCA:
                spinnerTipo.setSelection(0);
                break;

            case VENDA:
                spinnerTipo.setSelection(1);
                break;

            case AMBOS:
                spinnerTipo.setSelection(2);
                break;
        }

        new MaterialAlertDialogBuilder(this)
                .setTitle("Editar anúncio")
                .setView(view)
                .setNegativeButton(
                        "Cancelar",
                        null
                )
                .setPositiveButton(
                        "Salvar",
                        (dialog, which) -> {

                            salvarEdicao(
                                    editDescricao,
                                    spinnerTipo
                            );
                        }
                )
                .show();
    }

    private void salvarEdicao(
            TextInputEditText editDescricao,
            Spinner spinnerTipo
    ) {
        FirebaseUser usuarioAtual =
                authService.getUsuarioAtual();

        if (usuarioAtual == null) {
            Toast.makeText(
                    this,
                    "Usuário não autenticado",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        if (anuncioAtual == null) {
            return;
        }

        String descricao =
                editDescricao.getText() != null
                        ? editDescricao.getText()
                        .toString()
                        .trim()
                        : "";

        String tipoSelecionado =
                spinnerTipo.getSelectedItem()
                        .toString();

        Anuncio.TipoNegociacao tipo;

        switch (tipoSelecionado) {

            case "Troca":
                tipo = Anuncio.TipoNegociacao.TROCA;
                break;

            case "Venda":
                tipo = Anuncio.TipoNegociacao.VENDA;
                break;

            default:
                tipo = Anuncio.TipoNegociacao.AMBOS;
                break;
        }

        anuncioAtual.setDescricao(
                descricao.isEmpty()
                        ? "Anunciante não informou uma descrição"
                        : descricao
        );

        anuncioAtual.setTipoNegociacao(tipo);



        anuncioService.atualizarAnuncio(
                anuncioAtual,
                usuarioAtual.getUid(),
                new AnuncioService.Callback<Anuncio>() {

                    @Override
                    public void onSuccess(Anuncio anuncio) {

                        anuncioAtual = anuncio;

                        runOnUiThread(() -> {

                            configurarInformacoesLivro(
                                    anuncio.getCapa(),
                                    anuncio.getTitulo(),
                                    anuncio.getDescricao(),
                                    anuncio.getTipoNegociacao()
                            );

                            Toast.makeText(
                                    DetalhesAnuncioActivity.this,
                                    "Anúncio atualizado",
                                    Toast.LENGTH_SHORT
                            ).show();
                        });
                    }

                    @Override
                    public void onError(String mensagem) {

                        runOnUiThread(() ->
                                Toast.makeText(
                                        DetalhesAnuncioActivity.this,
                                        mensagem,
                                        Toast.LENGTH_SHORT
                                ).show()
                        );
                    }
                }
        );
    }

    private void configurarUsuario(String userId) {

        usuarioService.buscarUsuarioPorId(
                userId,
                new UsuarioService.ServiceCallback<Usuario>() {

                    @Override
                    public void onSucesso(Usuario usuario) {

                        if (usuario == null) {
                            textUsuario.setText(
                                    "Usuário não encontrado"
                            );
                            return;
                        }

                        // Por enquanto, exibindo o email.
                        // Depois pode ser alterado para o nome.
                        textUsuario.setText(
                                usuario.getEmailPrincipal()
                        );

                        Glide.with(
                                        DetalhesAnuncioActivity.this
                                )
                                .load(usuario.getFotoPerfil())
                                .placeholder(
                                        R.drawable.placeholder_usuario
                                )
                                .into(imageUsuario);
                    }

                    @Override
                    public void onErro(String erro) {

                        textUsuario.setText(
                                "Usuário não encontrado"
                        );
                    }
                }
        );
    }

    private void configurarInformacoesLivro(
            String capa,
            String titulo,
            String descricao,
            Anuncio.TipoNegociacao tipoNegociacao
    ) {

        Glide.with(this)
                .load(capa)
                .placeholder(R.drawable.placeholder_capa)
                .into(imageCapa);

        textTitulo.setText(
                titulo != null
                        ? titulo
                        : "Título não informado"
        );

        textDescricao.setText(
                descricao != null && !descricao.trim().isEmpty()
                        ? descricao
                        : "Anunciante não informou uma descrição"
        );

        if (tipoNegociacao != null) {

            textTipoNegociacao.setText(
                    "Tipo: " + tipoNegociacao.name()
            );

        } else {

            textTipoNegociacao.setText(
                    "Tipo: não informado"
            );
        }
    }

    private void inicializarComponentes() {

        imageCapa = findViewById(R.id.imageCapa);
        imageUsuario = findViewById(R.id.imageUsuario);
        buttonEditar = findViewById(R.id.buttonEditar);

        textTitulo = findViewById(R.id.textTitulo);
        textDescricao = findViewById(R.id.textDescricao);
        textUsuario = findViewById(R.id.textUsuario);
        textTipoNegociacao = findViewById(R.id.textTipoNegociacao);

        buttonNegociar = findViewById(R.id.buttonNegociar);

        authService = new AuthService(this);
        anuncioService = new AnuncioService();
        usuarioService = new UsuarioService();
    }

    private void configurarMenu() {

        bottomNavigation.setSelectedItemId(
                R.id.menu_home
        );

        bottomNavigation.setOnItemSelectedListener(item -> {

            int id = item.getItemId();

            Class<?> tela = null;

            if (id == R.id.menu_home) {

                tela = MainActivity.class;

            } else if (id == R.id.menu_anuncio) {

                tela = BuscaActivity.class;

            } else if (id == R.id.menu_livros) {

                tela = MeusLivrosActivity.class;

            } else if (id == R.id.menu_chat) {

                tela = ConversasActivity.class;

            } else if (id == R.id.menu_perfil) {

                return true;
            }

            if (tela != null) {

                Intent intent = new Intent(
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
        });
    }

    @Override
    protected void onResume() {

        super.onResume();

        if (bottomNavigation != null) {

            bottomNavigation.getMenu()
                    .findItem(R.id.menu_home)
                    .setChecked(true);
        }
    }
}