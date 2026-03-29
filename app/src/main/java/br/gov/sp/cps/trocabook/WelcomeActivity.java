package br.gov.sp.cps.trocabook;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class WelcomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FirebaseUser usuarioAtual = FirebaseAuth.getInstance().getCurrentUser();

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