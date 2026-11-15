package br.gov.sp.cps.trocabook.service;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import br.gov.sp.cps.trocabook.service.fileAzure.AzureApi;
import br.gov.sp.cps.trocabook.service.fileAzure.AzureRetrofitClient;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Response;

public class FileService {

    private static final String TAG = "FILE_SERVICE";
    private final Context context;

    public FileService(Context context) {
        this.context = context;
    }

    public interface Callback<T> {
        void onSuccess(T resultado);
        void onError(String erro);
    }

    /**
     * Tenta enviar a imagem para o Azure. Se falhar ou estiver fora do ar,
     * faz o fallback salvando localmente no armazenamento interno do app.
     */
    public void salvarImagem(
            Uri imageUri,
            Callback<String> callback) {

        if (imageUri == null) {
            callback.onError("Imagem inválida");
            return;
        }

        try {
            File arquivoTemp = criarArquivoTemporario(imageUri);
            if (arquivoTemp == null) {
                callback.onError("Não foi possível processar a imagem");
                return;
            }

            RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), arquivoTemp);
            MultipartBody.Part body = MultipartBody.Part.createFormData("file", arquivoTemp.getName(), requestFile);

            AzureApi azureApi = AzureRetrofitClient.getInstance().create(AzureApi.class);

            // Chamada com o retrofit2.Callback corrigido
            azureApi.uploadFotoUsuario(body).enqueue(new retrofit2.Callback<String>() {
                @Override
                public void onResponse(Call<String> call, Response<String> response) {
                    String respostaServidor = response.body();
                    if (response.isSuccessful() && respostaServidor != null && !respostaServidor.contains("<!DOCTYPE html>")) {
                        Log.d(TAG, "Upload para o Azure realizado com sucesso!");
                        arquivoTemp.delete();
                        callback.onSuccess(response.body());
                    } else {
                        Log.w(TAG, "Azure retornou erro (HTTP " + response.code() + "). Usando fallback local.");
                        callback.onSuccess(arquivoTemp.getAbsolutePath());
                    }
                }

                @Override
                public void onFailure(Call<String> call, Throwable t) {
                    Log.w(TAG, "Azure fora do ar ou sem internet (" + t.getMessage() + "). Usando fallback local.");
                    callback.onSuccess(arquivoTemp.getAbsolutePath());
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "Erro geral no upload, tentando salvar localmente", e);
            salvarLocalmenteFallback(imageUri, callback);
        }
    }

    private File criarArquivoTemporario(Uri imageUri) {
        try {
            File pastaCapas = criarPastaCapas();
            String nomeArquivo = "perfil_" + System.currentTimeMillis() + ".jpg";
            File arquivo = new File(pastaCapas, nomeArquivo);

            InputStream inputStream = context.getContentResolver().openInputStream(imageUri);
            if (inputStream == null) return null;

            OutputStream outputStream = new FileOutputStream(arquivo);
            copiarArquivo(inputStream, outputStream);

            inputStream.close();
            outputStream.close();

            return arquivo;
        } catch (Exception e) {
            return null;
        }
    }

    private void salvarLocalmenteFallback(Uri imageUri, Callback<String> callback) {
        try {
            File arquivo = criarArquivoTemporario(imageUri);
            if (arquivo != null) {
                callback.onSuccess(arquivo.getAbsolutePath());
            } else {
                callback.onError("Erro ao salvar imagem localmente");
            }
        } catch (Exception e) {
            callback.onError("Erro ao salvar imagem: " + e.getMessage());
        }
    }

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

    /**
     * Tenta enviar a capa do livro para o Azure. Se falhar ou estiver fora do ar,
     * faz o fallback salvando localmente no armazenamento interno do app.
     */
    public void salvarCapaLivro(
            Uri imageUri,
            Callback<String> callback) {

        if (imageUri == null) {
            callback.onError("Imagem inválida");
            return;
        }

        try {
            File arquivoTemp = criarArquivoTemporarioParaLivro(imageUri);
            if (arquivoTemp == null) {
                callback.onError("Não foi possível processar a imagem do livro");
                return;
            }

            RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), arquivoTemp);
            MultipartBody.Part body = MultipartBody.Part.createFormData("file", arquivoTemp.getName(), requestFile);

            AzureApi azureApi = AzureRetrofitClient.getInstance().create(AzureApi.class);

            // Chamada para o endpoint de upload de livro no Azure
            azureApi.uploadLivro(body).enqueue(new retrofit2.Callback<String>() {
                @Override
                public void onResponse(Call<String> call, Response<String> response) {
                    String respostaServidor = response.body();
                    Log.d(TAG, "Resposta do servidor: " + respostaServidor);

                    // Verifica se a resposta é válida, se veio com sucesso e SE NÃO É UM HTML
                    if (response.isSuccessful() && respostaServidor != null && !respostaServidor.contains("<!DOCTYPE html>")) {
                        Log.d(TAG, "Upload da capa do livro para o Azure realizado com sucesso!");
                        arquivoTemp.delete();
                        callback.onSuccess(respostaServidor);
                    } else {
                        // Se veio HTML (ex: tela de login) ou erro, trata como falha e usa o fallback local!
                        Log.w(TAG, "Azure retornou erro (HTTP " + response.code() + "). Usando fallback local.");
                        callback.onSuccess(arquivoTemp.getAbsolutePath());
                    }
                }

                @Override
                public void onFailure(Call<String> call, Throwable t) {
                    Log.w(TAG, "Azure fora do ar ao enviar livro. Usando fallback local.");
                    callback.onSuccess(arquivoTemp.getAbsolutePath());
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "Erro geral no upload do livro, tentando salvar localmente", e);
            salvarLocalmenteFallback(imageUri, callback);
        }
    }

    private File criarArquivoTemporarioParaLivro(Uri imageUri) {
        try {
            File pastaCapas = criarPastaCapas();
            String nomeArquivo = "livro_" + System.currentTimeMillis() + ".jpg";
            File arquivo = new File(pastaCapas, nomeArquivo);

            InputStream inputStream = context.getContentResolver().openInputStream(imageUri);
            if (inputStream == null) return null;

            OutputStream outputStream = new FileOutputStream(arquivo);
            copiarArquivo(inputStream, outputStream);

            inputStream.close();
            outputStream.close();

            return arquivo;
        } catch (Exception e) {
            return null;
        }
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