package br.gov.sp.cps.trocabook.ui.activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseUser;

import br.gov.sp.cps.trocabook.R;
import br.gov.sp.cps.trocabook.service.AuthService;

public class RecuperarSenhaActivity extends AppCompatActivity {

    private TextInputEditText editEmail;
    private AuthService authService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recuperar_senha);

        authService = new AuthService(this);
        editEmail = findViewById(R.id.editEmailRecuperar);
        Button btnEnviar = findViewById(R.id.btnEnviarLink);

        btnEnviar.setOnClickListener(v -> {

            String email = editEmail.getText().toString().trim();

            authService.enviarResetSenha(email, new AuthService.AuthCallback() {
                @Override
                public void onSuccess(FirebaseUser user) {
                    Toast.makeText(RecuperarSenhaActivity.this,
                            "Link enviado! Verifique sua caixa de entrada.",
                            Toast.LENGTH_LONG).show();

                    finish();
                }

                @Override
                public void onError(String erro) {
                    Toast.makeText(RecuperarSenhaActivity.this,
                            erro,
                            Toast.LENGTH_SHORT).show();
                }
            });
        });

        findViewById(R.id.btnVoltarRecuperar).setOnClickListener(v -> finish());
    }
}