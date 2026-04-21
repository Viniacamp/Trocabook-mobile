package br.gov.sp.cps.trocabook;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class AnuncioActivity extends AppCompatActivity {
    private TextView titulo, autores, categorias;
    private EditText descricao;
    private Button btnAnunciar, btnVoltar;
    private Spinner status;
    private ImageView capa;
    private Livro livro;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_anuncio);
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        if (mAuth.getCurrentUser() == null) {
            finish();
            return;
        }

        livro = (Livro) getIntent().getSerializableExtra("livro");
        capa = findViewById(R.id.imgCapa);
        titulo = findViewById(R.id.txtTitulo);
        autores = findViewById(R.id.txtAutores);
        categorias = findViewById(R.id.txtCategorias);
        descricao = findViewById(R.id.txtDescricao);
        status = findViewById(R.id.spinnerStatus);
        btnAnunciar = findViewById(R.id.btnAnunciar);
        btnVoltar = findViewById(R.id.btnVoltar);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                new String[]{"Troca", "Venda", "Ambos"}
        );

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        status.setAdapter(adapter);

        carregarInformacoes(livro);

        btnAnunciar.setOnClickListener(v ->{
            if (verificarCampos()) {
                salvarAnuncioBanco();
            } else {
                Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show();
            }
        });

        btnVoltar.setOnClickListener(v ->{
            finish();
        });
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void carregarInformacoes(Livro livro){
        if (livro != null) {

            titulo.setText(livro.getTitulo());


            if (livro.getAutores() != null) {
                autores.setText("Autores: " + String.join(", ", livro.getAutores()));
            }


            if (livro.getCategorias() != null) {
                categorias.setText("Categorias: " + String.join(", ", livro.getCategorias()));
            }


            Glide.with(this)
                    .load(livro.getCapa())
                    .placeholder(R.drawable.ic_launcher_foreground)
                    .into(capa);
        }
    }

    private boolean verificarCampos(){
        return !titulo.getText().toString().isEmpty() &&
                status.getSelectedItem() != null;
    }

    private void salvarAnuncioBanco(){
        String userId = mAuth.getCurrentUser().getUid();
        String descricaoTexto = descricao.getText().toString().trim();

        if (descricaoTexto.isEmpty()) {
            descricaoTexto = "Anunciante não informou uma descrição";
        }

        Map<String, Object> anuncio = new HashMap<>();
        anuncio.put("livroId", livro.getId());
        anuncio.put("titulo", livro.getTitulo());
        anuncio.put("capa", livro.getCapa());
        anuncio.put("descricao", descricaoTexto);
        anuncio.put("tipoNegociacao", status.getSelectedItem().toString());
        anuncio.put("userId", userId);

        db.collection("anuncios")
                .add(anuncio)
                .addOnSuccessListener(doc -> {

                    Toast.makeText(this,
                            "Anúncio criado com sucesso!",
                            Toast.LENGTH_SHORT).show();

                    perguntarProximoPasso();

                })
                .addOnFailureListener(e -> {

                    Toast.makeText(this,
                            "Erro: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
    }

    private void perguntarProximoPasso() {

        Snackbar.make(findViewById(android.R.id.content),
                        "Anúncio criado!",
                        Snackbar.LENGTH_LONG)
                .setAction("Ver meus livros", v -> {
                    startActivity(new Intent(this, MeusLivrosActivity.class));
                    finish();
                })
                .show();
    }
}