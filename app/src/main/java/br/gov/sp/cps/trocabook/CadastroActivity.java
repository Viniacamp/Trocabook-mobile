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
        if (result.getCredential() instanceof GoogleIdTokenCredential) {
            GoogleIdTokenCredential googleIdTokenCredential = (GoogleIdTokenCredential) result.getCredential();

            // Aqui você já tem o e-mail e nome
            runOnUiThread(() -> {
                Toast.makeText(this, "Bem-vindo: " + googleIdTokenCredential.getDisplayName(), Toast.LENGTH_SHORT).show();
                startActivity(new Intent(CadastroActivity.this, DadosSegurancaActivity.class));
            });
        }
    }
}