package br.gov.sp.cps.trocabook.service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.Person;
import androidx.core.graphics.drawable.IconCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import br.gov.sp.cps.trocabook.R;
import br.gov.sp.cps.trocabook.model.Usuario;
import br.gov.sp.cps.trocabook.ui.activities.ChatActivity;
import br.gov.sp.cps.trocabook.util.ChatState;
import br.gov.sp.cps.trocabook.util.ImageUtils;
import br.gov.sp.cps.trocabook.util.SvgLoader;

public class MeuFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "FCM_DEBUG";
    public static final String ACTION_ATUALIZAR_CHAT = "br.gov.sp.cps.trocabook.ATUALIZAR_CHAT";
    private static final String CHANNEL_ID = "chat_mensagens_channel";

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        Log.d(TAG, "Novo token FCM gerado: " + token);
        enviarTokenParaServidor(token);
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        if (!remoteMessage.getData().isEmpty()) {
            String tipoEvento = remoteMessage.getData().get("tipoEvento");
            String tipo = remoteMessage.getData().get("tipo");
            String uidAnuncio = remoteMessage.getData().get("uidAnuncio");
            String uidRemetente = remoteMessage.getData().get("uidRemetente");
            String uidDestinatario = remoteMessage.getData().get("uidDestinatario");

            // 1. Tratamento para eventos silenciosos (edição ou exclusão de mensagens)
            if (tipoEvento != null) {
                Intent intent = new Intent(ACTION_ATUALIZAR_CHAT);
                intent.putExtra("tipoEvento", tipoEvento);
                intent.putExtra("uidAnuncio", uidAnuncio);
                intent.putExtra("uidRemetente", uidRemetente);
                intent.putExtra("uidDestinatario", uidDestinatario);

                LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
                return;
            }

            // 2. Tratamento para novas mensagens (com exibição de banner visual rico estilo WhatsApp)
            if ("NOVA_MENSAGEM".equals(tipo) || remoteMessage.getData().containsKey("corpo")) {
                String corpoMensagem = remoteMessage.getData().get("corpo");

                FirebaseUser usuarioAtual = FirebaseAuth.getInstance().getCurrentUser();
                if (usuarioAtual != null) {
                    String meuUid = usuarioAtual.getUid();
                    String outroUsuarioId = meuUid.equals(uidDestinatario) ? uidRemetente : uidDestinatario;

                    // Se o usuário já estiver com esta exata conversa aberta, não mostra o banner visual
                    if (ChatState.estaChatAberto(uidAnuncio, outroUsuarioId)) {
                        Log.d(TAG, "Chat já está aberto. Notificação visual suprimida.");
                        return;
                    }

                    // Busca o nome e foto do remetente via UsuarioService e constrói a notificação rica
                    buscarDadosRemetenteEExibirNotificacao(outroUsuarioId, corpoMensagem, uidAnuncio);
                }
            }
        }
    }

    private void buscarDadosRemetenteEExibirNotificacao(String outroUsuarioId, String corpoMensagem, String anuncioId) {
        UsuarioService usuarioService = new UsuarioService();

        usuarioService.buscarUsuarioPorId(outroUsuarioId, new UsuarioService.ServiceCallback<Usuario>() {
            @Override
            public void onSucesso(Usuario usuario) {
                String nomeRemetente = "Usuário";
                String fotoPerfil = null;

                if (usuario != null) {
                    nomeRemetente = usuario.getEmailPrincipal() != null ? usuario.getEmailPrincipal() : "Usuário";
                    fotoPerfil = usuario.getFotoPerfil();
                }

                mostrarNotificacaoChatEstiloWhatsApp(nomeRemetente, corpoMensagem, anuncioId, outroUsuarioId, fotoPerfil);
            }

            @Override
            public void onErro(String erro) {
                Log.e(TAG, "Erro ao buscar dados do remetente para notificação: " + erro);
                mostrarNotificacaoChatEstiloWhatsApp("Nova mensagem", corpoMensagem, anuncioId, outroUsuarioId, null);
            }
        });
    }

    private void mostrarNotificacaoChatEstiloWhatsApp(String nomeRemetente, String corpo, String anuncioId, String outroUsuarioId, String urlFotoPerfil) {
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Mensagens do Chat",
                    NotificationManager.IMPORTANCE_HIGH
            );
            notificationManager.createNotificationChannel(channel);
        }

        // Intent para redirecionar diretamente para a ChatActivity correta ao tocar na notificação
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra("anuncioId", anuncioId);
        intent.putExtra("userDestinatario", outroUsuarioId);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                (int) System.currentTimeMillis(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        Person.Builder remetenteBuilder = new Person.Builder().setName(nomeRemetente);

        Bitmap bitmapFoto = null;
        try {
            String urlResolvida = ImageUtils.obterUrlImagem(urlFotoPerfil);
            bitmapFoto = SvgLoader.baixarBitmap(
                    getApplicationContext(),
                    urlResolvida,
                    100,
                    100,
                    true
            );
        } catch (java.io.IOException | RuntimeException e) {
            Log.e(TAG, "Erro ao carregar foto do remetente para a notificação.", e);
        }
        if (bitmapFoto == null) {
            bitmapFoto = SvgLoader.bitmapPlaceholder(
                    getApplicationContext(),
                    R.drawable.placeholder_usuario,
                    100,
                    100,
                    true
            );
        }
        if (bitmapFoto != null) {
            remetenteBuilder.setIcon(IconCompat.createWithBitmap(bitmapFoto));
        }

        Person remetente = remetenteBuilder.build();

        NotificationCompat.MessagingStyle messagingStyle = new NotificationCompat.MessagingStyle(
                new Person.Builder().setName("Eu").build()
        );
        messagingStyle.addMessage(corpo, System.currentTimeMillis(), remetente);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.logo_roxa_texto_preto)
                .setStyle(messagingStyle)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        notificationManager.notify((int) System.currentTimeMillis(), builder.build());
    }

    private void enviarTokenParaServidor(String token) {
        FirebaseUser usuarioAtual = FirebaseAuth.getInstance().getCurrentUser();
        if (usuarioAtual != null) {
            UsuarioService usuarioService = new UsuarioService();
            usuarioService.atualizarFcmToken(
                    usuarioAtual.getUid(),
                    token,
                    new UsuarioService.ServiceCallback<Void>() {
                        @Override
                        public void onSucesso(Void data) {
                            Log.d(TAG, "Token FCM atualizado via UsuarioService com sucesso!");
                        }

                        @Override
                        public void onErro(String erro) {
                            Log.e(TAG, "Erro ao atualizar token FCM via UsuarioService: " + erro);
                        }
                    }
            );
        }
    }
}