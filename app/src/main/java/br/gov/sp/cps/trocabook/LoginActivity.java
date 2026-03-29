package br.gov.sp.cps.trocabook;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.credentials.CredentialManager;
import androidx.credentials.GetCredentialRequest;
import androidx.credentials.GetCredentialResponse;
import androidx.credentials.exceptions.GetCredentialException;

import com.google.android.gms.common.SignInButton;
import com.google.android.libraries.identity.googleid.GetGoogleIdOption;
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "TROCABOOK_LOGIN";
    private TextInputEditText editEmail, editSenha;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private CredentialManager credentialManager;
    private final Executor executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Inicialização do Firebase e Componentes
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        credentialManager = CredentialManager.create(this);

        // Referências do Layout
        editEmail = findViewById(R.id.editLoginEmail);
        editSenha = findViewById(R.id.editLoginSenha);
        Button btnEntrar = findViewById(R.id.btnEntrar);
        SignInButton btnGoogle = findViewById(R.id.btnLoginGoogle);
        TextView txtEsqueciSenha = findViewById(R.id.txtEsqueciSenha);

        // Configuração de Cliques
        btnEntrar.setOnClickListener(v -> logarManual());
        btnGoogle.setOnClickListener(v -> loginComGoogle());

        // AÇÃO: Abrir a nova tela de Recuperar Senha
        txtEsqueciSenha.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RecuperarSenhaActivity.class);
            startActivity(intent);
        });

        findViewById(R.id.btnVoltarLogin).setOnClickListener(v -> finish());
    }

    private void logarManual() {
        String email = editEmail.getText().toString().trim();
        String senha = editSenha.getText().toString();

        if (email.isEmpty() || senha.isEmpty()) {
            Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.signInWithEmailAndPassword(email, senha)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        verificarCadastroNoFirestore(mAuth.getCurrentUser());
                    } else {
                        Toast.makeText(this, "E-mail ou senha incorretos", Toast.LENGTH_SHORT).show();
                    }
                });
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
                        try {
                            GoogleIdTokenCredential googleIdToken = GoogleIdTokenCredential.createFrom(result.getCredential().getData());
                            AuthCredential credential = GoogleAuthProvider.getCredential(googleIdToken.getIdToken(), null);

                            mAuth.signInWithCredential(credential).addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    verificarCadastroNoFirestore(mAuth.getCurrentUser());
                                }
                            });
                        } catch (Exception e) {
                            Log.e(TAG, "Erro ao processar credencial Google: " + e.getMessage());
                        }
                    }

                    @Override
                    public void onError(GetCredentialException e) {
                        Log.e(TAG, "Erro CredentialManager: " + e.getMessage());
                    }
                });
    }

    private void verificarCadastroNoFirestore(FirebaseUser user) {
        if (user == null) return;

        db.collection("usuarios").document(user.getUid()).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document != null && document.exists()) {
                            // Se o cadastro está completo, vai para a Splash decidir sobre biometria
                            irParaSplash();
                        } else {
                            // Se faltam dados (CPF/RG), manda para a tela de segurança
                            Toast.makeText(this, "Complete seu cadastro!", Toast.LENGTH_LONG).show();
                            Intent intent = new Intent(this, DadosSegurancaActivity.class);
                            intent.putExtra("NOME_USUARIO", user.getDisplayName());
                            intent.putExtra("EMAIL_USUARIO", user.getEmail());
                            startActivity(intent);
                            finish();
                        }
                    } else {
                        Log.e(TAG, "Erro ao consultar Firestore", task.getException());
                        Toast.makeText(this, "Erro de conexão com o servidor", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void irParaSplash() {
        Intent intent = new Intent(this, SplashActivity.class);
        intent.putExtra("AUTO_CHECK", true);
        startActivity(intent);
        finish();
    }
}