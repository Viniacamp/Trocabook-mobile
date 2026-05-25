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


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {

            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());

            v.setPadding(
                    systemBars.left,
                    systemBars.top,
                    systemBars.right,
                    systemBars.bottom
            );

            return insets;
        });

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
                    public void onSuccess(Usuario usuario) {

                        textNome.setText(
                                "Email do Negociador(a): "
                                        + usuario.getEmail_principal()
                        );

                        Glide.with(TelaPerfilNegociadorActivity.this)
                                .load(usuario.getFoto_perfil())
                                .placeholder(R.drawable.placeholder_usuario)
                                .into(imagePerfil);
                    }

                    @Override
                    public void onError(String erro) {

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

                        LivroHorizontalAdapter<Anuncio> adapter =
                                new LivroHorizontalAdapter<>(dados, anuncio ->{
                                    Intent intent = new Intent(TelaPerfilNegociadorActivity.this, DetalhesAnuncioActivity.class);
                                    intent.putExtra("anuncioId", anuncio.getId());
                                    startActivity(intent);
                                    finish();
                                });

                        recyclerLivros.setAdapter(adapter);
                    }

                    @Override
                    public void onError(String erro) {

                    }
                }
        );
    }

    private void carregarTrocas(String userId) {

        negociacoesService.contarTipoNegociacao(
                userId,
                "TROCA",

                new NegociacoesService.Callback<Integer>() {

                    @Override
                    public void onSuccess(Integer dados) {

                        textTrocas.setText(
                                "Trocas realizadas: " + dados
                        );

                        atualizarTotal();
                    }

                    @Override
                    public void onError(String erro) {

                    }
                }
        );
    }

    private void carregarVendas(String userId) {

        negociacoesService.contarTipoNegociacao(
                userId,
                "VENDA",

                new NegociacoesService.Callback<Integer>() {

                    @Override
                    public void onSuccess(Integer dados) {

                        textVendas.setText(
                                "Vendas realizadas: " + dados
                        );

                        atualizarTotal();
                    }

                    @Override
                    public void onError(String erro) {

                    }
                }
        );
    }

    private void atualizarTotal() {

        String trocasTexto = textTrocas.getText().toString();

        String vendasTexto = textVendas.getText().toString();

        int trocas = Integer.parseInt(
                trocasTexto.replaceAll("\\D+", "")
        );

        int vendas = Integer.parseInt(
                vendasTexto.replaceAll("\\D+", "")
        );

        int total = trocas + vendas;

        textNegociacoes.setText(
                "Total de negociações: " + total
        );
    }

    private void configurarMenu() {

        BottomNavigationView bottomNavigation =
                findViewById(R.id.bottomNavigation);

        bottomNavigation.setSelectedItemId(R.id.menu_home);

        bottomNavigation.setOnItemSelectedListener(item -> {

            int id = item.getItemId();

            if (id == R.id.menu_home) {

                finish();

                return true;
            }

            return false;
        });
    }
}