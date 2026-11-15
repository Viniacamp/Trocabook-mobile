package br.gov.sp.cps.trocabook.util;

import android.content.Context;
import android.graphics.drawable.PictureDrawable;
import android.net.Uri;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.caverock.androidsvg.SVG;

import java.io.InputStream;

public class SvgLoader {

    public static void carregarSvg(Context context, String url, ImageView imageView, int placeholder) {
        if (url == null || url.isEmpty()) {
            imageView.setImageResource(placeholder);
            return;
        }

        // Se for um link da web ou Firebase que termina em .svg
        if (url.toLowerCase().endsWith(".svg")) {
            // Você pode usar uma requisição HTTP em background ou Glide com um Decoder customizado.
            // Alternativa simples para SVG direto via InputStream:
            new Thread(() -> {
                try {
                    java.net.URL imageUrl = new java.net.URL(url);
                    java.net.HttpURLConnection connection = (java.net.HttpURLConnection) imageUrl.openConnection();
                    InputStream inputStream = connection.getInputStream();

                    SVG svg = SVG.getFromInputStream(inputStream);
                    PictureDrawable drawable = new PictureDrawable(svg.renderToPicture());

                    // Atualiza na UI thread
                    if (context instanceof android.app.Activity) {
                        ((android.app.Activity) context).runOnUiThread(() -> {
                            imageView.setImageDrawable(drawable);
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    // Fallback para placeholder em caso de erro
                    if (context instanceof android.app.Activity) {
                        ((android.app.Activity) context).runOnUiThread(() -> {
                            imageView.setImageResource(placeholder);
                        });
                    }
                }
            }).start();
        } else {
            Glide.with(context)
                    .load(url)
                    .placeholder(placeholder)
                    .error(placeholder)
                    .into(imageView);
        }
    }
}