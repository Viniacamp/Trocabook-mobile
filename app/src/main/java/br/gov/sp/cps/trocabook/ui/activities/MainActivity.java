package br.gov.sp.cps.trocabook.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
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
import br.gov.sp.cps.trocabook.ui.adapters.ResultadoBuscaAdapter;
import br.gov.sp.cps.trocabook.ui.adapters.UsuarioHorizontalAdapter;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigation;

    private RecyclerView recyclerUsuarios;

    private RecyclerView recyclerLivrosDestaque;

    private RecyclerView recyclerPropostas;

    private RecyclerView recyclerResultadosBusca;

    private CardView cardResultadosPesquisa;

    private View layoutLoading;

    private View layoutSemResultados;

    private ImageView imgLoading;

    private EditText editPesquisa;

    private UsuarioService usuarioService;

    private AnuncioService anuncioService;

    private NegociacoesService negociacoesService;

    private AuthService authService;

    private ResultadoBuscaAdapter resultadoBuscaAdapter;

    private List<Anuncio> listaResultados;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);

        setContentView(R.layout.activity_main);

        bottomNavigation =
                findViewById(R.id.bottomNavigation);

        iniciarComponentes();

        FirebaseUser user =
                authService.getUsuarioAtual();

        if (user == null) {

            finish();

            return;
        }

        configurarRecyclerViews();

        configurarBottomNavigation();

        configurarPesquisa();

        PopularBancoService popularBancoService =
                new PopularBancoService();

        mostrarLoading(true);

        popularBancoService.iniciarBanco(
                this::carregarDados
        );
    }

    private void iniciarComponentes() {

        usuarioService =
                new UsuarioService();

        anuncioService =
                new AnuncioService();

        negociacoesService =
                new NegociacoesService();

        authService =
                new AuthService(this);

        recyclerUsuarios =
                findViewById(R.id.recyclerUsuarios);

        recyclerLivrosDestaque =
                findViewById(R.id.recyclerLivrosDestaque);

        recyclerPropostas =
                findViewById(R.id.recyclerPropostas);

        recyclerResultadosBusca =
                findViewById(R.id.recyclerPesquisa);

        cardResultadosPesquisa =
                findViewById(R.id.cardResultadosPesquisa);

        layoutLoading =
                findViewById(R.id.layoutLoadingPesquisa);

        layoutSemResultados =
                findViewById(R.id.layoutSemResultados);

        imgLoading =
                findViewById(R.id.imgLoadingPesquisa);

        editPesquisa =
                findViewById(R.id.editPesquisa);

        listaResultados =
                new ArrayList<>();
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

        recyclerResultadosBusca.setLayoutManager(
                new LinearLayoutManager(this)
        );

        resultadoBuscaAdapter =
                new ResultadoBuscaAdapter(
                        listaResultados,
                        anuncio -> {

                            Intent intent =
                                    new Intent(
                                            MainActivity.this,
                                            DetalhesAnuncioActivity.class
                                    );

                            intent.putExtra(
                                    "anuncioId",
                                    anuncio.getId()
                            );

                            startActivity(intent);
                        }
                );

        recyclerResultadosBusca.setAdapter(
                resultadoBuscaAdapter
        );
    }

    private void configurarPesquisa() {

        editPesquisa.addTextChangedListener(
                new android.text.TextWatcher() {

                    @Override
                    public void beforeTextChanged(
                            CharSequence s,
                            int start,
                            int count,
                            int after
                    ) {

                    }

                    @Override
                    public void onTextChanged(
                            CharSequence s,
                            int start,
                            int before,
                            int count
                    ) {

                        String texto =
                                s.toString().trim();

                        if (texto.isEmpty()) {

                            cardResultadosPesquisa
                                    .setVisibility(View.GONE);

                            listaResultados.clear();

                            resultadoBuscaAdapter
                                    .notifyDataSetChanged();

                            return;
                        }

                        buscarAnuncios(texto);
                    }

                    @Override
                    public void afterTextChanged(
                            android.text.Editable s
                    ) {

                    }
                }
        );

        editPesquisa.setOnFocusChangeListener(
                (v, hasFocus) -> {

                    if (!hasFocus) {

                        cardResultadosPesquisa
                                .setVisibility(View.GONE);
                    }
                }
        );
    }

    private void buscarAnuncios(String texto) {

        cardResultadosPesquisa
                .setVisibility(View.VISIBLE);

        layoutSemResultados
                .setVisibility(View.GONE);

        recyclerResultadosBusca
                .setVisibility(View.GONE);

        mostrarLoading(true);

        anuncioService.buscarTodosOsAnuncios(
                new AnuncioService.Callback<List<Anuncio>>() {

                    @Override
                    public void onSuccess(
                            List<Anuncio> dados
                    ) {

                        runOnUiThread(() -> {

                            listaResultados.clear();

                            for (Anuncio anuncio : dados) {

                                if (anuncio.getTitulo()
                                        .toLowerCase()
                                        .contains(
                                                texto.toLowerCase()
                                        )) {

                                    listaResultados.add(anuncio);
                                }
                            }

                            resultadoBuscaAdapter
                                    .notifyDataSetChanged();

                            mostrarLoading(false);

                            if (listaResultados.isEmpty()) {

                                layoutSemResultados
                                        .setVisibility(View.VISIBLE);

                            } else {

                                recyclerResultadosBusca
                                        .setVisibility(View.VISIBLE);
                            }
                        });
                    }

                    @Override
                    public void onError(String mensagem) {

                        runOnUiThread(() -> {

                            mostrarLoading(false);

                            layoutSemResultados
                                    .setVisibility(View.VISIBLE);
                        });
                    }
                }
        );
    }

    private void carregarDados() {

        usuarioService.buscarUsuarios(
                new UsuarioService.ServiceCallback<List<Usuario>>() {

                    @Override
                    public void onSuccess(List<Usuario> data) {

                        runOnUiThread(() -> {

                            UsuarioHorizontalAdapter adapter =
                                    new UsuarioHorizontalAdapter(
                                            data,
                                            usuario -> {

                                                Intent intent;

                                                if (usuario.getId().equals(
                                                        authService
                                                                .getUsuarioAtual()
                                                                .getUid()
                                                )) {

                                                    intent =
                                                            new Intent(
                                                                    MainActivity.this,
                                                                    MeusLivrosActivity.class
                                                            );

                                                } else {

                                                    intent =
                                                            new Intent(
                                                                    MainActivity.this,
                                                                    TelaPerfilNegociadorActivity.class
                                                            );

                                                    intent.putExtra(
                                                            "userId",
                                                            usuario.getId()
                                                    );
                                                }

                                                startActivity(intent);
                                            }
                                    );

                            recyclerUsuarios
                                    .setAdapter(adapter);
                        });
                    }

                    @Override
                    public void onError(String erro) {

                    }
                }
        );

        anuncioService.buscarTodosOsAnuncios(
                new AnuncioService.Callback<List<Anuncio>>() {

                    @Override
                    public void onSuccess(
                            List<Anuncio> dados
                    ) {

                        runOnUiThread(() -> {

                            LivroHorizontalAdapter<Anuncio> adapter =
                                    new LivroHorizontalAdapter<>(
                                            dados,
                                            anuncio -> {

                                                Intent intent =
                                                        new Intent(
                                                                MainActivity.this,
                                                                DetalhesAnuncioActivity.class
                                                        );

                                                intent.putExtra(
                                                        "anuncioId",
                                                        anuncio.getId()
                                                );

                                                startActivity(intent);
                                            }
                                    );

                            recyclerLivrosDestaque
                                    .setAdapter(adapter);

                            mostrarLoading(false);
                        });
                    }

                    @Override
                    public void onError(String mensagem) {

                        runOnUiThread(() ->
                                mostrarLoading(false)
                        );
                    }
                }
        );

        negociacoesService.buscarTodasNegociacoes(
                new NegociacoesService.Callback<List<Livro>>() {

                    @Override
                    public void onSuccess(List<Livro> dados) {

                        runOnUiThread(() -> {

                            LivroHorizontalAdapter<Livro> adapter =
                                    new LivroHorizontalAdapter<>(
                                            dados,
                                            livro -> {}
                                    );

                            recyclerPropostas
                                    .setAdapter(adapter);
                        });
                    }

                    @Override
                    public void onError(String erro) {

                    }
                }
        );
    }

    private void mostrarLoading(boolean mostrar) {

        if (mostrar) {

            layoutLoading
                    .setVisibility(View.VISIBLE);

            recyclerResultadosBusca
                    .setVisibility(View.GONE);

            Animation rotate =
                    AnimationUtils.loadAnimation(
                            this,
                            R.anim.rotacao_loading
                    );

            imgLoading.startAnimation(rotate);

        } else {

            layoutLoading
                    .setVisibility(View.GONE);

            imgLoading.clearAnimation();
        }
    }

    private void configurarBottomNavigation() {

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

                Intent intent =
                        new Intent(this, tela);

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

        if (bottomNavigation != null) {

            bottomNavigation.getMenu()
                    .findItem(R.id.menu_home)
                    .setChecked(true);
        }
    }
}