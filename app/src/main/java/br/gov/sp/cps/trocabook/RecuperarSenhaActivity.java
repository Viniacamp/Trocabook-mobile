package br.gov.sp.cps.trocabook;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;

public class RecuperarSenhaActivity extends AppCompatActivity {

    private TextInputEditText editEmail;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recuperar_senha);

        mAuth = FirebaseAuth.getInstance();
        editEmail = findViewById(R.id.editEmailRecuperar);
        Button btnEnviar = findViewById(R.id.btnEnviarLink);

        btnEnviar.setOnClickListener(v -> {
            String email = editEmail.getText().toString().trim();

            if (email.isEmpty()) {
                Toast.makeText(this, "Por favor, digite seu e-mail", Toast.LENGTH_SHORT).show();
                return;
            }

            mAuth.sendPasswordResetEmail(email)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(this, "Link enviado! Verifique sua caixa de entrada.", Toast.LENGTH_LONG).show();
                            finish(); // Fecha a tela e volta para o Login
                        } else {
                            Toast.makeText(this, "Erro: E-mail não cadastrado.", Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        findViewById(R.id.btnVoltarRecuperar).setOnClickListener(v -> finish());
    }
}