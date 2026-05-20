package br.gov.sp.cps.trocabook.ui.adapters.utils;

import android.content.Context;

import java.io.File;

import br.gov.sp.cps.trocabook.R;

public class ImageResolver {

    public static Object resolve(Context context, String capa) {

        // 1. arquivo local
        if (capa.startsWith("/data") || capa.contains("/files/")) {
            return new File(capa);
        }

        // 2. drawable mock
        int resId = context.getResources()
                .getIdentifier(capa, "drawable", context.getPackageName());

        if (resId != 0) {
            return resId;
        }

        // 3. fallback
        return R.drawable.placeholder_capa;
    }
}
