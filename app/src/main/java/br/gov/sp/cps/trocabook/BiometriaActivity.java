package br.gov.sp.cps.trocabook;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import com.google.firebase.auth.FirebaseAuth;
import java.util.concurrent.Executor;

public class BiometriaActivity extends AppCompatActivity {

    private BiometricPrompt biometricPrompt;
    private BiometricPrompt.PromptInfo promptInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_biometria);

        Executor executor = ContextCompat.getMainExecutor(this);

        biometricPrompt = new BiometricPrompt(this, executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                setBiometriaStatus(true);
                irParaHome();
            }

            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                Toast.makeText(BiometriaActivity.this, "Erro: " + errString, Toast.LENGTH_SHORT).show();
            }
        });

        promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Biometria Trocabook")
                .setSubtitle("Use sua digital para entrar")
                .setNegativeButtonText("Cancelar")
                .build();

        findViewById(R.id.btnAtivarBiometria).setOnClickListener(v -> biometricPrompt.authenticate(promptInfo));

        findViewById(R.id.btnPularBiometria).setOnClickListener(v -> {
            setBiometriaStatus(false);
            irParaHome();
        });
    }

    private void setBiometriaStatus(boolean status) {
        SharedPreferences prefs = getSharedPreferences("TrocabookPrefs", MODE_PRIVATE);
        prefs.edit().putBoolean("biometria_ativa", status).apply();
    }

    private void irParaHome() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}