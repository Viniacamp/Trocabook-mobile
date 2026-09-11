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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

import br.gov.sp.cps.trocabook.R;
import br.gov.sp.cps.trocabook.model.Anuncio;
import br.gov.sp.cps.trocabook.model.Negociacao;
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

    private View cardResultadosPesquisa;
    private View layoutLoading;
    private View layoutSemResultados;

    private ImageView imgLoading;
    private EditText editPesquisa;

    private UsuarioService usuarioService;
    private AnuncioService anuncioService;
    private NegociacoesService negociacoesService;
    private AuthService authService;

    private ResultadoBuscaAdapter resultadoBuscaAdapter;

    private final List<Anuncio> listaResultados =
            new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);

        setContentView(R.layout.activity_main);

        iniciarComponentes();

        FirebaseUser usuarioAtual =
                authService.getUsuarioAtual();

        if (usuarioAtual == null) {
            finish();
            return;
        }

        configurarRecyclerViews();
        configurarPesquisa();
        configurarBottomNavigation();

        PopularBancoService popularBancoService =
                new PopularBancoService(this);

        mostrarLoading(true);

        popularBancoService.iniciarBanco(
                new PopularBancoService.Callback() {

                    @Override
                    public void onComplete() {
                        android.util.Log.d(
                                "HOME_DEBUG",
                                ">>> PopularBancoService terminou"
                        );
                        carregarDados();
                    }

                    @Override
                    public void onError(String erro) {
                        android.util.Log.e(
                                "HOME_DEBUG",
                                ">>> PopularBancoService ERRO: " + erro
                        );

                        runOnUiThread(() ->
                                mostrarLoading(false)
                        );
                    }
                }
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

        bottomNavigation =
                findViewById(R.id.bottomNavigation);

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
                            int after) {
                    }

                    @Override
                    public void onTextChanged(
                            CharSequence s,
                            int start,
                            int before,
                            int count) {

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
                            android.text.Editable s) {
                    }
                }
        );
    }

    private void buscarAnuncios(
            String texto) {

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
                            List<Anuncio> dados) {

                        runOnUiThread(() -> {

                            listaResultados.clear();

                            if (dados != null) {

                                String textoBusca =
                                        texto.toLowerCase();

                                for (Anuncio anuncio : dados) {
                                    android.util.Log.d(
                                            "HOME_CAPA",
                                            "Título: " + anuncio.getTitulo()
                                                    + " | Capa: " + anuncio.getCapa()
                                    );

                                    if (anuncio.getTitulo() != null
                                            && anuncio.getTitulo()
                                            .toLowerCase()
                                            .contains(textoBusca)) {

                                        listaResultados.add(
                                                anuncio
                                        );
                                    }
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
                    public void onError(
                            String erro) {

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
        android.util.Log.d(
                "HOME_DEBUG",
                ">>> carregarDados() foi chamado"
        );

        carregarUsuarios();

        carregarAnuncios();

        carregarNegociacoes();
    }

    private void carregarUsuarios() {

        usuarioService.buscarUsuarios(
                new UsuarioService.ServiceCallback<List<Usuario>>() {

                    @Override
                    public void onSucesso(
                            List<Usuario> dados) {

                        runOnUiThread(() -> {

                            if (dados == null) {
                                return;
                            }

                            UsuarioHorizontalAdapter adapter =
                                    new UsuarioHorizontalAdapter(
                                            dados,
                                            usuario -> {

                                                FirebaseUser usuarioAtual =
                                                        authService
                                                                .getUsuarioAtual();

                                                if (usuarioAtual == null) {
                                                    return;
                                                }

                                                Intent intent;

                                                if (usuario.getId()
                                                        .equals(
                                                                usuarioAtual
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
                    public void onErro(
                            String erro) {
                    }
                }
        );
    }

    private void carregarAnuncios() {
        android.util.Log.d(
                "HOME_DEBUG",
                ">>> carregarAnuncios() foi chamado"
        );


        anuncioService.buscarTodosOsAnuncios(
                new AnuncioService.Callback<List<Anuncio>>() {


                    @Override
                    public void onSuccess(
                            List<Anuncio> dados) {
                        android.util.Log.d(
                                "HOME_DEBUG",
                                ">>> buscarTodosOsAnuncios SUCCESS. Quantidade: "
                                        + (dados == null ? "null" : dados.size())
                        );


                        if (dados != null) {
                            for (Anuncio anuncio : dados) {
                                android.util.Log.d(
                                        "HOME_CAPA",
                                        "Título: " + anuncio.getTitulo()
                                                + " | Capa: " + anuncio.getCapa()
                                );
                            }
                        }


                        final List<Anuncio> anuncios =
                                dados != null
                                        ? dados
                                        : new ArrayList<>();

                        runOnUiThread(() -> {

                            LivroHorizontalAdapter<Anuncio> adapter =
                                    new LivroHorizontalAdapter<>(
                                            anuncios,
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
                    public void onError(
                            String erro) {
                        android.util.Log.e(
                                "HOME_DEBUG",
                                ">>> ERRO ao carregar anúncios: " + erro
                        );

                        runOnUiThread(() ->
                                mostrarLoading(false)
                        );
                    }
                }
        );
    }

    private void carregarNegociacoes() {

        negociacoesService.buscarTodos(
                new NegociacoesService.Callback<List<Negociacao>>() {

                    @Override
                    public void onSuccess(
                            List<Negociacao> dados) {

                        final List<Negociacao> negociacoes =
                                dados != null
                                        ? dados
                                        : new ArrayList<>();

                        runOnUiThread(() -> {

                            LivroHorizontalAdapter<Negociacao> adapter =
                                    new LivroHorizontalAdapter<>(
                                            negociacoes,
                                            negociacao -> {
                                                // Ação ao clicar na proposta
                                            }
                                    );

                            recyclerPropostas
                                    .setAdapter(adapter);
                        });
                    }

                    @Override
                    public void onError(
                            String erro) {
                    }
                }
        );
    }

    private void mostrarLoading(
            boolean mostrar) {

        if (mostrar) {

            layoutLoading
                    .setVisibility(View.VISIBLE);

            recyclerResultadosBusca
                    .setVisibility(View.GONE);

            if (imgLoading != null) {

                Animation rotate =
                        AnimationUtils.loadAnimation(
                                this,
                                R.anim.rotacao_loading
                        );

                imgLoading.startAnimation(
                        rotate
                );
            }

        } else {

            layoutLoading
                    .setVisibility(View.GONE);

            if (imgLoading != null) {
                imgLoading.clearAnimation();
            }
        }
    }

    private void configurarBottomNavigation() {

        bottomNavigation.setSelectedItemId(
                R.id.menu_home
        );

        bottomNavigation.setOnItemSelectedListener(
                item -> {

                    int id =
                            item.getItemId();

                    Class<?> tela = null;

                    if (id == R.id.menu_home) {

                        return true;

                    } else if (id == R.id.menu_anuncio) {

                        tela =
                                BuscaActivity.class;

                    } else if (id == R.id.menu_livros) {

                        tela =
                                MeusLivrosActivity.class;

                    } else if (id == R.id.menu_chat) {

                        tela =
                                ConversasActivity.class;

                    } else if (id == R.id.menu_perfil) {

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

            bottomNavigation.getMenu()
                    .findItem(
                            R.id.menu_home
                    )
                    .setChecked(true);
        }
    }
}