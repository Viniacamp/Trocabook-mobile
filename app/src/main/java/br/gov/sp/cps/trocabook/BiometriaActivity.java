package br.gov.sp.cps.trocabook;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import java.util.concurrent.Executor;

public class BiometriaActivity extends AppCompatActivity {

    private Executor executor;
    private BiometricPrompt biometricPrompt;
    private BiometricPrompt.PromptInfo promptInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_biometria);

        executor = ContextCompat.getMainExecutor(this);

        // CONFIGURAR O SENSOR
        biometricPrompt = new BiometricPrompt(this, executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                Toast.makeText(getApplicationContext(), "Biometria confirmada!", Toast.LENGTH_SHORT).show();
                irParaHome();
            }

            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                Toast.makeText(getApplicationContext(), "Erro: " + errString, Toast.LENGTH_SHORT).show();
            }
        });

        promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Ativar Biometria")
                .setSubtitle("Encoste o dedo no sensor para confirmar")
                .setNegativeButtonText("Cancelar")
                .build();

        findViewById(R.id.btnAtivarBiometria).setOnClickListener(v -> {

            biometricPrompt.authenticate(promptInfo);
        });

        findViewById(R.id.btnPularBiometria).setOnClickListener(v -> irParaHome());
    }

    private void irParaHome() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}