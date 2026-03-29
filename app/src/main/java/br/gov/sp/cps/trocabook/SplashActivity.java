package br.gov.sp.cps.trocabook;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import java.util.concurrent.Executor;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

            if (user != null) {
                SharedPreferences prefs = getSharedPreferences("TrocabookPrefs", MODE_PRIVATE);
                boolean biometriaAtiva = prefs.getBoolean("biometria_ativa", false);

                if (biometriaAtiva) {
                    chamarBiometria();
                } else {
                    startActivity(new Intent(this, MainActivity.class));
                    finish();
                }
            } else {
                startActivity(new Intent(this, WelcomeActivity.class));
                finish();
            }
        }, 1500);
    }

    private void chamarBiometria() {
        Executor executor = ContextCompat.getMainExecutor(this);
        BiometricPrompt biometricPrompt = new BiometricPrompt(this, executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                startActivity(new Intent(SplashActivity.this, MainActivity.class));
                finish();
            }
        });

        BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Acesso Seguro")
                .setSubtitle("Confirme sua identidade")
                .setNegativeButtonText("Sair")
                .build();

        biometricPrompt.authenticate(promptInfo);
    }
}