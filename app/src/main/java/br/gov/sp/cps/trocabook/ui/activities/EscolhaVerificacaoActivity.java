package br.gov.sp.cps.trocabook.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseUser;

import br.gov.sp.cps.trocabook.R;
import br.gov.sp.cps.trocabook.service.AuthService;

public class EscolhaVerificacaoActivity extends AppCompatActivity {

    private String emailRecuperacao, telefone;
    private AuthService authService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_escolha_verificacao);

        authService = new AuthService(this);
        emailRecuperacao = getIntent().getStringExtra("email_recuperacao");
        telefone = getIntent().getStringExtra("telefone");

        findViewById(R.id.btnEscolhaEmail).setOnClickListener(v -> dispararEmail());
        findViewById(R.id.btnEscolhaSMS).setOnClickListener(v -> dispararSMS());
        findViewById(R.id.btnVoltarEscolha).setOnClickListener(v -> finish());
    }

    private void dispararEmail() {
        authService.enviarVerificacaoEmail(new AuthService.AuthCallback() {
            @Override
            public void onSuccess(FirebaseUser user) {
                Toast.makeText(EscolhaVerificacaoActivity.this,
                        "Link enviado para o e-mail principal!",
                        Toast.LENGTH_SHORT).show();

                irParaVerificacao("EMAIL", user.getEmail(), null);
            }

            @Override
            public void onError(String erro) {
                Toast.makeText(EscolhaVerificacaoActivity.this,
                        erro,
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void dispararSMS() {

        authService.enviarSMS(telefone, this, new AuthService.PhoneCallback() {

            @Override
            public void onCodeSent(String verificationId) {
                irParaVerificacao("SMS", telefone, verificationId);
            }

            @Override
            public void onError(String erro) {
                Toast.makeText(EscolhaVerificacaoActivity.this, erro, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void irParaVerificacao(String metodo, String destino, String vId) {
        Intent intent = new Intent(this, VerificacaoActivity.class);
        intent.putExtra("METODO_ESCOLHIDO", metodo);
        intent.putExtra("DESTINO_CODIGO", destino);
        intent.putExtra("VERIFICATION_ID", vId);
        startActivity(intent);
    }
}