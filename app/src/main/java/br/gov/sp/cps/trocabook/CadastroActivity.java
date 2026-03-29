package br.gov.sp.cps.trocabook;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class CadastroActivity extends AppCompatActivity {

    private static final String TAG = "TROCABOOK_AUTH";
    private CredentialManager credentialManager;
    private FirebaseAuth mAuth;
    private final Executor executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro);

        mAuth = FirebaseAuth.getInstance();
        credentialManager = CredentialManager.create(this);

        Button btnEmail = findViewById(R.id.btnCadastrarEmail);
        SignInButton btnGoogle = findViewById(R.id.btnCadastrarGoogle);
        Button btnVoltar = findViewById(R.id.btnVoltarWelcome);

        btnEmail.setOnClickListener(v -> startActivity(new Intent(this, CriarSenhaActivity.class)));
        btnVoltar.setOnClickListener(v -> finish());

        btnGoogle.setOnClickListener(v -> loginComGoogle());
    }

    private void loginComGoogle() {
        GetGoogleIdOption googleIdOption = new GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(BuildConfig.GOOGLE_CLIENT_ID)
                .build();

        GetCredentialRequest request = new GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build();

        credentialManager.getCredentialAsync(this, request, null, executor,
                new androidx.credentials.CredentialManagerCallback<GetCredentialResponse, GetCredentialException>() {
                    @Override
                    public void onResult(GetCredentialResponse result) {
                        handleSignIn(result);
                    }

                    @Override
                    public void onError(GetCredentialException e) {
                        runOnUiThread(() -> Log.e(TAG, "Erro CredentialManager: " + e.getMessage()));
                    }
                });
    }

    private void handleSignIn(GetCredentialResponse result) {
        androidx.credentials.Credential credential = result.getCredential();

        try {
            GoogleIdTokenCredential googleIdToken;
            if (credential instanceof GoogleIdTokenCredential) {
                googleIdToken = (GoogleIdTokenCredential) credential;
            } else {
                googleIdToken = GoogleIdTokenCredential.createFrom(credential.getData());
            }
            autenticarNoFirebase(googleIdToken.getIdToken());
        } catch (Exception e) {
            Log.e(TAG, "Erro ao processar credencial: " + e.getMessage());
            runOnUiThread(() -> Toast.makeText(this, "Erro na autenticação Google", Toast.LENGTH_SHORT).show());
        }
    }

    private void autenticarNoFirebase(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            proximaTela(user.getDisplayName(), user.getEmail());
                        }
                    } else {
                        Log.e(TAG, "Erro Firebase Auth: " + task.getException());
                    }
                });
    }

    private void proximaTela(String nome, String email) {
        runOnUiThread(() -> {
            Toast.makeText(this, "Login Google com sucesso!", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(CadastroActivity.this, DadosSegurancaActivity.class);
            intent.putExtra("NOME_USUARIO", nome);
            intent.putExtra("EMAIL_USUARIO", email);
            startActivity(intent);
            finish();
        });
    }
}