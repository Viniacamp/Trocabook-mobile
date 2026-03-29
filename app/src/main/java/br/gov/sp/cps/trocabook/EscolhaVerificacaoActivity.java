package br.gov.sp.cps.trocabook;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.FirebaseException;

import java.util.concurrent.TimeUnit;

public class EscolhaVerificacaoActivity extends AppCompatActivity {

    private String emailRecuperacao, telefone;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_escolha_verificacao);

        mAuth = FirebaseAuth.getInstance();
        emailRecuperacao = getIntent().getStringExtra("email_recuperacao");
        telefone = getIntent().getStringExtra("telefone");

        findViewById(R.id.btnEscolhaEmail).setOnClickListener(v -> dispararEmail());
        findViewById(R.id.btnEscolhaSMS).setOnClickListener(v -> dispararSMS());
        findViewById(R.id.btnVoltarEscolha).setOnClickListener(v -> finish());
    }

    private void dispararEmail() {
        if (mAuth.getCurrentUser() != null) {
            String emailPrincipalDoFirebase = mAuth.getCurrentUser().getEmail();

            mAuth.getCurrentUser().sendEmailVerification()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(this, "Link enviado para o e-mail principal!", Toast.LENGTH_SHORT).show();
                            irParaVerificacao("EMAIL", emailPrincipalDoFirebase, null);
                        } else {
                            Toast.makeText(this, "Erro ao enviar: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void dispararSMS() {
        String telLimpo = telefone.replaceAll("[^\\d]", "");
        String telFinal = "+55" + telLimpo;

        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(mAuth)
                .setPhoneNumber(telFinal)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(this)
                .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    @Override
                    public void onCodeSent(String verificationId, PhoneAuthProvider.ForceResendingToken token) {
                        irParaVerificacao("SMS", telefone, verificationId);
                    }

                    @Override
                    public void onVerificationCompleted(com.google.firebase.auth.PhoneAuthCredential credential) {}

                    @Override
                    public void onVerificationFailed(FirebaseException e) {
                        Toast.makeText(EscolhaVerificacaoActivity.this, "Erro: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                })
                .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    private void irParaVerificacao(String metodo, String destino, String vId) {
        Intent intent = new Intent(this, VerificacaoActivity.class);
        intent.putExtra("METODO_ESCOLHIDO", metodo);
        intent.putExtra("DESTINO_CODIGO", destino);
        intent.putExtra("VERIFICATION_ID", vId);
        startActivity(intent);
    }
}