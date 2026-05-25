package br.gov.sp.cps.trocabook.ui.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

import br.gov.sp.cps.trocabook.R;
import br.gov.sp.cps.trocabook.model.Anuncio;
import br.gov.sp.cps.trocabook.model.Livro;
import br.gov.sp.cps.trocabook.model.Usuario;
import br.gov.sp.cps.trocabook.service.AnuncioService;
import br.gov.sp.cps.trocabook.service.AuthService;
import br.gov.sp.cps.trocabook.service.NegociacoesService;
import br.gov.sp.cps.trocabook.service.PopularBancoService;
import br.gov.sp.cps.trocabook.service.UsuarioService;
import br.gov.sp.cps.trocabook.ui.adapters.LivroHorizontalAdapter;
import br.gov.sp.cps.trocabook.ui.adapters.UsuarioHorizontalAdapter;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigation;

    private RecyclerView recyclerUsuarios;
    private RecyclerView recyclerLivrosDestaque;
    private RecyclerView recyclerPropostas;

    private UsuarioService usuarioService;

    private AnuncioService anuncioService;
    private NegociacoesService negociacoesService;
    private AuthService authService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);

        setContentView(R.layout.activity_main);

        bottomNavigation = findViewById(R.id.bottomNavigation);
        iniciarComponentes();
        FirebaseUser user = authService.getUsuarioAtual();
        if (user == null) {
            finish();
            return;
        }

        configurarRecyclerViews();

        configurarBottomNavigation();

        PopularBancoService popularBancoService =
                new PopularBancoService();

        popularBancoService.iniciarBanco(

                () -> {

                    carregarDados();
                }
        );
    }

    private void iniciarComponentes() {
        usuarioService = new UsuarioService();

        anuncioService = new AnuncioService();

        negociacoesService = new NegociacoesService();

        authService = new AuthService(this);

        recyclerUsuarios = findViewById(R.id.recyclerUsuarios);

        recyclerLivrosDestaque =
                findViewById(R.id.recyclerLivrosDestaque);

        recyclerPropostas =
                findViewById(R.id.recyclerPropostas);
    }

    private void configurarRecyclerViews() {

        recyclerUsuarios.setLayoutManager(
                new LinearLayoutManager(
                        this,
                        LinearLayoutManager.HORIZONTAL,
                        false
                )
        );

        recyclerLivrosDestaque.setLayoutManager(
                new LinearLayoutManager(
                        this,
                        LinearLayoutManager.HORIZONTAL,
                        false
                )
        );

        recyclerPropostas.setLayoutManager(
                new LinearLayoutManager(
                        this,
                        LinearLayoutManager.HORIZONTAL,
                        false
                )
        );
    }

    private void carregarDados() {
        usuarioService.buscarUsuarios(new UsuarioService.ServiceCallback<List<Usuario>>() {
            @Override
            public void onSuccess(List<Usuario> data) {
                UsuarioHorizontalAdapter adapter =
                        new UsuarioHorizontalAdapter(data, usuario -> {
                            Intent intent;
                            if (usuario.getId().equals(authService.getUsuarioAtual().getUid())){
                                intent = new Intent(MainActivity.this, MeusLivrosActivity.class);
                                startActivity(intent);
                            } else {
                                intent = new Intent(MainActivity.this, TelaPerfilNegociadorActivity.class);
                                intent.putExtra("userId", usuario.getId());
                                startActivity(intent);
                            }
                        });
                recyclerUsuarios.setAdapter(adapter);

            }

            @Override
            public void onError(String erro) {

            }
        });


        anuncioService.buscarTodosOsAnuncios(new AnuncioService.Callback<List<Anuncio>>() {
            @Override
            public void onSuccess(List<Anuncio> dados) {
                LivroHorizontalAdapter<Anuncio> adapter = new LivroHorizontalAdapter<>(dados, anuncio -> {
                    Intent intent = new Intent(MainActivity.this, DetalhesAnuncioActivity.class);
                    intent.putExtra("anuncioId", anuncio.getId());
                    startActivity(intent);
                });
                recyclerLivrosDestaque.setAdapter(adapter);
            }

            @Override
            public void onError(String mensagem) {

            }
        });

        negociacoesService.buscarTodasNegociacoes(new NegociacoesService.Callback<List<Livro>>() {
            @Override
            public void onSuccess(List<Livro> dados) {
                LivroHorizontalAdapter<Livro> adapter = new LivroHorizontalAdapter<>(dados, livro -> {});
                recyclerPropostas.setAdapter(adapter);
            }

            @Override
            public void onError(String erro) {

            }
        });
    }

    private void configurarBottomNavigation() {

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

    @Override
    protected void onResume() {

        super.onResume();

        bottomNavigation.getMenu()
                .findItem(R.id.menu_home)
                .setChecked(true);
    }
}