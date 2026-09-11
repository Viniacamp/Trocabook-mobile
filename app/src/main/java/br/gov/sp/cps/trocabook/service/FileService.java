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

    /**
     * Salva uma imagem recebida através de uma Uri
     * no armazenamento interno do aplicativo.
     */
    public void salvarImagem(
            Uri imageUri,
            Callback<String> callback) {

        if (imageUri == null) {
            callback.onError("Imagem inválida");
            return;
        }

        try {

            File pastaCapas =
                    criarPastaCapas();

            String nomeArquivo =
                    "capa_" + System.currentTimeMillis() + ".jpg";

            File arquivo =
                    new File(pastaCapas, nomeArquivo);

            InputStream inputStream =
                    context.getContentResolver()
                            .openInputStream(imageUri);

            if (inputStream == null) {
                callback.onError(
                        "Não foi possível abrir a imagem"
                );
                return;
            }

            OutputStream outputStream =
                    new FileOutputStream(arquivo);

            copiarArquivo(
                    inputStream,
                    outputStream
            );

            inputStream.close();
            outputStream.close();

            callback.onSuccess(
                    arquivo.getAbsolutePath()
            );

        } catch (Exception e) {

            callback.onError(
                    "Erro ao salvar imagem"
            );
        }
    }

    /**
     * Copia uma imagem localizada na pasta assets
     * para o armazenamento interno do aplicativo.
     *
     * @param caminhoAsset caminho relativo dentro de assets
     */
    public void salvarImagemAsset(
            String caminhoAsset,
            Callback<String> callback) {

        if (caminhoAsset == null
                || caminhoAsset.trim().isEmpty()) {

            callback.onError(
                    "Caminho da imagem inválido"
            );
            return;
        }

        try {

            File pastaCapas =
                    criarPastaCapas();

            String nomeArquivo =
                    "capa_" + System.currentTimeMillis() + ".jpg";

            File arquivo =
                    new File(pastaCapas, nomeArquivo);

            InputStream inputStream =
                    context.getAssets()
                            .open(caminhoAsset);

            OutputStream outputStream =
                    new FileOutputStream(arquivo);

            copiarArquivo(
                    inputStream,
                    outputStream
            );

            inputStream.close();
            outputStream.close();

            callback.onSuccess(
                    arquivo.getAbsolutePath()
            );

        } catch (Exception e) {

            callback.onError(
                    "Erro ao salvar imagem dos assets"
            );
        }
    }

    /**
     * Exclui uma imagem salva no armazenamento interno.
     */
    public void deletarImagem(String caminho) {

        if (caminho == null
                || caminho.isEmpty()) {
            return;
        }

        File arquivo =
                new File(caminho);

        if (arquivo.exists()) {
            arquivo.delete();
        }
    }

    private File criarPastaCapas() {

        File pastaCapas =
                new File(
                        context.getFilesDir(),
                        "capas"
                );

        if (!pastaCapas.exists()) {
            pastaCapas.mkdirs();
        }

        return pastaCapas;
    }

    private void copiarArquivo(
            InputStream inputStream,
            OutputStream outputStream)
            throws Exception {

        byte[] buffer =
                new byte[4096];

        int length;

        while ((length =
                inputStream.read(buffer)) != -1) {

            outputStream.write(
                    buffer,
                    0,
                    length
            );
        }

        outputStream.flush();
    }
}