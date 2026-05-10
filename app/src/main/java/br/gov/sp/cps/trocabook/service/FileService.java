package br.gov.sp.cps.trocabook.service;

import android.content.Context;
import android.net.Uri;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class FileService {

    private final Context context;

    public FileService(Context context) {
        this.context = context;
    }

    public interface Callback<T> {
        void onSuccess(T resultado);
        void onError(String erro);
    }

    public void salvarImagem(Uri imageUri, Callback<String> callback) {

        if (imageUri == null) {
            callback.onError("Imagem inválida");
            return;
        }

        try {

            File pastaCapas =
                    new File(context.getFilesDir(), "capas");

            if (!pastaCapas.exists()) {
                pastaCapas.mkdirs();
            }

            String nomeArquivo =
                    "capa_" + System.currentTimeMillis() + ".jpg";

            File arquivo =
                    new File(pastaCapas, nomeArquivo);

            InputStream inputStream =
                    context.getContentResolver()
                            .openInputStream(imageUri);

            OutputStream outputStream =
                    new FileOutputStream(arquivo);

            byte[] buffer = new byte[1024];
            int length;

            while ((length = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, length);
            }

            inputStream.close();
            outputStream.close();

            callback.onSuccess(arquivo.getAbsolutePath());

        } catch (Exception e) {
            callback.onError("Erro ao salvar imagem");
        }
    }

    public void deletarImagem(String caminho) {

        if (caminho == null || caminho.isEmpty()) {
            return;
        }

        File arquivo = new File(caminho);

        if (arquivo.exists()) {
            arquivo.delete();
        }
    }
}