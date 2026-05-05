package br.gov.sp.cps.trocabook.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import br.gov.sp.cps.trocabook.R;
import br.gov.sp.cps.trocabook.service.BiometriaService;
import br.gov.sp.cps.trocabook.service.SessionService;

public class BiometriaActivity extends AppCompatActivity {

    private BiometriaService biometriaService;

    private SessionService sessionService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_biometria);
        biometriaService = new BiometriaService();
        sessionService = new SessionService(this);


        findViewById(R.id.btnAtivarBiometria).setOnClickListener(v -> {

            biometriaService.autenticar(this, new BiometriaService.BiometriaCallback() {

                @Override
                public void onSuccess() {
                    sessionService.setBiometriaAtiva(true);
                    irParaHome();
                }

                @Override
                public void onError(String erro) {
                    Toast.makeText(BiometriaActivity.this,
                            erro,
                            Toast.LENGTH_SHORT).show();
                }
            });

        });

        findViewById(R.id.btnPularBiometria).setOnClickListener(v -> {
            sessionService.setBiometriaAtiva(false);
            irParaHome();
        });
    }

    private void irParaHome() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}