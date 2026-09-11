package br.gov.sp.cps.trocabook.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

import br.gov.sp.cps.trocabook.R;
import br.gov.sp.cps.trocabook.model.Anuncio;
import br.gov.sp.cps.trocabook.model.Negociacao;
import br.gov.sp.cps.trocabook.model.Usuario;
import br.gov.sp.cps.trocabook.service.AnuncioService;
import br.gov.sp.cps.trocabook.service.AuthService;
import br.gov.sp.cps.trocabook.service.NegociacoesService;
import br.gov.sp.cps.trocabook.service.UsuarioService;
import br.gov.sp.cps.trocabook.ui.adapters.LivroHorizontalAdapter;

public class TelaPerfilNegociadorActivity extends AppCompatActivity {

    private ImageView imagePerfil;

    private TextView textNome;
    private TextView textTrocas;
    private TextView textVendas;
    private TextView textNegociacoes;
    private BottomNavigationView bottomNavigation;

    private RecyclerView recyclerLivros;

    private UsuarioService usuarioService;
    private NegociacoesService negociacoesService;
    private AnuncioService anuncioService;
    private AuthService authService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);

        setContentView(R.layout.activity_tela_perfil_negociador);

        bottomNavigation = findViewById(R.id.bottomNavigation);

        ViewCompat.setOnApplyWindowInsetsListener(
                findViewById(R.id.main),
                (v, insets) -> {

                    Insets systemBars =
                            insets.getInsets(WindowInsetsCompat.Type.systemBars());

                    v.setPadding(
                            systemBars.left,
                            systemBars.top,
                            systemBars.right,
                            systemBars.bottom
                    );

                    return insets;
                }
        );

        inicializarViews();

        usuarioService = new UsuarioService();
        negociacoesService = new NegociacoesService();
        anuncioService = new AnuncioService();
        authService = new AuthService(this);

        FirebaseUser user = authService.getUsuarioAtual();

        if (user == null) {
            finish();
            return;
        }

        String userId = getIntent().getStringExtra("userId");

        if (userId == null || userId.trim().isEmpty()) {
            finish();
            return;
        }

        carregarUsuario(userId);
        carregarLivros(userId);
        carregarTrocas(userId);
        carregarVendas(userId);

        configurarMenu();
    }

    private void inicializarViews() {

        imagePerfil = findViewById(R.id.imagePerfil);

        textNome = findViewById(R.id.textNome);

        textTrocas = findViewById(R.id.textTrocas);

        textVendas = findViewById(R.id.textVendas);

        textNegociacoes = findViewById(R.id.textNegociacoes);

        recyclerLivros = findViewById(R.id.recyclerLivros);

        recyclerLivros.setLayoutManager(
                new GridLayoutManager(this, 2)
        );
    }

    private void carregarUsuario(String userId) {

        usuarioService.buscarUsuarioPorId(
                userId,

                new UsuarioService.ServiceCallback<Usuario>() {

                    @Override
                    public void onSucesso(Usuario usuario) {

                        if (usuario == null) {
                            return;
                        }

                        textNome.setText(
                                "Email do Negociador(a): "
                                        + usuario.getEmailPrincipal()
                        );

                        Glide.with(TelaPerfilNegociadorActivity.this)
                                .load(usuario.getFotoPerfil())
                                .placeholder(R.drawable.placeholder_usuario)
                                .into(imagePerfil);
                    }

                    @Override
                    public void onErro(String erro) {
                        // Não interrompe a visualização do perfil.
                    }
                }
        );
    }

    private void carregarLivros(String userId) {

        anuncioService.buscarAnunciosUsuario(
                userId,

                new AnuncioService.Callback<List<Anuncio>>() {

                    @Override
                    public void onSuccess(List<Anuncio> dados) {

                        if (dados == null) {
                            return;
                        }

                        LivroHorizontalAdapter<Anuncio> adapter =
                                new LivroHorizontalAdapter<>(
                                        dados,
                                        anuncio -> {

                                            Intent intent = new Intent(
                                                    TelaPerfilNegociadorActivity.this,
                                                    DetalhesAnuncioActivity.class
                                            );

                                            intent.putExtra(
                                                    "anuncioId",
                                                    anuncio.getId()
                                            );

                                            startActivity(intent);
                                        }
                                );

                        recyclerLivros.setAdapter(adapter);
                    }

                    @Override
                    public void onError(String erro) {
                        // Não interrompe a visualização do perfil.
                    }
                }
        );
    }

    private void carregarTrocas(String userId) {

        negociacoesService.contarNegociacoesPorUsuarioETipo(
                userId,
                Negociacao.TipoNegociacao.TROCA,

                new NegociacoesService.Callback<Long>() {

                    @Override
                    public void onSuccess(Long dados) {

                        long totalTrocas = dados != null ? dados : 0;

                        textTrocas.setText(
                                "Trocas realizadas: " + totalTrocas
                        );

                        atualizarTotal();
                    }

                    @Override
                    public void onError(String erro) {
                        // Mantém o valor padrão.
                    }
                }
        );
    }

    private void carregarVendas(String userId) {

        negociacoesService.contarNegociacoesPorUsuarioETipo(
                userId,
                Negociacao.TipoNegociacao.VENDA,

                new NegociacoesService.Callback<Long>() {

                    @Override
                    public void onSuccess(Long dados) {

                        long totalVendas = dados != null ? dados : 0;

                        textVendas.setText(
                                "Vendas realizadas: " + totalVendas
                        );

                        atualizarTotal();
                    }

                    @Override
                    public void onError(String erro) {
                        // Mantém o valor padrão.
                    }
                }
        );
    }

    private void atualizarTotal() {

        String trocasTexto = textTrocas.getText().toString();
        String vendasTexto = textVendas.getText().toString();

        String trocasNumeros =
                trocasTexto.replaceAll("\\D+", "");

        String vendasNumeros =
                vendasTexto.replaceAll("\\D+", "");

        int trocas = trocasNumeros.isEmpty()
                ? 0
                : Integer.parseInt(trocasNumeros);

        int vendas = vendasNumeros.isEmpty()
                ? 0
                : Integer.parseInt(vendasNumeros);

        int total = trocas + vendas;

        textNegociacoes.setText(
                "Total de negociações: " + total
        );
    }

    private void configurarMenu() {

        bottomNavigation.setSelectedItemId(R.id.menu_home);

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

                Intent intent = new Intent(this, tela);

                intent.setFlags(
                        Intent.FLAG_ACTIVITY_CLEAR_TOP |
                                Intent.FLAG_ACTIVITY_SINGLE_TOP
                );

                startActivity(intent);
            }

            return true;
        });
    }
}