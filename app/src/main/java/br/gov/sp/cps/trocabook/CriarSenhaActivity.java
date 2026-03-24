package br.gov.sp.cps.trocabook;

import android.content.Intent;
import android.os.Bundle;
import android.text.*;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
// IMPORTANTE: Adicione as importações do Firebase
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;

public class CriarSenhaActivity extends AppCompatActivity {

    private TextInputLayout layoutNome, layoutSobrenome, layoutEmail, layoutSenha, layoutConfirma;
    private TextInputEditText editNome, editSobrenome, editEmail, editSenha, editConfirma;

    private FirebaseAuth mAuth;

    private final InputFilter apenasLetras = (source, start, end, dest, dstart, dend) -> {
        StringBuilder filtrado = new StringBuilder();
        for (int i = start; i < end; i++) {
            char c = source.charAt(i);
            if (Character.isLetter(c) || Character.isSpaceChar(c)) {
                filtrado.append(c);
            }
        }
        return filtrado.toString();
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_criar_senha);

        mAuth = FirebaseAuth.getInstance();

        layoutNome = findViewById(R.id.layoutNome);
        layoutSobrenome = findViewById(R.id.layoutSobrenome);
        layoutEmail = findViewById(R.id.layoutEmail);
        layoutSenha = findViewById(R.id.layoutSenha);
        layoutConfirma = findViewById(R.id.layoutConfirmaSenha);

        editNome = findViewById(R.id.editNome);
        editSobrenome = findViewById(R.id.editSobrenome);
        editEmail = findViewById(R.id.editEmail);
        editSenha = findViewById(R.id.editSenha);
        editConfirma = findViewById(R.id.editConfirmaSenha);

        editNome.setFilters(new InputFilter[]{apenasLetras});
        editSobrenome.setFilters(new InputFilter[]{apenasLetras});

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

        mAuth.createUserWithEmailAndPassword(email, senha)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        irParaProximaTela();
                    } else {
                        String erro;
                        try {
                            throw task.getException();
                        } catch (FirebaseAuthUserCollisionException e) {
                            erro = "Este e-mail já está cadastrado.";
                        } catch (Exception e) {
                            erro = "Erro ao cadastrar: " + e.getMessage();
                        }
                        Toast.makeText(this, erro, Toast.LENGTH_LONG).show();
                    }
                });
    }


    private void irParaProximaTela() {
        Intent intent = new Intent(this, DadosSegurancaActivity.class);
        intent.putExtra("NOME_USUARIO", editNome.getText().toString() + " " + editSobrenome.getText().toString());
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
        if (editNome.getText().toString().isEmpty()) {
            layoutNome.setError("Digite seu nome");
            return false;
        }
        if (editSobrenome.getText().toString().isEmpty()) {
            layoutSobrenome.setError("Digite seu sobrenome");
            return false;
        }
        if (!editSenha.getText().toString().equals(editConfirma.getText().toString())) {
            layoutConfirma.setError("Senhas não coincidem");
            return false;
        }
        return layoutEmail.getError() == null && layoutSenha.getError() == null;
    }
}