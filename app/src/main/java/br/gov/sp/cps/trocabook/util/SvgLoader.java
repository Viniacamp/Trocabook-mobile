package br.gov.sp.cps.trocabook.util;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Picture;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.caverock.androidsvg.SVG;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import br.gov.sp.cps.trocabook.R;

public final class SvgLoader {

    private static final int MAX_IMAGE_BYTES = 8 * 1024 * 1024;
    private static final int MAX_SVG_DIMENSION = 512;
    private static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(3);
    private static final Handler MAIN_HANDLER = new Handler(Looper.getMainLooper());

    private SvgLoader() {
    }

    public static void carregarSvg(
            Context context,
            String url,
            ImageView imageView,
            int placeholder
    ) {
        carregar(context, url, imageView, placeholder, false);
    }

    public static void carregar(
            Context context,
            String source,
            ImageView imageView,
            int placeholder
    ) {
        carregar(context, source, imageView, placeholder, false);
    }

    public static void carregar(
            Context context,
            String source,
            ImageView imageView,
            int placeholder,
            boolean circular
    ) {
        imageView.setImageResource(placeholder);
        imageView.setTag(R.id.svg_loader_request_source, source);

        if (source == null || source.trim().isEmpty()) {
            return;
        }

        String normalized = source.trim();
        if (!isFetchableSource(normalized)) {
            carregarComGlide(imageView, Uri.parse(normalized), placeholder, circular);
            return;
        }

        EXECUTOR.execute(() -> {
            Bitmap bitmap;
            try {
                ImageData imageData = readImage(context.getApplicationContext(), normalized);
                if (isSvg(imageData.bytes, imageData.contentType)) {
                    bitmap = renderSvg(imageData.bytes, MAX_SVG_DIMENSION, MAX_SVG_DIMENSION);
                } else {
                    bitmap = decodeRaster(imageData.bytes, MAX_SVG_DIMENSION, MAX_SVG_DIMENSION);
                    if (bitmap == null) {
                        throw new IOException("Formato de imagem não suportado.");
                    }
                }
            } catch (IOException | RuntimeException e) {
                MAIN_HANDLER.post(() -> {
                    if (normalized.equals(imageView.getTag(R.id.svg_loader_request_source))) {
                        imageView.setImageResource(placeholder);
                    }
                });
                return;
            }

            Bitmap loadedBitmap = bitmap;
            MAIN_HANDLER.post(() -> {
                if (!normalized.equals(imageView.getTag(R.id.svg_loader_request_source))
                        || (context instanceof Activity
                        && (((Activity) context).isFinishing()
                        || ((Activity) context).isDestroyed()))) {
                    return;
                }
                carregarComGlide(imageView, loadedBitmap, placeholder, circular);
            });
        });
    }

    public static Bitmap baixarBitmap(
            Context context,
            String source,
            int width,
            int height,
            boolean circular
    ) throws IOException {
        if (source == null || source.trim().isEmpty()) {
            return null;
        }

        ImageData imageData = readImage(context.getApplicationContext(), source.trim());
        Bitmap bitmap;
        if (isSvg(imageData.bytes, imageData.contentType)) {
            bitmap = renderSvg(imageData.bytes, width, height);
        } else {
            BitmapFactory.Options bounds = new BitmapFactory.Options();
            bounds.inJustDecodeBounds = true;
            BitmapFactory.decodeByteArray(imageData.bytes, 0, imageData.bytes.length, bounds);
            if (bounds.outWidth <= 0 || bounds.outHeight <= 0) {
                throw new IOException("Formato de imagem não suportado.");
            }

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = calculateSampleSize(bounds, width, height);
            bitmap = BitmapFactory.decodeByteArray(
                    imageData.bytes,
                    0,
                    imageData.bytes.length,
                    options
            );
            if (bitmap == null) {
                throw new IOException("Não foi possível decodificar a imagem.");
            }
        }

        Bitmap resized = Bitmap.createScaledBitmap(bitmap, width, height, true);
        if (resized != bitmap) {
            bitmap.recycle();
        }
        return circular ? circularCrop(resized) : resized;
    }

    public static Bitmap bitmapPlaceholder(
            Context context,
            int drawableId,
            int width,
            int height,
            boolean circular
    ) {
        Drawable drawable = context.getDrawable(drawableId);
        if (drawable == null) {
            return null;
        }
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(new Rect(0, 0, width, height));
        drawable.draw(canvas);
        return circular ? circularCrop(bitmap) : bitmap;
    }

    private static ImageData readImage(Context context, String source) throws IOException {
        Uri uri = Uri.parse(source);
        String scheme = uri.getScheme();
        if (scheme != null && "content".equalsIgnoreCase(scheme)) {
            String contentType = context.getContentResolver().getType(uri);
            try (InputStream input = context.getContentResolver().openInputStream(uri)) {
                if (input == null) {
                    throw new IOException("Não foi possível abrir o arquivo de imagem.");
                }
                return new ImageData(readLimited(input), contentType);
            }
        }
        if (scheme != null && "file".equalsIgnoreCase(scheme)) {
            try (InputStream input = new FileInputStream(new File(uri.getPath()))) {
                return new ImageData(readLimited(input), null);
            }
        }

        HttpURLConnection connection = (HttpURLConnection) new URL(source).openConnection();
        connection.setConnectTimeout(10000);
        connection.setReadTimeout(15000);
        connection.setInstanceFollowRedirects(true);
        try {
            connection.connect();
            int status = connection.getResponseCode();
            if (status < HttpURLConnection.HTTP_OK
                    || status >= HttpURLConnection.HTTP_MULT_CHOICE) {
                throw new IOException("Falha HTTP ao carregar a imagem: " + status);
            }
            if (connection.getContentLengthLong() > MAX_IMAGE_BYTES) {
                throw new IOException("A imagem excede o tamanho permitido.");
            }
            try (InputStream input = connection.getInputStream()) {
                return new ImageData(readLimited(input), connection.getContentType());
            }
        } finally {
            connection.disconnect();
        }
    }

    private static byte[] readLimited(InputStream input) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] buffer = new byte[8192];
        int total = 0;
        int read;
        while ((read = input.read(buffer)) != -1) {
            total += read;
            if (total > MAX_IMAGE_BYTES) {
                throw new IOException("A imagem excede o tamanho permitido.");
            }
            output.write(buffer, 0, read);
        }
        return output.toByteArray();
    }

    private static boolean isFetchableSource(String source) {
        String lowerCase = source.toLowerCase(Locale.ROOT);
        return lowerCase.startsWith("http://")
                || lowerCase.startsWith("https://")
                || lowerCase.startsWith("content://")
                || lowerCase.startsWith("file://");
    }

    private static boolean isSvg(byte[] bytes, String contentType) {
        if (contentType != null
                && contentType.toLowerCase(Locale.ROOT).contains("image/svg+xml")) {
            return true;
        }

        String prefix = new String(
                bytes,
                0,
                Math.min(bytes.length, 2048),
                StandardCharsets.UTF_8
        ).replace("\uFEFF", "").trim().toLowerCase(Locale.ROOT);
        return prefix.startsWith("<svg")
                || (prefix.startsWith("<?xml") && prefix.contains("<svg"));
    }

    private static Bitmap renderSvg(byte[] bytes, int width, int height) throws IOException {
        SVG svg;
        try {
            svg = SVG.getFromInputStream(new ByteArrayInputStream(bytes));
        } catch (com.caverock.androidsvg.SVGParseException e) {
            throw new IOException("Não foi possível interpretar o SVG.", e);
        }
        Picture picture = svg.renderToPicture();
        int pictureWidth = picture.getWidth();
        int pictureHeight = picture.getHeight();
        if (pictureWidth <= 0 || pictureHeight <= 0) {
            throw new IOException("O SVG não possui dimensões válidas.");
        }

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        float scale = Math.min(width / (float) pictureWidth, height / (float) pictureHeight);
        float left = (width - pictureWidth * scale) / 2f;
        float top = (height - pictureHeight * scale) / 2f;
        canvas.translate(left, top);
        canvas.scale(scale, scale);
        canvas.drawPicture(picture);
        return bitmap;
    }

    private static Bitmap decodeRaster(byte[] bytes, int targetWidth, int targetHeight) {
        BitmapFactory.Options bounds = new BitmapFactory.Options();
        bounds.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(bytes, 0, bytes.length, bounds);
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) {
            return null;
        }
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = calculateSampleSize(bounds, targetWidth, targetHeight);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);
    }

    private static int calculateSampleSize(
            BitmapFactory.Options bounds,
            int targetWidth,
            int targetHeight
    ) {
        int sampleSize = 1;
        while (bounds.outWidth / (sampleSize * 2) >= targetWidth
                && bounds.outHeight / (sampleSize * 2) >= targetHeight) {
            sampleSize *= 2;
        }
        return sampleSize;
    }

    private static Bitmap circularCrop(Bitmap source) {
        int size = Math.min(source.getWidth(), source.getHeight());
        int left = (source.getWidth() - size) / 2;
        int top = (source.getHeight() - size) / 2;
        Bitmap output = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        Path circle = new Path();
        circle.addCircle(size / 2f, size / 2f, size / 2f, Path.Direction.CW);
        canvas.clipPath(circle);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
        canvas.drawBitmap(source, -left, -top, paint);
        if (output != source) {
            source.recycle();
        }
        return output;
    }

    private static void carregarComGlide(
            ImageView imageView,
            Object model,
            int placeholder,
            boolean circular
    ) {
        RequestBuilder<android.graphics.drawable.Drawable> request = Glide.with(imageView)
                .load(model)
                .placeholder(placeholder)
                .error(placeholder);
        if (circular) {
            request.circleCrop();
        }
        request.into(imageView);
    }

    private static class ImageData {
        private final byte[] bytes;
        private final String contentType;

        private ImageData(byte[] bytes, String contentType) {
            this.bytes = bytes;
            this.contentType = contentType;
        }
    }
}
