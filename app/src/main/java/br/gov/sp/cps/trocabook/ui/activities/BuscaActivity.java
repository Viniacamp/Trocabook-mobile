package br.gov.sp.cps.trocabook.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import br.gov.sp.cps.trocabook.model.Livro;
import br.gov.sp.cps.trocabook.service.AuthService;
import br.gov.sp.cps.trocabook.ui.adapters.LivroAdapter;
import br.gov.sp.cps.trocabook.R;
import br.gov.sp.cps.trocabook.service.LivroService;

public class BuscaActivity extends AppCompatActivity {

    private TextInputLayout textLayoutTitulo;
    private TextInputEditText textEditTitulo;
    private Button btnBuscar, btnAnunciarManualmente;
    private RecyclerView recyclerViewLivros;

    private LivroAdapter livroAdapter;
    private List<Livro> listaLivros = new ArrayList<>();

    private AuthService authService;
    private LivroService livroService;
    private BottomNavigationView bottomNavigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_busca_livro);

        authService = new AuthService(this);
        livroService = new LivroService();

        if (authService.getUsuarioAtual() == null) {
            finish();
            return;
        }

        textLayoutTitulo = findViewById(R.id.textLayoutTitulo);
        textEditTitulo = findViewById(R.id.textEditTitulo);
        btnBuscar = findViewById(R.id.btnBuscar);
        btnAnunciarManualmente = findViewById(R.id.btnAnuncioManual);
        recyclerViewLivros = findViewById(R.id.recyclerViewLivros);
        bottomNavigation = findViewById(R.id.bottomNavigation);

        bottomNavigation.setSelectedItemId(R.id.menu_anuncio);

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

        recyclerViewLivros.setLayoutManager(new LinearLayoutManager(this));

        livroAdapter = new LivroAdapter(listaLivros, livro -> {
            Intent intent = new Intent(BuscaActivity.this, AnuncioActivity.class);
            intent.putExtra("livro", (Serializable) livro);
            startActivity(intent);
        });

        recyclerViewLivros.setAdapter(livroAdapter);

        btnBuscar.setOnClickListener(v -> buscarLivro());
        btnAnunciarManualmente.setOnClickListener(v -> {
            startActivity(new Intent(this, AnuncioManualActivity.class));
            finish();
        });
    }

    private void buscarLivro() {

        String titulo = textEditTitulo.getText().toString().trim();

        if (titulo.isEmpty()) {
            textLayoutTitulo.setError("Digite um título");
            return;
        }

        textLayoutTitulo.setError(null);

        livroService.buscarLivros(titulo, new LivroService.Callback<List<Livro>>() {
            @Override
            public void onSucesso(List<Livro> livros) {
                runOnUiThread(() -> atualizarLista(livros));
            }

            @Override
            public void onErro(Exception e) {
                runOnUiThread(() ->
                        Log.e("BUSCA", "Erro ao buscar livros", e)
                );
            }
        });
    }

    private void atualizarLista(List<Livro> livros) {
        listaLivros.clear();
        listaLivros.addAll(livros);
        livroAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onResume() {
        super.onResume();

        bottomNavigation.getMenu()
                .findItem(R.id.menu_anuncio)
                .setChecked(true);
    }
}