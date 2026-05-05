package br.gov.sp.cps.trocabook.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.common.SignInButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseUser;

import br.gov.sp.cps.trocabook.R;
import br.gov.sp.cps.trocabook.service.AuthService;
import br.gov.sp.cps.trocabook.service.UsuarioService;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText editEmail, editSenha;

    private AuthService authService;
    private UsuarioService usuarioService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // 🔥 Services
        authService = new AuthService(this);
        usuarioService = new UsuarioService();

        // Views
        editEmail = findViewById(R.id.editLoginEmail);
        editSenha = findViewById(R.id.editLoginSenha);
        Button btnEntrar = findViewById(R.id.btnEntrar);
        SignInButton btnGoogle = findViewById(R.id.btnLoginGoogle);
        TextView txtEsqueciSenha = findViewById(R.id.txtEsqueciSenha);

        // Ações
        btnEntrar.setOnClickListener(v -> logarManual());
        btnGoogle.setOnClickListener(v -> loginComGoogle());

        txtEsqueciSenha.setOnClickListener(v ->
                startActivity(new Intent(this, RecuperarSenhaActivity.class))
        );

        findViewById(R.id.btnVoltarLogin).setOnClickListener(v -> finish());
    }

    private void logarManual() {

        String email = editEmail.getText().toString().trim();
        String senha = editSenha.getText().toString();

        authService.loginComEmail(email, senha, new AuthService.AuthCallback() {
            @Override
            public void onSuccess(FirebaseUser user) {
                verificarCadastro(user);
            }

            @Override
            public void onError(String erro) {
                Toast.makeText(LoginActivity.this, erro, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loginComGoogle() {

        authService.autenticarComGoogle(this, new AuthService.AuthCallback() {
            @Override
            public void onSuccess(FirebaseUser user) {
                verificarCadastro(user);
            }

            @Override
            public void onError(String erro) {
                Toast.makeText(LoginActivity.this, erro, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void verificarCadastro(FirebaseUser user) {

        if (user == null) return;

        usuarioService.usuarioExiste(user.getUid(), new UsuarioService.ServiceCallback<Boolean>() {

            @Override
            public void onSuccess(Boolean existe) {

                if (existe) {
                    irParaSplash();
                } else {
                    Toast.makeText(LoginActivity.this,
                            "Complete seu cadastro!",
                            Toast.LENGTH_LONG).show();

                    Intent intent = new Intent(LoginActivity.this, DadosSegurancaActivity.class);
                    intent.putExtra("NOME_USUARIO", user.getDisplayName());
                    intent.putExtra("EMAIL_USUARIO", user.getEmail());
                    startActivity(intent);
                    finish();
                }
            }

            @Override
            public void onError(String erro) {
                Toast.makeText(LoginActivity.this, erro, Toast.LENGTH_SHORT).show();
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