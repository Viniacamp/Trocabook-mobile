package br.gov.sp.cps.trocabook.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import br.gov.sp.cps.trocabook.R;

public class MainActivity extends AppCompatActivity {
    private BottomNavigationView bottomNavigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        bottomNavigation = findViewById(R.id.bottomNavigation);

        bottomNavigation.setSelectedItemId(R.id.menu_home);

        bottomNavigation.setOnItemSelectedListener(item -> {

            int id = item.getItemId();

            Class<?> tela = null;

            if (id == R.id.menu_home) {
                tela = MainActivity.class;

            } else if (id == R.id.menu_anuncio) {
                tela = BuscaActivity.class;

            } else if (id == R.id.menu_livros) {
                tela = MeusLivrosActivity.class;

            } else if (id == R.id.menu_chat) {

                // futura tela chat
                return true;

            } else if (id == R.id.menu_perfil) {

                // futura tela perfil
                return true;
            }

            if (tela != null) {

                Intent intent = new Intent(this, tela);

                intent.setFlags(
                        Intent.FLAG_ACTIVITY_CLEAR_TOP |
                                Intent.FLAG_ACTIVITY_SINGLE_TOP
                );

                startActivity(intent);
            }

            return true;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        bottomNavigation.getMenu()
                .findItem(R.id.menu_home)
                .setChecked(true);
    }


}