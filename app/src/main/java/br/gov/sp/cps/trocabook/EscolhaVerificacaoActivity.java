package br.gov.sp.cps.trocabook;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class EscolhaVerificacaoActivity extends AppCompatActivity {

    private String emailRecuperacao, telefone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_escolha_verificacao);

        emailRecuperacao = getIntent().getStringExtra("email_recuperacao");
        telefone = getIntent().getStringExtra("telefone");

        Button btnEmail = findViewById(R.id.btnEscolhaEmail);
        Button btnSMS = findViewById(R.id.btnEscolhaSMS);
        Button btnVoltar = findViewById(R.id.btnVoltarEscolha);

        btnEmail.setOnClickListener(v -> {
            irParaVerificacao("EMAIL", emailRecuperacao);
        });

        btnSMS.setOnClickListener(v -> {
            irParaVerificacao("SMS", telefone);
        });

        btnVoltar.setOnClickListener(v -> finish());
    }

    private void irParaVerificacao(String metodo, String destino) {
        Intent intent = new Intent(this, VerificacaoActivity.class);
        intent.putExtra("METODO_ESCOLHIDO", metodo);
        intent.putExtra("DESTINO_CODIGO", destino);
        startActivity(intent);
    }
}