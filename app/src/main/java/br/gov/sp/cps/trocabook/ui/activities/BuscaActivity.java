package br.gov.sp.cps.trocabook.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.List;

import br.gov.sp.cps.trocabook.R;
import br.gov.sp.cps.trocabook.model.Livro;
import br.gov.sp.cps.trocabook.model.dto.LivroBuscaOutput;
import br.gov.sp.cps.trocabook.service.AnuncioService;
import br.gov.sp.cps.trocabook.service.AuthService;
import br.gov.sp.cps.trocabook.service.LivroService;
import br.gov.sp.cps.trocabook.ui.adapters.LivroBuscaAdapter;

public class BuscaActivity extends AppCompatActivity {

    private TextInputLayout textLayoutTitulo;
    private TextInputEditText textEditTitulo;

    private Button btnBuscar;
    private Button btnAnunciarManualmente;

    private RecyclerView recyclerViewLivros;

    private LivroBuscaAdapter livroBuscaAdapter;

    private List<LivroBuscaOutput> listaLivros = new ArrayList<>();

    private AuthService authService;
    private LivroService livroService;

    private BottomNavigationView bottomNavigation;

    private CardView layoutLoading;
    private ImageView imgLoading;
    private Animation animacaoLoading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_busca_livro);

        bottomNavigation = findViewById(R.id.bottomNavigation);

        authService = new AuthService(this);
        livroService = new LivroService();

        if (authService.getUsuarioAtual() == null) {
            finish();
            return;
        }

        inicializarComponentes();
        configurarMenu();
        configurarRecycler();
        configurarEventos();
    }

    private void inicializarComponentes() {

        textLayoutTitulo =
                findViewById(R.id.textLayoutTitulo);

        textEditTitulo =
                findViewById(R.id.textEditTitulo);

        btnBuscar =
                findViewById(R.id.btnBuscar);

        btnAnunciarManualmente =
                findViewById(R.id.btnAnuncioManual);

        recyclerViewLivros =
                findViewById(R.id.recyclerViewLivros);

        layoutLoading =
                findViewById(R.id.layoutLoading);

        imgLoading =
                findViewById(R.id.imgLoading);

        animacaoLoading =
                AnimationUtils.loadAnimation(
                        this,
                        R.anim.rotacao_loading
                );
    }

    private void configurarRecycler() {

        recyclerViewLivros.setLayoutManager(
                new LinearLayoutManager(this)
        );

        livroBuscaAdapter = new LivroBuscaAdapter(
                listaLivros,
                this::selecionarLivro
        );

        recyclerViewLivros.setAdapter(livroBuscaAdapter);
    }

    private void selecionarLivro(LivroBuscaOutput livroOutput) {

        if (livroOutput == null) {
            return;
        }

        mostrarLoading();


        livroService.cadastrarLivroGoogleBooks(
                livroOutput,
                new LivroService.Callback<Livro>() {

                    @Override
                    public void onSucesso(Livro livro) {

                        runOnUiThread(() -> {

                            esconderLoading();

                            abrirAnuncioActivity(livro);
                        });
                    }

                    @Override
                    public void onErro(Exception e) {

                        runOnUiThread(() -> {

                            esconderLoading();

                            Log.e(
                                    "BUSCA",
                                    "Erro ao cadastrar livro",
                                    e
                            );
                        });
                    }
                }
        );
    }

    private void abrirAnuncioActivity(Livro livro) {

        Intent intent = new Intent(
                BuscaActivity.this,
                AnuncioActivity.class
        );

        intent.putExtra(
                "livro",
                livro
        );

        startActivity(intent);
    }

    private void configurarEventos() {

        btnBuscar.setOnClickListener(
                v -> buscarLivro()
        );

        btnAnunciarManualmente.setOnClickListener(v -> {

            startActivity(
                    new Intent(
                            this,
                            AnuncioManualActivity.class
                    )
            );

            finish();
        });
    }

    private void buscarLivro() {

        String titulo =
                textEditTitulo.getText()
                        .toString()
                        .trim();

        if (titulo.isEmpty()) {

            textLayoutTitulo.setError(
                    "Digite um título"
            );

            return;
        }

        textLayoutTitulo.setError(null);

        mostrarLoading();

        livroService.buscarLivros(
                titulo,
                new LivroService.Callback<List<LivroBuscaOutput>>() {

                    @Override
                    public void onSucesso(
                            List<LivroBuscaOutput> livros) {

                        runOnUiThread(() -> {

                            esconderLoading();

                            atualizarLista(livros);
                        });
                    }

                    @Override
                    public void onErro(Exception e) {

                        runOnUiThread(() -> {

                            esconderLoading();

                            Log.e(
                                    "BUSCA",
                                    "Erro ao buscar livros",
                                    e
                            );
                        });
                    }
                }
        );
    }

    private void atualizarLista(
            List<LivroBuscaOutput> livros) {

        livroBuscaAdapter.atualizarLista(livros);
    }

    private void mostrarLoading() {

        layoutLoading.setVisibility(
                View.VISIBLE
        );

        imgLoading.startAnimation(
                animacaoLoading
        );
    }

    private void esconderLoading() {

        layoutLoading.setVisibility(
                View.GONE
        );

        imgLoading.clearAnimation();
    }

    private void configurarMenu() {

        bottomNavigation.setSelectedItemId(
                R.id.menu_anuncio
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
                .findItem(R.id.menu_anuncio)
                .setChecked(true);
    }
}