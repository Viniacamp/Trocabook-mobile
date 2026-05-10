package br.gov.sp.cps.trocabook.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseUser;

import br.gov.sp.cps.trocabook.R;
import br.gov.sp.cps.trocabook.service.AuthService;

public class VerificacaoActivity extends AppCompatActivity {

    private TextView txtInstrucao;
    private EditText editCodigo;
    private Button btnConfirmar;
    private String verificationId, metodo;

    private Handler handler = new Handler();

    private AuthService authService;
    private Runnable runnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verificacao);

        txtInstrucao = findViewById(R.id.txtInstrucaoVerificacao);
        editCodigo = findViewById(R.id.editCodigoVerificacao);
        btnConfirmar = findViewById(R.id.btnConfirmar);

        authService = new AuthService(this);

        metodo = getIntent().getStringExtra("METODO_ESCOLHIDO");
        String destino = getIntent().getStringExtra("DESTINO_CODIGO");
        verificationId = getIntent().getStringExtra("VERIFICATION_ID");

        if ("EMAIL".equals(metodo)) {
            txtInstrucao.setText("Link enviado para: " + destino + "\n\nAbra seu e-mail e clique no link de confirmação.\nO Trocabook avançará sozinho assim que você confirmar!");
            editCodigo.setVisibility(View.GONE);
            btnConfirmar.setVisibility(View.GONE);

            handler.postDelayed(() -> iniciarChecagemAutomatica(), 5000);
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
                authService.verificarEmailVerificado(new AuthService.AuthCallback() {
                    @Override
                    public void onSuccess(FirebaseUser user) {
                        irParaBiometria();
                    }

                    @Override
                    public void onError(String erro) {
                        handler.postDelayed(runnable, 4000);
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

        authService.confirmarCodigoSMS(
                verificationId,
                codigo,
                new AuthService.AuthCallback() {
                    @Override
                    public void onSuccess(FirebaseUser user) {
                        irParaBiometria();
                    }

                    @Override
                    public void onError(String erro) {
                        Toast.makeText(VerificacaoActivity.this, erro, Toast.LENGTH_SHORT).show();
                    }
                }
        );
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