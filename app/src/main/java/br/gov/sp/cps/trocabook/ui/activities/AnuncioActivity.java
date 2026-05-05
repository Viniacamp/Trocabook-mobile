package br.gov.sp.cps.trocabook.ui.activities;

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

import br.gov.sp.cps.trocabook.model.Livro;
import br.gov.sp.cps.trocabook.R;
import br.gov.sp.cps.trocabook.service.AnuncioService;

public class AnuncioActivity extends AppCompatActivity {
    private TextView titulo, autores, categorias;
    private EditText descricao;
    private Button btnAnunciar, btnVoltar;
    private Spinner status;
    private ImageView capa;
    private Livro livro;
    private AnuncioService anuncioService;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_anuncio);
        mAuth = FirebaseAuth.getInstance();

        if (mAuth.getCurrentUser() == null) {
            finish();
            return;
        }

        anuncioService = new AnuncioService();

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

    private void salvarAnuncioBanco() {

        String userId = mAuth.getCurrentUser().getUid();
        String descricaoTexto = descricao.getText().toString().trim();
        String tipo = status.getSelectedItem().toString();

        anuncioService.criarAnuncio(livro, descricaoTexto, tipo, userId,
                new AnuncioService.Callback<Void>() {
                    @Override
                    public void onSuccess(Void resultado) {

                        runOnUiThread(() -> {
                            Toast.makeText(AnuncioActivity.this,
                                    "Anúncio criado com sucesso!",
                                    Toast.LENGTH_SHORT).show();

                            perguntarProximoPasso();
                        });
                    }

                    @Override
                    public void onError(String mensagem) {
                        runOnUiThread(() ->
                                Toast.makeText(AnuncioActivity.this,
                                        "Erro: " + mensagem,
                                        Toast.LENGTH_LONG).show()
                        );
                    }
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