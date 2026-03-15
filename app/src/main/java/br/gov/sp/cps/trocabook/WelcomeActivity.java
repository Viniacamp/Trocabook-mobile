package br.gov.sp.cps.trocabook;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class WelcomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        Button btnLogin = findViewById(R.id.btnIrLogin);
        Button btnCadastro = findViewById(R.id.btnIrCadastro);

        btnLogin.setOnClickListener(v -> {
            Intent intent = new Intent(WelcomeActivity.this, LoginActivity.class);
            startActivity(intent);
        });

        btnCadastro.setOnClickListener(v -> {
            Intent intent = new Intent(WelcomeActivity.this, CadastroActivity.class);
            startActivity(intent);
        });
    }
}