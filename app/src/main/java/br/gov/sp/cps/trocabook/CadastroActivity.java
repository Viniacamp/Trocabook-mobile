package br.gov.sp.cps.trocabook;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.credentials.CredentialManager;
import androidx.credentials.GetCredentialRequest;
import androidx.credentials.GetCredentialResponse;
import androidx.credentials.exceptions.GetCredentialException;

import com.google.android.gms.common.SignInButton;
import com.google.android.libraries.identity.googleid.GetGoogleIdOption;
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class CadastroActivity extends AppCompatActivity {

    private CredentialManager credentialManager;
    private final Executor executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro);

        credentialManager = CredentialManager.create(this);

        Button btnEmail = findViewById(R.id.btnCadastrarEmail);
        SignInButton btnGoogle = findViewById(R.id.btnCadastrarGoogle);
        Button btnVoltar = findViewById(R.id.btnVoltarWelcome);

        btnEmail.setOnClickListener(v -> {
            startActivity(new Intent(this, CriarSenhaActivity.class));
        });

        btnGoogle.setOnClickListener(v -> loginComGoogle());

        btnVoltar.setOnClickListener(v -> finish());
    }

    private void loginComGoogle() {
        GetGoogleIdOption googleIdOption = new GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(BuildConfig.GOOGLE_CLIENT_ID)
                .build();

        GetCredentialRequest request = new GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build();

        // Executa a requisição de forma assíncrona (não trava a tela)
        credentialManager.getCredentialAsync(this, request, null, executor, new androidx.credentials.CredentialManagerCallback<GetCredentialResponse, GetCredentialException>() {
            @Override
            public void onResult(GetCredentialResponse result) {
                handleSignIn(result);
            }

            @Override
            public void onError(GetCredentialException e) {
                runOnUiThread(() -> Toast.makeText(CadastroActivity.this, "Erro: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void handleSignIn(GetCredentialResponse result) {
        // 1. Pegamos a credencial bruta
        androidx.credentials.Credential credential = result.getCredential();

        // 2. Log de debug para sabermos o que o Google está cuspindo
        android.util.Log.d("TROCABOOK", "Tipo recebido: " + credential.getType());

        // 3. Verificação compatível com as versões mais novas do Android
        if (credential instanceof GoogleIdTokenCredential) {
            GoogleIdTokenCredential googleIdToken = (GoogleIdTokenCredential) credential;
            String nome = googleIdToken.getDisplayName();
            String email = googleIdToken.getId();

            executarTrocaDeTela(nome, email);

        } else if (credential.getType().equals(GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL)) {
            // Às vezes ele vem como o tipo genérico, então extraímos manualmente
            try {
                GoogleIdTokenCredential googleIdToken = GoogleIdTokenCredential.createFrom(credential.getData());
                executarTrocaDeTela(googleIdToken.getDisplayName(), googleIdToken.getId());
            } catch (Exception e) {
                android.util.Log.e("TROCABOOK", "Erro ao converter: " + e.getMessage());
            }
        } else {
            runOnUiThread(() -> Toast.makeText(this, "Credencial não reconhecida", Toast.LENGTH_SHORT).show());
        }
    }

    private void executarTrocaDeTela(String nome, String email) {
        runOnUiThread(() -> {
            Toast.makeText(this, "Bem-vindo: " + nome, Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(CadastroActivity.this, DadosSegurancaActivity.class);
            intent.putExtra("NOME_USUARIO", nome);
            intent.putExtra("EMAIL_USUARIO", email);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        });
    }
}