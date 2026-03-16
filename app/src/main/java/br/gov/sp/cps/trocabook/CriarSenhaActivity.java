package br.gov.sp.cps.trocabook;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class CriarSenhaActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_criar_senha);

        Button btnProxima = findViewById(R.id.btnProxima);
        EditText editSenha = findViewById(R.id.editSenha);
        EditText editConfirma = findViewById(R.id.editConfirmaSenha);


        btnProxima.setOnClickListener(v -> {
            String senha = editSenha.getText().toString();
            String confirma = editConfirma.getText().toString();

            if (senha.equals(confirma) && !senha.isEmpty()) {
                Intent intent = new Intent(this, DadosSegurancaActivity.class);
                startActivity(intent);
            } else {
                Toast.makeText(this, "As senhas não conferem!", Toast.LENGTH_SHORT).show();
            }
        });

        Button btnVoltar = findViewById(R.id.btnVoltarCadastro);
        btnVoltar.setOnClickListener(v -> {
            finish();
        });
    }
}