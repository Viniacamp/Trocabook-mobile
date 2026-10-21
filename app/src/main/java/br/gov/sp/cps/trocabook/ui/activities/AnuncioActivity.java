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

import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

import br.gov.sp.cps.trocabook.R;
import br.gov.sp.cps.trocabook.model.Autor;
import br.gov.sp.cps.trocabook.model.Categoria;
import br.gov.sp.cps.trocabook.model.Livro;
import br.gov.sp.cps.trocabook.service.AnuncioService;
import br.gov.sp.cps.trocabook.service.AuthService;
import br.gov.sp.cps.trocabook.service.AutorService;
import br.gov.sp.cps.trocabook.service.CategoriaService;

public class AnuncioActivity extends AppCompatActivity {

    private TextView titulo;
    private TextView autores;
    private TextView categorias;

    private EditText descricao;

    private Button btnAnunciar;
    private Button btnVoltar;

    private Spinner spinnerTipoNegocicacao;

    private ImageView capa;

    private Livro livro;

    private AnuncioService anuncioService;
    private AutorService autorService;
    private CategoriaService categoriaService;
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
        autorService = new AutorService();
        categoriaService = new CategoriaService();

        livro = (Livro) getIntent().getSerializableExtra("livro");

        if (livro == null) {
            Toast.makeText(
                    this,
                    "Não foi possível carregar o livro.",
                    Toast.LENGTH_LONG
            ).show();

            finish();
            return;
        }

        inicializarViews();
        configurarBottomNavigation();
        configurarSpinner();
        carregarInformacoes(livro);
        configurarBotoes();
    }

    private void inicializarViews() {

        capa = findViewById(R.id.imgCapa);

        titulo = findViewById(R.id.txtTitulo);
        autores = findViewById(R.id.txtAutores);
        categorias = findViewById(R.id.txtCategorias);

        descricao = findViewById(R.id.txtDescricao);

        spinnerTipoNegocicacao = findViewById(R.id.spinnerStatus);

        btnAnunciar = findViewById(R.id.btnAnunciar);
        btnVoltar = findViewById(R.id.btnVoltar);

        bottomNavigation = findViewById(R.id.bottomNavigation);
    }

    private void configurarBottomNavigation() {

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

                tela = ConversasActivity.class;

            } else if (id == R.id.menu_perfil) {

                // futura tela de perfil
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
    }

    private void configurarSpinner() {

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                new String[]{
                        "Troca",
                        "Venda",
                        "Ambos"
                }
        );

        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        spinnerTipoNegocicacao.setAdapter(adapter);
    }

    private void carregarInformacoes(Livro livro) {

        titulo.setText(livro.getTitulo());

        Glide.with(this)
                .load(livro.getUrlImagem())
                .placeholder(R.drawable.placeholder_capa)
                .into(capa);

        carregarAutores(livro.getIdsAutores());
        carregarCategorias(livro.getIdsCategorias());
    }

    private void carregarAutores(List<String> idsAutores) {

        if (idsAutores == null || idsAutores.isEmpty()) {

            autores.setText("Autores: Não informado");
            return;
        }

        List<String> nomesAutores = new ArrayList<>();

        buscarAutorRecursivamente(
                idsAutores,
                0,
                nomesAutores
        );
    }

    private void buscarAutorRecursivamente(
            List<String> idsAutores,
            int indice,
            List<String> nomesAutores) {

        if (indice >= idsAutores.size()) {

            if (nomesAutores.isEmpty()) {
                autores.setText("Autores: Não informado");
            } else {
                autores.setText(
                        "Autores: " + String.join(
                                ", ",
                                nomesAutores
                        )
                );
            }

            return;
        }

        String idAutor = idsAutores.get(indice);

        autorService.buscarAutorPorId(
                idAutor,
                new AutorService.Callback<Autor>() {

                    @Override
                    public void onSucesso(Autor autor) {

                        if (autor != null &&
                                autor.getNome() != null &&
                                !autor.getNome().trim().isEmpty()) {

                            nomesAutores.add(autor.getNome());
                        }

                        buscarAutorRecursivamente(
                                idsAutores,
                                indice + 1,
                                nomesAutores
                        );
                    }

                    @Override
                    public void onErro(Exception e) {

                        buscarAutorRecursivamente(
                                idsAutores,
                                indice + 1,
                                nomesAutores
                        );
                    }
                }
        );
    }

    private void carregarCategorias(List<String> idsCategorias) {

        if (idsCategorias == null || idsCategorias.isEmpty()) {

            categorias.setText("Categorias: Não informado");
            return;
        }

        List<String> nomesCategorias = new ArrayList<>();

        buscarCategoriaRecursivamente(
                idsCategorias,
                0,
                nomesCategorias
        );
    }

    private void buscarCategoriaRecursivamente(
            List<String> idsCategorias,
            int indice,
            List<String> nomesCategorias) {

        if (indice >= idsCategorias.size()) {

            if (nomesCategorias.isEmpty()) {
                categorias.setText("Categorias: Não informado");
            } else {
                categorias.setText(
                        "Categorias: " + String.join(
                                ", ",
                                nomesCategorias
                        )
                );
            }

            return;
        }

        String idCategoria = idsCategorias.get(indice);

        categoriaService.buscarCategoriaPorId(
                idCategoria,
                new CategoriaService.Callback<Categoria>() {

                    @Override
                    public void onSucesso(Categoria categoria) {

                        if (categoria != null &&
                                categoria.getNome() != null &&
                                !categoria.getNome().trim().isEmpty()) {

                            nomesCategorias.add(
                                    categoria.getNome()
                            );
                        }

                        buscarCategoriaRecursivamente(
                                idsCategorias,
                                indice + 1,
                                nomesCategorias
                        );
                    }

                    @Override
                    public void onErro(Exception e) {

                        buscarCategoriaRecursivamente(
                                idsCategorias,
                                indice + 1,
                                nomesCategorias
                        );
                    }
                }
        );
    }

    private void configurarBotoes() {

        btnAnunciar.setOnClickListener(v -> {

            if (verificarCampos()) {

                salvarAnuncioBanco();

            } else {

                Toast.makeText(
                        this,
                        "Preencha todos os campos",
                        Toast.LENGTH_SHORT
                ).show();
            }
        });

        btnVoltar.setOnClickListener(v -> finish());
    }

    private boolean verificarCampos() {

        return livro != null &&
                livro.getTitulo() != null &&
                !livro.getTitulo().trim().isEmpty() &&
                spinnerTipoNegocicacao.getSelectedItem() != null;
    }

    private void salvarAnuncioBanco() {

        String userId =
                authService.getUsuarioAtual().getUid();

        String descricaoTexto =
                descricao.getText()
                        .toString()
                        .trim();

        String tipo =
                spinnerTipoNegocicacao.getSelectedItem()
                        .toString();

        anuncioService.criarAnuncio(
                livro,
                descricaoTexto,
                tipo,
                userId,
                new AnuncioService.Callback<br.gov.sp.cps.trocabook.model.Anuncio>() {

                    @Override
                    public void onSuccess(
                            br.gov.sp.cps.trocabook.model.Anuncio anuncio) {

                        runOnUiThread(() -> {

                            Toast.makeText(
                                    AnuncioActivity.this,
                                    "Anúncio criado com sucesso!",
                                    Toast.LENGTH_SHORT
                            ).show();

                            perguntarProximoPasso();
                        });
                    }

                    @Override
                    public void onError(String mensagem) {

                        runOnUiThread(() ->
                                Toast.makeText(
                                        AnuncioActivity.this,
                                        "Erro: " + mensagem,
                                        Toast.LENGTH_LONG
                                ).show()
                        );
                    }
                }
        );
    }

    private void perguntarProximoPasso() {

        Snackbar.make(
                        findViewById(android.R.id.content),
                        "Anúncio criado!",
                        Snackbar.LENGTH_LONG
                )
                .setAction(
                        "Ver meus livros",
                        v -> {

                            startActivity(
                                    new Intent(
                                            this,
                                            MeusLivrosActivity.class
                                    )
                            );

                            finish();
                        }
                )
                .show();
    }

    @Override
    protected void onResume() {

        super.onResume();

        if (bottomNavigation != null) {

            bottomNavigation.getMenu()
                    .findItem(R.id.menu_anuncio)
                    .setChecked(true);
        }
    }
}