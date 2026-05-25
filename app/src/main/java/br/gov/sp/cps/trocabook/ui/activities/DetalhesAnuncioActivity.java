package br.gov.sp.cps.trocabook.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseUser;

import br.gov.sp.cps.trocabook.R;
import br.gov.sp.cps.trocabook.model.Anuncio;
import br.gov.sp.cps.trocabook.model.Usuario;
import br.gov.sp.cps.trocabook.service.AnuncioService;
import br.gov.sp.cps.trocabook.service.AuthService;
import br.gov.sp.cps.trocabook.service.UsuarioService;

public class DetalhesAnuncioActivity extends AppCompatActivity {

    private ImageView imageCapa;
    private ImageView imageUsuario;

    private TextView textTitulo;
    private TextView textDescricao;
    private TextView textUsuario;
    private TextView textTipoNegociacao;

    private Button buttonNegociar;
    private AuthService authService;
    private AnuncioService anuncioService;
    private UsuarioService usuarioService;
    private BottomNavigationView bottomNavigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_detalhes_anuncio);
        bottomNavigation = findViewById(R.id.bottomNavigation);
        inicializarComponentes();
        FirebaseUser user = authService.getUsuarioAtual();
        if (user == null) {
            finish();
            return;
        }

        configurarMenu();
        String anuncioId = getIntent().getStringExtra("anuncioId");
        anuncioService.buscarAnuncioPorId(anuncioId, new AnuncioService.Callback<Anuncio>() {
            @Override
            public void onSuccess(Anuncio dados) {
                configurarUsuario(dados.getUserId());
                configurarInformacoesLivro(dados.getCapa(), dados.getTitulo(), dados.getDescricao(), dados.getTipoNegociacao());
                buttonNegociar.setOnClickListener(v -> {
                    Intent intent = new Intent(DetalhesAnuncioActivity.this, ChatActivity.class);
                    intent.putExtra("anuncioId", anuncioId);
                    intent.putExtra("userDestinatario", dados.getUserId());
                    startActivity(intent);
                    finish();
                });
            }

            @Override
            public void onError(String mensagem) {

            }
        });
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void configurarUsuario(String userId){
        usuarioService.buscarUsuarioPorId(userId, new UsuarioService.ServiceCallback<Usuario>() {
            @Override
            public void onSuccess(Usuario usuario) {
                textUsuario.setText(usuario.getEmail_principal()); // por enquanto email, depois sera nome
                System.out.println(usuario.getEmail_principal());
                Glide.with(DetalhesAnuncioActivity.this)
                        .load(usuario.getFoto_perfil())
                        .placeholder(R.drawable.placeholder_usuario)
                        .into(imageUsuario);
            }
            @Override
            public void onError(String erro) {
                textUsuario.setText("Usuário não encontrado");
            }
        });
    }

    private void configurarInformacoesLivro(String capa, String titulo, String descricao, String tipoNegociacao){
        System.out.println(capa);
        System.out.println(titulo);
        System.out.println(descricao);
        System.out.println(tipoNegociacao);
        Glide.with(this)
                .load(capa)
                .placeholder(R.drawable.placeholder_capa)
                .into(imageCapa);
        textTitulo.setText(titulo);
        textDescricao.setText(descricao);
        textTipoNegociacao.setText("Tipo: " + tipoNegociacao);
    }

    private void inicializarComponentes(){
        imageCapa = findViewById(R.id.imageCapa);
        imageUsuario = findViewById(R.id.imageUsuario);
        textTitulo = findViewById(R.id.textTitulo);
        textDescricao = findViewById(R.id.textDescricao);
        textUsuario = findViewById(R.id.textUsuario);
        textTipoNegociacao = findViewById(R.id.textTipoNegociacao);
        buttonNegociar = findViewById(R.id.buttonNegociar);
        authService = new AuthService(this);
        anuncioService = new AnuncioService();
        usuarioService = new UsuarioService();

    }

    private void configurarMenu(){
        bottomNavigation.setSelectedItemId(R.id.menu_home);

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

    @Override
    protected void onResume() {

        super.onResume();

        bottomNavigation.getMenu()
                .findItem(R.id.menu_home)
                .setChecked(true);
    }
}