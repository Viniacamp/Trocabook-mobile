package br.gov.sp.cps.trocabook.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.*;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseUser;

import br.gov.sp.cps.trocabook.R;
import br.gov.sp.cps.trocabook.service.AuthService;

public class CriarSenhaActivity extends AppCompatActivity {

    private TextInputLayout layoutEmail, layoutSenha, layoutConfirma;
    private TextInputEditText editEmail, editSenha, editConfirma;

    private AuthService authService;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_criar_senha);

        authService = new AuthService(this);

        layoutEmail = findViewById(R.id.layoutEmail);
        layoutSenha = findViewById(R.id.layoutSenha);
        layoutConfirma = findViewById(R.id.layoutConfirmaSenha);

        editEmail = findViewById(R.id.editEmail);
        editSenha = findViewById(R.id.editSenha);
        editConfirma = findViewById(R.id.editConfirmaSenha);

        configurarValidacoes();

        Button btnProxima = findViewById(R.id.btnProxima);
        btnProxima.setOnClickListener(v -> {
            if (validarTudo()) {
                cadastrarUsuarioNoFirebase();
            }
        });

        findViewById(R.id.btnVoltarCadastro).setOnClickListener(v -> finish());
    }

    private void cadastrarUsuarioNoFirebase() {
        String email = editEmail.getText().toString().trim();
        String senha = editSenha.getText().toString();

        authService.cadastrarComEmail(email, senha, new AuthService.AuthCallback() {
            @Override
            public void onSuccess(FirebaseUser user) {
                irParaProximaTela();
            }

            @Override
            public void onError(String erro) {
                Toast.makeText(CriarSenhaActivity.this, erro, Toast.LENGTH_LONG).show();
            }
        });
    }


    private void irParaProximaTela() {
        Intent intent = new Intent(this, DadosSegurancaActivity.class);
        intent.putExtra("EMAIL_USUARIO", editEmail.getText().toString());
        startActivity(intent);
    }

    private void configurarValidacoes() {
        editEmail.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                String email = editEmail.getText().toString().trim();
                if (!email.isEmpty() && !email.endsWith("@gmail.com")) {
                    layoutEmail.setError("Use um email @gmail.com");
                } else {
                    layoutEmail.setError(null);
                }
            }
        });

        editSenha.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                String senha = editSenha.getText().toString();
                if (!senha.matches("^(?=.*[A-Za-z])(?=.*\\d).{8,}$")) {
                    layoutSenha.setError("Mínimo 8 caracteres, com letras e números");
                } else {
                    layoutSenha.setError(null);
                }
            }
        });

        editConfirma.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                if (!editSenha.getText().toString().equals(editConfirma.getText().toString())) {
                    layoutConfirma.setError("Senhas não coincidem");
                } else {
                    layoutConfirma.setError(null);
                }
            }
        });
    }

    private boolean validarTudo() {
        if (!editSenha.getText().toString().equals(editConfirma.getText().toString())) {
            layoutConfirma.setError("Senhas não coincidem");
            return false;
        }
        return layoutEmail.getError() == null && layoutSenha.getError() == null;
    }
}