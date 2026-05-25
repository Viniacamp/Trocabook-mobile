package br.gov.sp.cps.trocabook.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

import br.gov.sp.cps.trocabook.R;
import br.gov.sp.cps.trocabook.model.Usuario;
import br.gov.sp.cps.trocabook.model.dto.MensagemDTO;
import br.gov.sp.cps.trocabook.service.AuthService;
import br.gov.sp.cps.trocabook.service.UsuarioService;
import br.gov.sp.cps.trocabook.service.chat.ChatService;
import br.gov.sp.cps.trocabook.ui.adapters.ChatAdapter;

public class ChatActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigation;

    private RecyclerView rvMensagens;

    private EditText editMensagem;

    private FloatingActionButton btnEnviarMensagem;

    private ChatService chatService;

    private AuthService authService;
    private UsuarioService usuarioService;

    private ChatAdapter adapter;

    private List<MensagemDTO> listaMensagens;

    private String anuncioId;

    private String userDestinatario;

    private String userRemetente;

    private ImageView imgUsuarioChat;
    private TextView txtNomeUsuarioChat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);

        setContentView(R.layout.activity_chat);
        bottomNavigation = findViewById(R.id.bottomNavigation);
        inicializarComponentes();
        FirebaseUser user = authService.getUsuarioAtual();
        if (user == null) {
            finish();
            return;
        }
        configurarMenu();

        receberDadosIntent();

        carregarUsuarioChat();

        configurarRecycler();

        carregarMensagens();

        configurarEnvioMensagem();

    }

    private void inicializarComponentes() {

        bottomNavigation = findViewById(R.id.bottomNavigation);

        rvMensagens = findViewById(R.id.rvMensagens);

        imgUsuarioChat = findViewById(R.id.imgUsuarioChat);

        txtNomeUsuarioChat = findViewById(R.id.txtNomeUsuarioChat);

        editMensagem = findViewById(R.id.editMensagem);

        btnEnviarMensagem = findViewById(R.id.btnEnviarMensagem);

        chatService = new ChatService();

        authService = new AuthService(this);

        listaMensagens = new ArrayList<>();

        usuarioService = new UsuarioService();
    }

    private void carregarUsuarioChat() {

        usuarioService.buscarUsuarioPorId(
                userDestinatario,
                new UsuarioService.ServiceCallback<Usuario>() {

                    @Override
                    public void onSuccess(Usuario usuario) {

                        txtNomeUsuarioChat.setText(
                                usuario.getEmail_principal()
                        );

                        Glide.with(ChatActivity.this)
                                .load(usuario.getFoto_perfil())
                                .placeholder(
                                        R.drawable.placeholder_usuario
                                )
                                .into(imgUsuarioChat);
                    }

                    @Override
                    public void onError(String erro) {

                        txtNomeUsuarioChat.setText(
                                "Usuário"
                        );
                    }
                }
        );
    }

    private void receberDadosIntent() {

        anuncioId = getIntent().getStringExtra("anuncioId");

        userDestinatario =
                getIntent().getStringExtra("userDestinatario");

        userRemetente =
                authService.getUsuarioAtual().getUid();
    }

    private void configurarRecycler() {

        adapter = new ChatAdapter(
                listaMensagens,
                userRemetente
        );

        rvMensagens.setLayoutManager(
                new LinearLayoutManager(this)
        );

        rvMensagens.setAdapter(adapter);
    }

    private void carregarMensagens() {

        chatService.buscarMensagens(
                userRemetente,
                userDestinatario,
                anuncioId,
                new ChatService.Callback<List<MensagemDTO>>() {

                    @Override
                    public void onSuccess(List<MensagemDTO> dados) {

                        listaMensagens.clear();

                        listaMensagens.addAll(dados);

                        adapter.notifyDataSetChanged();

                        rvMensagens.scrollToPosition(
                                listaMensagens.size() - 1
                        );
                    }

                    @Override
                    public void onError(String erro) {

                    }
                }
        );
    }

    private void configurarEnvioMensagem() {

        btnEnviarMensagem.setOnClickListener(v -> {

            String conteudo =
                    editMensagem.getText().toString().trim();

            if (conteudo.isEmpty()) {
                return;
            }

            MensagemDTO mensagem = new MensagemDTO();

            mensagem.setUidRemetente(userRemetente);

            mensagem.setUidDestinatario(userDestinatario);

            mensagem.setUidAnuncio(anuncioId);

            mensagem.setConteudo(conteudo);

            chatService.salvarMensagem(
                    mensagem,
                    new ChatService.Callback<MensagemDTO>() {

                        @Override
                        public void onSuccess(MensagemDTO dados) {

                            editMensagem.setText("");

                            carregarMensagens();
                        }

                        @Override
                        public void onError(String erro) {

                        }
                    }
            );
        });
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
                .findItem(R.id.menu_chat)
                .setChecked(true);
    }
}