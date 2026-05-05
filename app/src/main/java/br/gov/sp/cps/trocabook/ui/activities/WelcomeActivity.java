package br.gov.sp.cps.trocabook.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseUser;

import br.gov.sp.cps.trocabook.R;
import br.gov.sp.cps.trocabook.service.AuthService;

public class WelcomeActivity extends AppCompatActivity {
    private AuthService authService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        authService = new AuthService(this);
        FirebaseUser usuarioAtual = authService.getUsuarioAtual();

        if (usuarioAtual != null) {
            Intent intent = new Intent(this, BiometriaActivity.class);
            intent.putExtra("AUTO_CHECK", true);
            startActivity(intent);
            finish();
            return;
        }

        setContentView(R.layout.activity_welcome);

        findViewById(R.id.btnIrLogin).setOnClickListener(v ->
                startActivity(new Intent(this, LoginActivity.class)));

        findViewById(R.id.btnIrCadastro).setOnClickListener(v ->
                startActivity(new Intent(this, CadastroActivity.class)));
    }
}