package br.gov.sp.cps.trocabook.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseUser;

import br.gov.sp.cps.trocabook.R;
import br.gov.sp.cps.trocabook.service.AuthService;
import br.gov.sp.cps.trocabook.service.BiometriaService;
import br.gov.sp.cps.trocabook.service.SessionService;

public class SplashActivity extends AppCompatActivity {

    private AuthService authService;
    private SessionService sessionService;
    private BiometriaService biometriaService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        authService = new AuthService(this);
        sessionService = new SessionService(this);
        biometriaService = new BiometriaService();

        new Handler(Looper.getMainLooper()).postDelayed(this::verificarFluxo, 1500);
    }

    private void verificarFluxo() {

        FirebaseUser user = authService.getUsuarioAtual();

        if (user != null) {

            if (sessionService.isBiometriaAtiva()) {
                autenticarComBiometria();
            } else {
                irParaMain();
            }

        } else {
            irParaWelcome();
        }
    }

    private void autenticarComBiometria() {

        biometriaService.autenticar(this, new BiometriaService.BiometriaCallback() {

            @Override
            public void onSuccess() {
                irParaMain();
            }

            @Override
            public void onError(String erro) {
                irParaWelcome();
                finish();
            }
        });
    }

    private void irParaMain() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    private void irParaWelcome() {
        startActivity(new Intent(this, WelcomeActivity.class));
        finish();
    }
}