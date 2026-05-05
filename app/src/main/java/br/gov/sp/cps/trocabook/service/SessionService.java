package br.gov.sp.cps.trocabook.service;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionService {

    private static final String PREF_NAME = "TrocabookPrefs";
    private static final String KEY_BIOMETRIA = "biometria_ativa";

    private final SharedPreferences prefs;

    public SessionService(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }
    public boolean isBiometriaAtiva() {
        return prefs.getBoolean(KEY_BIOMETRIA, false);
    }


    public void setBiometriaAtiva(boolean ativa) {
        prefs.edit().putBoolean(KEY_BIOMETRIA, ativa).apply();
    }
}