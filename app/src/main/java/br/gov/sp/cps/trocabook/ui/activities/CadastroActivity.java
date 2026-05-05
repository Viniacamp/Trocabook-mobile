package br.gov.sp.cps.trocabook.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.common.SignInButton;
import com.google.firebase.auth.FirebaseUser;

import br.gov.sp.cps.trocabook.R;
import br.gov.sp.cps.trocabook.service.AuthService;

public class CadastroActivity extends AppCompatActivity {

    private AuthService authService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro);

        authService = new AuthService(this);

        Button btnEmail = findViewById(R.id.btnCadastrarEmail);
        SignInButton btnGoogle = findViewById(R.id.btnCadastrarGoogle);
        Button btnVoltar = findViewById(R.id.btnVoltarWelcome);

        btnEmail.setOnClickListener(v ->
                startActivity(new Intent(this, CriarSenhaActivity.class))
        );

        btnVoltar.setOnClickListener(v -> finish());

        btnGoogle.setOnClickListener(v -> loginComGoogle());
    }

    private void loginComGoogle() {

        authService.autenticarComGoogle(this, new AuthService.AuthCallback() {

            @Override
            public void onSuccess(FirebaseUser user) {
                if (user != null) {
                    proximaTela(user.getDisplayName(), user.getEmail());
                }
            }

            @Override
            public void onError(String erro) {
                Toast.makeText(CadastroActivity.this, erro, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void proximaTela(String nome, String email) {
        Toast.makeText(this, "Login Google com sucesso!", Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(this, DadosSegurancaActivity.class);
        intent.putExtra("NOME_USUARIO", nome);
        intent.putExtra("EMAIL_USUARIO", email);

        startActivity(intent);
        finish();
    }
}