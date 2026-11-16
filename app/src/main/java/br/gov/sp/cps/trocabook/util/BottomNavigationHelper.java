package br.gov.sp.cps.trocabook.util;

import android.app.Activity;
import android.content.Intent;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import br.gov.sp.cps.trocabook.R;
import br.gov.sp.cps.trocabook.ui.activities.BuscaActivity;
import br.gov.sp.cps.trocabook.ui.activities.CatalogoActivity;
import br.gov.sp.cps.trocabook.ui.activities.ConversasActivity;
import br.gov.sp.cps.trocabook.ui.activities.MainActivity;
import br.gov.sp.cps.trocabook.ui.activities.MeusLivrosActivity;

public final class BottomNavigationHelper {

    private BottomNavigationHelper() {
    }

    public static void configurar(
            Activity activity,
            BottomNavigationView navigation,
            int itemSelecionado
    ) {
        navigation.setSelectedItemId(itemSelecionado);
        navigation.setOnItemSelectedListener(item -> {
            Class<?> destino = destinoPara(item.getItemId());
            if (destino == null || destino.equals(activity.getClass())) {
                return true;
            }

            Intent intent = new Intent(activity, destino);
            intent.setFlags(
                    Intent.FLAG_ACTIVITY_CLEAR_TOP
                            | Intent.FLAG_ACTIVITY_SINGLE_TOP
            );
            activity.startActivity(intent);
            return true;
        });
    }

    private static Class<?> destinoPara(int itemId) {
        if (itemId == R.id.menu_home) {
            return MainActivity.class;
        }
        if (itemId == R.id.menu_anuncio) {
            return BuscaActivity.class;
        }
        if (itemId == R.id.menu_livros) {
            return MeusLivrosActivity.class;
        }
        if (itemId == R.id.menu_chat) {
            return ConversasActivity.class;
        }
        if (itemId == R.id.menu_catalogo) {
            return CatalogoActivity.class;
        }
        return null;
    }
}
