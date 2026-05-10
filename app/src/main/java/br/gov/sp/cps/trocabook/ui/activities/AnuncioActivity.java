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
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;

import br.gov.sp.cps.trocabook.model.Livro;
import br.gov.sp.cps.trocabook.R;
import br.gov.sp.cps.trocabook.service.AnuncioService;
import br.gov.sp.cps.trocabook.service.AuthService;

public class AnuncioActivity extends AppCompatActivity {
    private TextView titulo, autores, categorias;
    private EditText descricao;
    private Button btnAnunciar, btnVoltar;
    private Spinner status;
    private ImageView capa;
    private Livro livro;
    private AnuncioService anuncioService;
    private AuthService authService;
    private BottomNavigationView bottomNavigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_anuncio);
        authService = new AuthService(this);

        if (authService.getUsuarioAtual() == null) {
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

        String userId = authService.getUsuarioAtual().getUid();
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

    @Override
    protected void onResume() {
        super.onResume();

        bottomNavigation.getMenu()
                .findItem(R.id.menu_anuncio)
                .setChecked(true);
    }
}