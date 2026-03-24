package br.gov.sp.cps.trocabook;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

public class VerificacaoActivity extends AppCompatActivity {

    private TextView txtInstrucao;
    private EditText editCodigo;
    private Button btnConfirmar;
    private String verificationId, metodo;

    private Handler handler = new Handler();
    private Runnable runnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verificacao);

        txtInstrucao = findViewById(R.id.txtInstrucaoVerificacao);
        editCodigo = findViewById(R.id.editCodigoVerificacao);
        btnConfirmar = findViewById(R.id.btnConfirmar);

        metodo = getIntent().getStringExtra("METODO_ESCOLHIDO");
        String destino = getIntent().getStringExtra("DESTINO_CODIGO");
        verificationId = getIntent().getStringExtra("VERIFICATION_ID");

        if ("EMAIL".equals(metodo)) {
            txtInstrucao.setText("Link enviado para: " + destino + "\n\nAbra seu e-mail e clique no link de confirmação.\nO Trocabook avançará sozinho assim que você confirmar!");
            editCodigo.setVisibility(View.GONE); // Esconde o campo de números
            btnConfirmar.setVisibility(View.GONE); // Esconde o botão (fica automático)

            iniciarChecagemAutomatica();
        } else {
            txtInstrucao.setText("Insira o código de 6 dígitos enviado por SMS para:\n" + destino);
            editCodigo.setVisibility(View.VISIBLE);
            btnConfirmar.setVisibility(View.VISIBLE);
            btnConfirmar.setText("CONFIRMAR CÓDIGO SMS");
        }

        btnConfirmar.setOnClickListener(v -> {
            if ("SMS".equals(metodo)) {
                validarCodigoSMS(editCodigo.getText().toString().trim());
            }
        });
    }

    private void iniciarChecagemAutomatica() {
        runnable = new Runnable() {
            @Override
            public void run() {
                FirebaseAuth.getInstance().getCurrentUser().reload().addOnCompleteListener(task -> {
                    if (FirebaseAuth.getInstance().getCurrentUser().isEmailVerified()) {
                        irParaBiometria();
                    } else {
                        handler.postDelayed(this, 3000); // Tenta de novo em 3 segundos
                    }
                });
            }
        };
        handler.post(runnable);
    }

    private void validarCodigoSMS(String codigo) {
        if (codigo.length() < 6) {
            Toast.makeText(this, "Digite os 6 dígitos", Toast.LENGTH_SHORT).show();
            return;
        }
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, codigo);
        FirebaseAuth.getInstance().getCurrentUser().updatePhoneNumber(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) { irParaBiometria(); }
                    else { Toast.makeText(this, "Código inválido!", Toast.LENGTH_SHORT).show(); }
                });
    }

    private void irParaBiometria() {
        if (handler != null && runnable != null) handler.removeCallbacks(runnable);
        startActivity(new Intent(this, BiometriaActivity.class));
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (handler != null && runnable != null) handler.removeCallbacks(runnable);
    }
}