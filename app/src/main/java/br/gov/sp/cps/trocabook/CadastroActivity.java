package br.gov.sp.cps.trocabook;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import com.google.android.gms.common.SignInButton; // ADICIONE ESTE IMPORT
import androidx.appcompat.app.AppCompatActivity;

public class CadastroActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro);

        Button btnEmail = findViewById(R.id.btnCadastrarEmail);

        SignInButton btnGoogle = findViewById(R.id.btnCadastrarGoogle);

        Button btnVoltar = findViewById(R.id.btnVoltarWelcome);

        btnGoogle.setSize(SignInButton.SIZE_WIDE);

        btnEmail.setOnClickListener(v -> {
        });

        btnGoogle.setOnClickListener(v -> {
        });

        btnVoltar.setOnClickListener(v -> {
            finish();
        });
    }
}