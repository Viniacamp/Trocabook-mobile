package br.gov.sp.cps.trocabook;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class VerificacaoActivity extends AppCompatActivity {

    private TextView txtInstrucao;
    private EditText editCodigo;
    private Button btnConfirmar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verificacao);


        txtInstrucao = findViewById(R.id.txtInstrucaoVerificacao);
        editCodigo = findViewById(R.id.editCodigoVerificacao);
        btnConfirmar = findViewById(R.id.btnConfirmar);

        String metodo = getIntent().getStringExtra("METODO_ESCOLHIDO");
        String destino = getIntent().getStringExtra("DESTINO_CODIGO");

        if (metodo != null && destino != null) {
            if (metodo.equals("EMAIL")) {
                String emailMascarado = mascararEmail(destino);
                txtInstrucao.setText("Insira o código enviado para o e-mail:\n" + emailMascarado);
            } else {
                String telMascarado = mascararTelefone(destino);
                txtInstrucao.setText("Insira o código enviado por SMS para:\n" + telMascarado);
            }
        }
        
        btnConfirmar.setOnClickListener(v -> {
            String codigoDigitado = editCodigo.getText().toString().trim();

            if (codigoDigitado.length() < 6) {
                Toast.makeText(this, "O código deve ter 6 dígitos", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Conta verificada com sucesso!", Toast.LENGTH_LONG).show();

                Intent intent = new Intent(this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });
    }

    private String mascararEmail(String email) {
        if (email == null || !email.contains("@")) return email;
        int atIndex = email.indexOf("@");
        if (atIndex <= 2) return email;

        String prefixo = email.substring(0, 2);
        String dominio = email.substring(atIndex);
        return prefixo + "****" + dominio;
    }

    private String mascararTelefone(String tel) {
        if (tel == null) return "";
        String numeros = tel.replaceAll("[^\\d]", "");

        if (numeros.length() >= 4) {
            String ultimosQuatro = numeros.substring(numeros.length() - 4);
            return "(**) *****-" + ultimosQuatro;
        }
        return tel;
    }
}