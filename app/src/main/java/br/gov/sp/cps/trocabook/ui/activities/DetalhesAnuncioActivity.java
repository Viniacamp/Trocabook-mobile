package br.gov.sp.cps.trocabook.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;
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

    private TextView textTitulo;
    private TextView textDescricao;
    private TextView textUsuario;
    private TextView textTipoNegociacao;

    private Button buttonNegociar;

    private AuthService authService;
    private AnuncioService anuncioService;
    private UsuarioService usuarioService;

    private BottomNavigationView bottomNavigation;

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

                        configurarUsuario(
                                dados.getUidUsuario()
                        );

                        configurarInformacoesLivro(
                                dados.getCapa(),
                                dados.getTitulo(),
                                dados.getDescricao(),
                                dados.getTipoNegociacao()
                        );

                        buttonNegociar.setOnClickListener(v -> {

                            Intent intent = new Intent(
                                    DetalhesAnuncioActivity.this,
                                    ChatActivity.class
                            );

                            intent.putExtra(
                                    "anuncioId",
                                    dados.getId()
                            );

                            intent.putExtra(
                                    "userDestinatario",
                                    dados.getUidUsuario()
                            );

                            startActivity(intent);
                        });
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