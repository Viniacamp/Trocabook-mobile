package br.gov.sp.cps.trocabook.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

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
import br.gov.sp.cps.trocabook.service.AnuncioService;
import br.gov.sp.cps.trocabook.service.AuthService;
import br.gov.sp.cps.trocabook.service.NegociacoesService;
import br.gov.sp.cps.trocabook.ui.adapters.LivroHorizontalAdapter;

public class MeusLivrosActivity extends AppCompatActivity {

    private RecyclerView recyclerAnuncios;
    private RecyclerView recyclerNegociados;

    private LivroHorizontalAdapter<Anuncio> adapterAnuncios;
    private LivroHorizontalAdapter<Negociacao> adapterNegociados;

    private List<Anuncio> listaAnuncios = new ArrayList<>();
    private List<Negociacao> listaNegociados = new ArrayList<>();

    private AuthService authService;
    private AnuncioService anuncioService;
    private NegociacoesService negociacaoService;

    private TextView txtVazioAnuncios;
    private TextView txtVazioNegociados;
    private BottomNavigationView bottomNavigation;
    private Button btnVoltar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meuslivros);

        authService = new AuthService(this);
        anuncioService = new AnuncioService();
        negociacaoService = new NegociacoesService();

        FirebaseUser user = authService.getUsuarioAtual();

        if (user == null) {
            finish();
            return;
        }

        String userId = user.getUid();

        recyclerAnuncios = findViewById(R.id.recyclerAnuncios);
        recyclerNegociados = findViewById(R.id.recyclerNegociados);

        txtVazioAnuncios = findViewById(R.id.txtVazioAnuncios);
        txtVazioNegociados = findViewById(R.id.txtVazioNegociados);
        bottomNavigation = findViewById(R.id.bottomNavigation);
        btnVoltar = findViewById(R.id.btnVoltar);

        configurarBottomNavigation();

        recyclerAnuncios.setLayoutManager(
                new LinearLayoutManager(
                        this,
                        LinearLayoutManager.HORIZONTAL,
                        false
                )
        );

        recyclerNegociados.setLayoutManager(
                new LinearLayoutManager(
                        this,
                        LinearLayoutManager.HORIZONTAL,
                        false
                )
        );

        adapterAnuncios = new LivroHorizontalAdapter<>(
                listaAnuncios,
                anuncio -> {
                    Intent intent = new Intent(
                            MeusLivrosActivity.this,
                            DetalhesAnuncioActivity.class
                    );

                    intent.putExtra(
                            "anuncioId",
                            anuncio.getId()
                    );

                    startActivity(intent);
                }
        );

        adapterNegociados = new LivroHorizontalAdapter<>(
                listaNegociados,
                negociacao -> {
                    // Ação ao clicar em uma negociação.
                }
        );

        recyclerAnuncios.setAdapter(adapterAnuncios);
        recyclerNegociados.setAdapter(adapterNegociados);

        carregarAnuncios(userId);
        carregarNegociacoes(userId);

        btnVoltar.setOnClickListener(v -> {
            Intent intent = new Intent(
                    MeusLivrosActivity.this,
                    MainActivity.class
            );

            intent.setFlags(
                    Intent.FLAG_ACTIVITY_CLEAR_TOP |
                            Intent.FLAG_ACTIVITY_SINGLE_TOP
            );

            startActivity(intent);
        });
    }

    private void configurarBottomNavigation() {

        bottomNavigation.setSelectedItemId(R.id.menu_livros);

        bottomNavigation.setOnItemSelectedListener(item -> {

            int id = item.getItemId();

            Class<?> tela = null;

            if (id == R.id.menu_home) {

                tela = MainActivity.class;

            } else if (id == R.id.menu_anuncio) {

                tela = BuscaActivity.class;

            } else if (id == R.id.menu_livros) {

                return true;

            } else if (id == R.id.menu_chat) {

                tela = ConversasActivity.class;

            } else if (id == R.id.menu_perfil) {

                // Futura tela de perfil.
                return true;
            }

            if (tela != null) {

                Intent intent = new Intent(
                        MeusLivrosActivity.this,
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

    private void carregarAnuncios(String userId) {

        anuncioService.buscarAnunciosUsuario(
                userId,
                new AnuncioService.Callback<List<Anuncio>>() {

                    @Override
                    public void onSuccess(List<Anuncio> lista) {

                        listaAnuncios.clear();

                        if (lista != null) {
                            listaAnuncios.addAll(lista);
                        }

                        txtVazioAnuncios.setVisibility(
                                listaAnuncios.isEmpty()
                                        ? View.VISIBLE
                                        : View.GONE
                        );

                        adapterAnuncios.notifyDataSetChanged();
                    }

                    @Override
                    public void onError(String erro) {

                        Toast.makeText(
                                MeusLivrosActivity.this,
                                erro,
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                }
        );
    }

    private void carregarNegociacoes(String userId) {

        negociacaoService.buscarTodos(
                new NegociacoesService.Callback<List<Negociacao>>() {

                    @Override
                    public void onSuccess(List<Negociacao> lista) {

                        listaNegociados.clear();

                        if (lista != null) {

                            for (Negociacao negociacao : lista) {

                                if (negociacao == null) {
                                    continue;
                                }

                                boolean eAnunciante =
                                        userId.equals(
                                                negociacao.getUsuarioAnuncianteId()
                                        );

                                boolean eComprador =
                                        userId.equals(
                                                negociacao.getUsuarioCompradorId()
                                        );

                                if (eAnunciante || eComprador) {
                                    listaNegociados.add(negociacao);
                                }
                            }
                        }

                        txtVazioNegociados.setVisibility(
                                listaNegociados.isEmpty()
                                        ? View.VISIBLE
                                        : View.GONE
                        );

                        adapterNegociados.notifyDataSetChanged();
                    }

                    @Override
                    public void onError(String erro) {

                        Toast.makeText(
                                MeusLivrosActivity.this,
                                erro,
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                }
        );
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (bottomNavigation != null) {
            bottomNavigation.getMenu()
                    .findItem(R.id.menu_livros)
                    .setChecked(true);
        }
    }
}