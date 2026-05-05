package br.gov.sp.cps.trocabook.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import br.gov.sp.cps.trocabook.model.Livro;
import br.gov.sp.cps.trocabook.ui.adapters.LivroAdapter;
import br.gov.sp.cps.trocabook.R;
import br.gov.sp.cps.trocabook.service.LivroService;

public class BuscaActivity extends AppCompatActivity {

    private TextInputLayout textLayoutTitulo;
    private TextInputEditText textEditTitulo;
    private Button btnBuscar;
    private RecyclerView recyclerViewLivros;

    private LivroAdapter livroAdapter;
    private List<Livro> listaLivros = new ArrayList<>();

    private FirebaseAuth mAuth;
    private LivroService livroService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_busca_livro);

        mAuth = FirebaseAuth.getInstance();
        livroService = new LivroService();

        if (mAuth.getCurrentUser() == null) {
            finish();
            return;
        }

        textLayoutTitulo = findViewById(R.id.textLayoutTitulo);
        textEditTitulo = findViewById(R.id.textEditTitulo);
        btnBuscar = findViewById(R.id.btnBuscar);
        recyclerViewLivros = findViewById(R.id.recyclerViewLivros);

        recyclerViewLivros.setLayoutManager(new LinearLayoutManager(this));

        livroAdapter = new LivroAdapter(listaLivros, livro -> {
            Intent intent = new Intent(BuscaActivity.this, AnuncioActivity.class);
            intent.putExtra("livro", (Serializable) livro);
            startActivity(intent);
        });

        recyclerViewLivros.setAdapter(livroAdapter);

        btnBuscar.setOnClickListener(v -> buscarLivro());
    }

    private void buscarLivro() {

        String titulo = textEditTitulo.getText().toString().trim();

        if (titulo.isEmpty()) {
            textLayoutTitulo.setError("Digite um título");
            return;
        }

        textLayoutTitulo.setError(null);

        livroService.buscarLivros(titulo, new LivroService.Callback() {
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
}