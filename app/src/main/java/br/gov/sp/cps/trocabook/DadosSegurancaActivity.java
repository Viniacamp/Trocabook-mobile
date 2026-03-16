package br.gov.sp.cps.trocabook;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class DadosSegurancaActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dados_seguranca);

        Button btnVerificar = findViewById(R.id.btnEnviarVerificacao);
        btnVerificar.setOnClickListener(v -> {
            Intent intent = new Intent(this, VerificacaoActivity.class);
            startActivity(intent);
        });

        Button btnVoltar = findViewById(R.id.btnVoltarSeguranca);
        btnVoltar.setOnClickListener(v -> {
            finish();
        });
    }
}