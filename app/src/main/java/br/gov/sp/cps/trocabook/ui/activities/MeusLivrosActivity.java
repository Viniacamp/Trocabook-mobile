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
import br.gov.sp.cps.trocabook.model.Livro;
import br.gov.sp.cps.trocabook.service.AnuncioService;
import br.gov.sp.cps.trocabook.service.AuthService;
import br.gov.sp.cps.trocabook.service.NegociacoesService;
import br.gov.sp.cps.trocabook.ui.adapters.LivroHorizontalAdapter;

public class MeusLivrosActivity extends AppCompatActivity {

    private RecyclerView recyclerAnuncios;
    private RecyclerView recyclerNegociados;

    private LivroHorizontalAdapter adapterAnuncios;
    private LivroHorizontalAdapter adapterNegociados;

    private List<Livro> listaAnuncios = new ArrayList<>();
    private List<Livro> listaNegociados = new ArrayList<>();

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

        bottomNavigation.setSelectedItemId(R.id.menu_livros);

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

                // futura tela chat
                return true;

            } else if (id == R.id.menu_perfil) {

                // futura tela perfil
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

        recyclerAnuncios.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        );

        recyclerNegociados.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        );

        adapterAnuncios = new LivroHorizontalAdapter(listaAnuncios);
        adapterNegociados = new LivroHorizontalAdapter(listaNegociados);

        recyclerAnuncios.setAdapter(adapterAnuncios);
        recyclerNegociados.setAdapter(adapterNegociados);

        btnVoltar = findViewById(R.id.btnVoltar);

        carregarAnuncios(userId);
        carregarNegociacoes(userId);

        btnVoltar.setOnClickListener(v ->{
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        });
    }

    private void carregarAnuncios(String userId) {

        anuncioService.buscarAnunciosUsuario(userId, new AnuncioService.Callback<List<Livro>>() {

            @Override
            public void onSuccess(List<Livro> lista) {

                listaAnuncios.clear();
                listaAnuncios.addAll(lista);

                txtVazioAnuncios.setVisibility(lista.isEmpty() ? View.VISIBLE : View.GONE);
                adapterAnuncios.notifyDataSetChanged();
            }

            @Override
            public void onError(String erro) {

            }
        });
    }

    private void carregarNegociacoes(String userId) {

        negociacaoService.buscarNegociacoesUsuario(userId, new NegociacoesService.Callback<List<Livro>>() {

            @Override
            public void onSuccess(List<Livro> lista) {

                listaNegociados.clear();
                listaNegociados.addAll(lista);

                txtVazioNegociados.setVisibility(lista.isEmpty() ? View.VISIBLE : View.GONE);
                adapterNegociados.notifyDataSetChanged();
            }

            @Override
            public void onError(String erro) {
                Toast.makeText(MeusLivrosActivity.this, erro, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        bottomNavigation.getMenu()
                .findItem(R.id.menu_livros)
                .setChecked(true);
    }
}