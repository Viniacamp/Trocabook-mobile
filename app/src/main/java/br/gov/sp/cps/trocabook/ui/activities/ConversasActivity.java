package br.gov.sp.cps.trocabook.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

import br.gov.sp.cps.trocabook.R;
import br.gov.sp.cps.trocabook.model.Usuario;
import br.gov.sp.cps.trocabook.model.dto.ConversaDTO;
import br.gov.sp.cps.trocabook.model.dto.MensagemDTO;
import br.gov.sp.cps.trocabook.service.AuthService;
import br.gov.sp.cps.trocabook.service.UsuarioService;
import br.gov.sp.cps.trocabook.service.chat.ChatService;
import br.gov.sp.cps.trocabook.ui.adapters.ConversaAdapter;

public class ConversasActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigation;

    private RecyclerView rvConversas;

    private LinearLayout layoutSemConversas;

    private CardView layoutLoading;

    private ImageView imgLoading;

    private ConversaAdapter adapter;

    private List<ConversaDTO> listaConversas;

    private ChatService chatService;

    private UsuarioService usuarioService;

    private AuthService authService;

    private String uidUsuarioLogado;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);

        setContentView(R.layout.activity_conversas);

        bottomNavigation = findViewById(R.id.bottomNavigation);

        inicializarComponentes();

        FirebaseUser user =
                authService.getUsuarioAtual();

        if (user == null) {

            finish();

            return;
        }

        uidUsuarioLogado = user.getUid();

        configurarMenu();

        configurarRecycler();

        carregarConversas();
    }

    private void inicializarComponentes() {

        rvConversas =
                findViewById(R.id.recyclerConversas);

        layoutSemConversas =
                findViewById(R.id.layoutSemConversas);

        layoutLoading =
                findViewById(R.id.layoutLoading);

        imgLoading =
                findViewById(R.id.imgLoading);

        listaConversas =
                new ArrayList<>();

        chatService =
                new ChatService();

        usuarioService =
                new UsuarioService();

        authService =
                new AuthService(this);
    }

    private void configurarRecycler() {

        adapter = new ConversaAdapter(
                listaConversas,
                uidUsuarioLogado,
                conversa -> {

                    Intent intent =
                            new Intent(
                                    ConversasActivity.this,
                                    ChatActivity.class
                            );

                    intent.putExtra(
                            "anuncioId",
                            conversa.getUltimaMensagem()
                                    .getUidAnuncio()
                    );

                    intent.putExtra(
                            "userDestinatario",
                            conversa.getOutroUsuarioId()
                    );

                    startActivity(intent);
                }
        );

        rvConversas.setLayoutManager(
                new LinearLayoutManager(this)
        );

        rvConversas.setAdapter(adapter);
    }

    private void carregarConversas() {

        mostrarLoading();

        chatService.buscarMensagens(
                uidUsuarioLogado,
                null,
                null,
                new ChatService.Callback<List<MensagemDTO>>() {

                    @Override
                    public void onSuccess(
                            List<MensagemDTO> mensagens
                    ) {

                        runOnUiThread(() -> {

                            listaConversas.clear();

                            if (mensagens.isEmpty()) {

                                esconderLoading();

                                layoutSemConversas
                                        .setVisibility(View.VISIBLE);

                                rvConversas
                                        .setVisibility(View.GONE);

                                return;
                            }

                            layoutSemConversas
                                    .setVisibility(View.GONE);

                            rvConversas
                                    .setVisibility(View.VISIBLE);

                            final int totalMensagens =
                                    mensagens.size();

                            final int[] carregadas = {0};

                            for (MensagemDTO mensagem : mensagens) {

                                String outroUsuarioId;

                                if (mensagem.getUidRemetente()
                                        .equals(uidUsuarioLogado)) {

                                    outroUsuarioId =
                                            mensagem.getUidDestinatario();

                                } else {

                                    outroUsuarioId =
                                            mensagem.getUidRemetente();
                                }

                                usuarioService.buscarUsuarioPorId(
                                        outroUsuarioId,
                                        new UsuarioService.ServiceCallback<Usuario>() {

                                            @Override
                                            public void onSuccess(
                                                    Usuario usuario
                                            ) {

                                                ConversaDTO conversa =
                                                        new ConversaDTO();

                                                conversa.setUsuario(usuario);

                                                conversa.setOutroUsuarioId(
                                                        outroUsuarioId
                                                );

                                                conversa.setUltimaMensagem(
                                                        mensagem
                                                );

                                                listaConversas.add(conversa);

                                                adapter.notifyDataSetChanged();

                                                carregadas[0]++;

                                                if (carregadas[0]
                                                        == totalMensagens) {

                                                    esconderLoading();
                                                }
                                            }

                                            @Override
                                            public void onError(String erro) {

                                                carregadas[0]++;

                                                if (carregadas[0]
                                                        == totalMensagens) {

                                                    esconderLoading();
                                                }
                                            }
                                        }
                                );
                            }
                        });
                    }

                    @Override
                    public void onError(String erro) {

                        runOnUiThread(() -> {

                            esconderLoading();

                            layoutSemConversas
                                    .setVisibility(View.VISIBLE);

                            rvConversas
                                    .setVisibility(View.GONE);
                        });
                    }
                }
        );
    }

    private void mostrarLoading() {

        layoutLoading.setVisibility(View.VISIBLE);

        Animation animation = AnimationUtils.loadAnimation(
                this,
                R.anim.rotacao_loading
        );

        imgLoading.startAnimation(animation);
    }

    private void esconderLoading() {

        imgLoading.clearAnimation();

        layoutLoading.setVisibility(View.GONE);
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