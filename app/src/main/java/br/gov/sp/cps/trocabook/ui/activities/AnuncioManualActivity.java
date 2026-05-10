package br.gov.sp.cps.trocabook.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.telephony.mbms.StreamingServiceInfo;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.net.Uri;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

import br.gov.sp.cps.trocabook.R;
import br.gov.sp.cps.trocabook.model.Livro;
import br.gov.sp.cps.trocabook.service.AnuncioService;
import br.gov.sp.cps.trocabook.service.AuthService;
import br.gov.sp.cps.trocabook.service.FileService;
import br.gov.sp.cps.trocabook.service.LivroService;

public class AnuncioManualActivity extends AppCompatActivity {
    private TextInputEditText editTitulo, editDescricao, editAutores;
    private ImageView capa;
    private Spinner status;
    private TextView textoUpload;
    private TextInputEditText editCategorias;
    private Button btnAnunciar;
    private Uri imageUri;
    private BottomNavigationView bottomNavigation;

    private List<String> categoriasSelecionadas = new ArrayList<>();
    private List<String> categoriasDisponiveis = new ArrayList<>();

    private AnuncioService anuncioService;
    private LivroService livroService;
    private AuthService authService;
    private FileService fileService;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_anuncio_manual);
        authService = new AuthService(this);
        anuncioService = new AnuncioService();
        livroService = new LivroService();
        fileService = new FileService(this);

        if (authService.getUsuarioAtual() == null) {
            finish();
            return;
        }

        editTitulo = findViewById(R.id.editTitulo);
        editDescricao = findViewById(R.id.editDescricao);
        editAutores = findViewById(R.id.editAutores);
        btnAnunciar = findViewById(R.id.btnAnunciar);
        editCategorias = findViewById(R.id.editCategorias);
        textoUpload = findViewById(R.id.txtUpload);


        editCategorias.setOnClickListener(v -> {
            abrirDialogCategorias();
        });

        status = findViewById(R.id.spinnerStatus);
        capa = findViewById(R.id.imgCapa);
        capa.setOnClickListener(v -> {
            selecionarImagem.launch("image/*");
        });
        configurarStatus();
        configurarCategorias();

        btnAnunciar.setOnClickListener(v ->{
            if (verificarCampos()){
                salvarAnuncioBanco();
            }
        });

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





    }

    private final ActivityResultLauncher<String> selecionarImagem =
            registerForActivityResult(
                    new ActivityResultContracts.GetContent(),
                    uri -> {

                        if (uri != null) {
                            imageUri = uri;
                            capa.setImageURI(uri);
                            ViewGroup.LayoutParams params = capa.getLayoutParams();
                            params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                            params.width = ViewGroup.LayoutParams.WRAP_CONTENT;
                            capa.setLayoutParams(params);
                            textoUpload.setVisibility(View.GONE);
                        }
                    }
            );


    private void configurarStatus(){
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                new String[]{"Troca", "Venda", "Ambos"}
        );

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        status.setAdapter(adapter);
    }

    private void configurarCategorias() {

        livroService.buscarCategorias(new LivroService.Callback<List<String>>() {

            @Override
            public void onSucesso(List<String> listaCategorias) {

                listaCategorias.add("Outra categoria");

                categoriasDisponiveis = listaCategorias;
            }

            @Override
            public void onErro(Exception e) {

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
                    categoriasSelecionadas.contains(categoriasArray[i]);
        }

        androidx.appcompat.app.AlertDialog.Builder builder =
                new androidx.appcompat.app.AlertDialog.Builder(this);

        builder.setTitle("Selecione as categorias");

        builder.setMultiChoiceItems(
                categoriasArray,
                checkedItems,
                (dialog, which, isChecked) -> {

                    String categoria = categoriasArray[which];

                    if (isChecked) {

                        if (!categoriasSelecionadas.contains(categoria)
                                && !categoria.equals("Outra categoria")) {

                            categoriasSelecionadas.add(categoria);
                        }

                        if (categoria.equals("Outra categoria")) {
                            abrirDialogNovaCategoria();
                        }

                    } else {

                        categoriasSelecionadas.remove(categoria);
                    }
                }
        );

        builder.setPositiveButton("OK", (dialog, which) -> {

            editCategorias.setText(
                    android.text.TextUtils.join(
                            ", ",
                            categoriasSelecionadas
                    )
            );
        });

        builder.show();
    }

    private void abrirDialogNovaCategoria() {

        final TextInputEditText input =
                new TextInputEditText(this);

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Nova categoria")
                .setView(input)

                .setPositiveButton("Adicionar", (dialog, which) -> {

                    String novaCategoria =
                            input.getText().toString().trim();

                    if (!novaCategoria.isEmpty()) {

                        if (!categoriasDisponiveis.contains(novaCategoria)) {

                            int ultimaPosicao =
                                    categoriasDisponiveis.size() - 1;

                            categoriasDisponiveis.add(
                                    ultimaPosicao,
                                    novaCategoria
                            );
                        }

                        if (!categoriasSelecionadas.contains(novaCategoria)) {

                            categoriasSelecionadas.add(novaCategoria);
                        }

                        editCategorias.setText(
                                android.text.TextUtils.join(
                                        ", ",
                                        categoriasSelecionadas
                                )
                        );
                    }
                })

                .setNegativeButton("Cancelar", null)
                .show();
    }

    private boolean verificarCampos() {

        if (editTitulo.getText().toString().trim().isEmpty()) {
            editTitulo.setError("Digite o título");
            return false;
        }

        if (imageUri == null) {
            Toast.makeText(this, "Selecione uma imagem", Toast.LENGTH_LONG).show();
            return false;
        }

        return true;
    }

    private void criarAnuncio(Livro livro) {
        String descricaoTexto = editDescricao.getText().toString().trim();
        anuncioService.criarAnuncio(
                livro,
                descricaoTexto,
                status.getSelectedItem().toString(),
                authService.getUsuarioAtual().getUid(),

                new AnuncioService.Callback<Void>() {

                    @Override
                    public void onSuccess(Void dados) {

                        limparCampos();

                        perguntarProximoPasso();
                    }

                    @Override
                    public void onError(String mensagem) {

                        Toast.makeText(
                                AnuncioManualActivity.this,
                                mensagem,
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }
        );
    }
    private void salvarAnuncioBanco() {

        String titulo = editTitulo.getText().toString().trim();

        List<String> autores =
                java.util.Arrays.asList(
                        editAutores.getText().toString().split(",")
                );

        List<String> categoria = categoriasSelecionadas;

        livroService.salvarLivro(
                titulo,
                autores,
                categoria,
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

    private void limparCampos(){
        editTitulo.setText("");
        editDescricao.setText("");
        editAutores.setText("");
        editCategorias.setText("");
        capa.setImageDrawable(null);
        imageUri = null;
        categoriasSelecionadas.clear();
    }

    @Override
    protected void onResume() {
        super.onResume();

        bottomNavigation.getMenu()
                .findItem(R.id.menu_anuncio)
                .setChecked(true);
    }
}