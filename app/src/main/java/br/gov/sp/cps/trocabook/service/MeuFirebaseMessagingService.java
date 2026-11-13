package br.gov.sp.cps.trocabook.service;

import android.content.Intent;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MeuFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "FCM_DEBUG";
    public static final String ACTION_ATUALIZAR_CHAT = "br.gov.sp.cps.trocabook.ATUALIZAR_CHAT";

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        Log.d(TAG, "Novo token FCM gerado: " + token);
        enviarTokenParaServidor(token);
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        Log.d(TAG, "Notificação recebida do microsserviço: " + remoteMessage.getFrom());

        // Verifica se é uma mensagem de chat ou dados para atualizar a tela em tempo real
        // O microsserviço pode enviar dados extras (ex: tipo de evento: NOVA_MENSAGEM, EDITAR, EXCLUIR)
        if (!remoteMessage.getData().isEmpty() || remoteMessage.getNotification() != null) {
            Log.d(TAG, "Disparando atualização de chat via Broadcast Local");

            // Dispara um aviso interno no app para a ChatActivity atualizar as mensagens na hora
            Intent intent = new Intent(ACTION_ATUALIZAR_CHAT);
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        }
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