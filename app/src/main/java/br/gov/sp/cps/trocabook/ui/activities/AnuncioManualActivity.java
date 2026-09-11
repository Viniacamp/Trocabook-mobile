package br.gov.sp.cps.trocabook.ui.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import br.gov.sp.cps.trocabook.R;
import br.gov.sp.cps.trocabook.model.Anuncio;
import br.gov.sp.cps.trocabook.model.Categoria;
import br.gov.sp.cps.trocabook.model.Livro;
import br.gov.sp.cps.trocabook.service.AnuncioService;
import br.gov.sp.cps.trocabook.service.AuthService;
import br.gov.sp.cps.trocabook.service.CategoriaService;
import br.gov.sp.cps.trocabook.service.FileService;
import br.gov.sp.cps.trocabook.service.LivroService;

public class AnuncioManualActivity extends AppCompatActivity {

    private TextInputEditText editTitulo;
    private TextInputEditText editDescricao;
    private TextInputEditText editAutores;
    private TextInputEditText editCategorias;

    private ImageView capa;

    private Spinner status;

    private TextView textoUpload;

    private Button btnAnunciar;

    private Uri imageUri;

    private BottomNavigationView bottomNavigation;

    private List<String> categoriasSelecionadas = new ArrayList<>();
    private List<String> categoriasDisponiveis = new ArrayList<>();

    private AnuncioService anuncioService;
    private LivroService livroService;
    private CategoriaService categoriaService;
    private AuthService authService;
    private FileService fileService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_anuncio_manual);

        authService = new AuthService(this);

        if (authService.getUsuarioAtual() == null) {
            finish();
            return;
        }

        anuncioService = new AnuncioService();
        livroService = new LivroService();
        categoriaService = new CategoriaService();
        fileService = new FileService(this);

        inicializarViews();
        configurarStatus();
        configurarCategorias();
        configurarImagem();
        configurarBotoes();
        configurarBottomNavigation();
    }

    private void inicializarViews() {

        editTitulo = findViewById(R.id.editTitulo);
        editDescricao = findViewById(R.id.editDescricao);
        editAutores = findViewById(R.id.editAutores);
        editCategorias = findViewById(R.id.editCategorias);

        btnAnunciar = findViewById(R.id.btnAnunciar);

        textoUpload = findViewById(R.id.txtUpload);

        status = findViewById(R.id.spinnerStatus);

        capa = findViewById(R.id.imgCapa);

        bottomNavigation = findViewById(R.id.bottomNavigation);
    }

    private void configurarStatus() {

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

        status.setAdapter(adapter);
    }

    private void configurarCategorias() {

        categoriaService.buscarTodas(
                new CategoriaService.Callback<List<Categoria>>() {

                    @Override
                    public void onSucesso(List<Categoria> categorias) {

                        categoriasDisponiveis.clear();

                        if (categorias != null) {

                            for (Categoria categoria : categorias) {

                                if (categoria != null &&
                                        categoria.getNome() != null &&
                                        !categoria.getNome().trim().isEmpty()) {

                                    categoriasDisponiveis.add(
                                            categoria.getNome()
                                    );
                                }
                            }
                        }

                        categoriasDisponiveis.add("Outra categoria");
                    }

                    @Override
                    public void onErro(Exception e) {

                        Toast.makeText(
                                AnuncioManualActivity.this,
                                "Erro ao carregar categorias",
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }
        );
    }

    private void configurarImagem() {

        capa.setOnClickListener(
                v -> selecionarImagem.launch("image/*")
        );
    }

    private final ActivityResultLauncher<String> selecionarImagem =
            registerForActivityResult(
                    new ActivityResultContracts.GetContent(),
                    uri -> {

                        if (uri != null) {

                            imageUri = uri;

                            capa.setImageURI(uri);

                            capa.setScaleType(
                                    ImageView.ScaleType.CENTER_CROP
                            );

                            textoUpload.setVisibility(View.GONE);
                        }
                    }
            );

    private void configurarBotoes() {

        editCategorias.setOnClickListener(
                v -> abrirDialogCategorias()
        );

        btnAnunciar.setOnClickListener(v -> {

            if (verificarCampos()) {
                salvarAnuncioBanco();
            }
        });
    }

    private void abrirDialogCategorias() {

        String[] categoriasArray =
                categoriasDisponiveis.toArray(new String[0]);

        boolean[] checkedItems =
                new boolean[categoriasArray.length];

        for (int i = 0; i < categoriasArray.length; i++) {

            checkedItems[i] =
                    categoriasSelecionadas.contains(
                            categoriasArray[i]
                    );
        }

        androidx.appcompat.app.AlertDialog.Builder builder =
                new androidx.appcompat.app.AlertDialog.Builder(this);

        builder.setTitle("Selecione as categorias");

        builder.setMultiChoiceItems(
                categoriasArray,
                checkedItems,
                (dialog, which, isChecked) -> {

                    String categoria =
                            categoriasArray[which];

                    if (isChecked) {

                        if (categoria.equals("Outra categoria")) {

                            abrirDialogNovaCategoria();

                        } else if (!categoriasSelecionadas.contains(
                                categoria
                        )) {

                            categoriasSelecionadas.add(
                                    categoria
                            );
                        }

                    } else {

                        categoriasSelecionadas.remove(
                                categoria
                        );
                    }
                }
        );

        builder.setPositiveButton(
                "OK",
                (dialog, which) -> {

                    editCategorias.setText(
                            android.text.TextUtils.join(
                                    ", ",
                                    categoriasSelecionadas
                            )
                    );
                }
        );

        builder.show();
    }

    private void abrirDialogNovaCategoria() {

        final TextInputEditText input =
                new TextInputEditText(this);

        input.setHint("Nome da categoria");

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Nova categoria")
                .setView(input)
                .setPositiveButton(
                        "Adicionar",
                        (dialog, which) -> {

                            String novaCategoria =
                                    input.getText()
                                            .toString()
                                            .trim();

                            if (novaCategoria.isEmpty()) {
                                return;
                            }

                            if (!categoriasDisponiveis.contains(
                                    novaCategoria
                            )) {

                                int ultimaPosicao =
                                        categoriasDisponiveis.size() - 1;

                                categoriasDisponiveis.add(
                                        ultimaPosicao,
                                        novaCategoria
                                );
                            }

                            if (!categoriasSelecionadas.contains(
                                    novaCategoria
                            )) {

                                categoriasSelecionadas.add(
                                        novaCategoria
                                );
                            }

                            editCategorias.setText(
                                    android.text.TextUtils.join(
                                            ", ",
                                            categoriasSelecionadas
                                    )
                            );
                        }
                )
                .setNegativeButton(
                        "Cancelar",
                        null
                )
                .show();
    }

    private boolean verificarCampos() {

        if (editTitulo.getText() == null ||
                editTitulo.getText()
                        .toString()
                        .trim()
                        .isEmpty()) {

            editTitulo.setError("Digite o título");
            return false;
        }

        if (editAutores.getText() == null ||
                editAutores.getText()
                        .toString()
                        .trim()
                        .isEmpty()) {

            editAutores.setError("Digite pelo menos um autor");
            return false;
        }

        if (categoriasSelecionadas.isEmpty()) {

            Toast.makeText(
                    this,
                    "Selecione pelo menos uma categoria",
                    Toast.LENGTH_LONG
            ).show();

            return false;
        }

        if (imageUri == null) {

            Toast.makeText(
                    this,
                    "Selecione uma imagem",
                    Toast.LENGTH_LONG
            ).show();

            return false;
        }

        return true;
    }

    private void salvarAnuncioBanco() {

        String titulo =
                editTitulo.getText()
                        .toString()
                        .trim();

        List<String> autores =
                Arrays.asList(
                        editAutores.getText()
                                .toString()
                                .split(",")
                );

        List<String> categorias =
                new ArrayList<>(
                        categoriasSelecionadas
                );

        livroService.salvarLivro(
                titulo,
                autores,
                categorias,
                imageUri,
                fileService,
                new LivroService.Callback<Livro>() {

                    @Override
                    public void onSucesso(Livro livro) {

                        criarAnuncio(livro);
                    }

                    @Override
                    public void onErro(Exception e) {

                        Toast.makeText(
                                AnuncioManualActivity.this,
                                "Erro ao salvar livro",
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }
        );
    }

    private void criarAnuncio(Livro livro) {

        String descricaoTexto =
                editDescricao.getText()
                        .toString()
                        .trim();

        String tipo =
                status.getSelectedItem()
                        .toString();

        String uidUsuario =
                authService.getUsuarioAtual()
                        .getUid();

        anuncioService.criarAnuncio(
                livro,
                descricaoTexto,
                tipo,
                uidUsuario,
                new AnuncioService.Callback<Anuncio>() {

                    @Override
                    public void onSuccess(Anuncio anuncio) {

                        runOnUiThread(() -> {

                            limparCampos();

                            perguntarProximoPasso();
                        });
                    }

                    @Override
                    public void onError(String mensagem) {

                        runOnUiThread(() ->
                                Toast.makeText(
                                        AnuncioManualActivity.this,
                                        mensagem,
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

    private void limparCampos() {

        editTitulo.setText("");
        editDescricao.setText("");
        editAutores.setText("");
        editCategorias.setText("");

        capa.setImageResource(
                android.R.drawable.ic_menu_upload
        );

        capa.setScaleType(
                ImageView.ScaleType.CENTER_INSIDE
        );

        imageUri = null;

        categoriasSelecionadas.clear();

        textoUpload.setVisibility(
                View.VISIBLE
        );
    }

    private void configurarBottomNavigation() {

        bottomNavigation.setSelectedItemId(
                R.id.menu_anuncio
        );

        bottomNavigation.setOnItemSelectedListener(
                item -> {

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
                }
        );
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