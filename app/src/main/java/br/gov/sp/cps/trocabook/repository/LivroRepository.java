package br.gov.sp.cps.trocabook.repository;

import android.util.Log;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firestore.v1.ListenRequestOrBuilder;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import br.gov.sp.cps.trocabook.BuildConfig;
import br.gov.sp.cps.trocabook.model.Livro;

public class LivroRepository {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    public interface Callback<T> {
        void onSucesso(T livros);
        void onErro(Exception e);
    }

    public void salvarLivro(Livro livro, Callback<Livro> callback) {
        String id = db.collection("livros")
                .document()
                .getId();

        livro.setId(id);
        db.collection("livros")
                .document(livro.getId())
                .set(livro)
                .addOnSuccessListener(doc -> callback.onSucesso(livro))
                .addOnFailureListener(e ->
                        callback.onErro(new Exception("Erro ao salvar livro")));
    }


    public void buscarNoFirebase(String titulo, Callback<List<Livro>> callback) {

        db.collection("livros")
                .whereEqualTo("titulo", titulo)
                .get()
                .addOnSuccessListener(query -> {

                    List<Livro> lista = new ArrayList<>();

                    for (DocumentSnapshot doc : query.getDocuments()) {
                        Livro livro = doc.toObject(Livro.class);
                        lista.add(livro);
                    }

                    callback.onSucesso(lista);
                })
                .addOnFailureListener(callback::onErro);
    }




    public void buscarNaApi(String titulo, Callback<List<Livro>> callback) {

        new Thread(() -> {
            HttpURLConnection conn = null;
            BufferedReader reader = null;

            try {
                String tituloFormatado = URLEncoder.encode(titulo, "UTF-8");

                String url = "https://www.googleapis.com/books/v1/volumes?q="
                        + tituloFormatado
                        + "&key=" + BuildConfig.GOOGLE_API_KEY;

                conn = (HttpURLConnection) new URL(url).openConnection();
                conn.setRequestMethod("GET");

                int responseCode = conn.getResponseCode();

                InputStream stream;

                if (responseCode < 200 || responseCode >= 300) {
                    stream = conn.getErrorStream();
                    throw new Exception("Erro HTTP: " + responseCode);
                }

                stream = conn.getInputStream();

                reader = new BufferedReader(new InputStreamReader(stream));
                StringBuilder result = new StringBuilder();

                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }

                JSONObject json = new JSONObject(result.toString());
                JSONArray itens = json.optJSONArray("items");

                List<Livro> livros = new ArrayList<>();

                if (itens != null) {
                    for (int i = 0; i < itens.length(); i++) {

                        JSONObject obj = itens.getJSONObject(i);
                        String googleId = obj.optString("id", "");

                        JSONObject volume = obj.getJSONObject("volumeInfo");

                        Livro livro = new Livro();

                        if (googleId.isEmpty()) {
                            googleId = db.collection("livros").document().getId();
                        }

                        livro.setId(googleId);
                        livro.setTitulo(volume.optString("title", "Sem título"));


                        String capa = "";
                        if (volume.has("imageLinks")) {
                            capa = volume.getJSONObject("imageLinks")
                                    .optString("thumbnail", "")
                                    .replace("http", "https");
                        }
                        livro.setCapa(capa);


                        List<String> autores = new ArrayList<>();
                        if (volume.has("authors")) {
                            JSONArray autoresJson = volume.getJSONArray("authors");
                            for (int j = 0; j < autoresJson.length(); j++) {
                                autores.add(autoresJson.getString(j));
                            }
                        }
                        livro.setAutores(autores);


                        List<String> categorias = new ArrayList<>();
                        if (volume.has("categories")) {
                            JSONArray categoriasJson = volume.getJSONArray("categories");
                            for (int j = 0; j < categoriasJson.length(); j++) {
                                categorias.add(categoriasJson.getString(j));
                            }
                        }
                        livro.setCategorias(categorias);

                        livros.add(livro);


                        db.collection("livros")
                                .document(googleId)
                                .set(livro);
                    }
                }

                callback.onSucesso(livros);

            } catch (Exception e) {
                Log.e("API", "Erro API", e);
                callback.onErro(e);

            } finally {
                try {
                    if (reader != null) reader.close();
                    if (conn != null) conn.disconnect();
                } catch (Exception ignored) {}
            }
        }).start();
    }

    public void buscarCategoriasNoFirebase(Callback<List<List<String>>> callback) {
        db.collection("livros")
                .get()
                .addOnSuccessListener(query -> {
                    List<List<String>> lista = new ArrayList<>();
                    for (DocumentSnapshot doc : query.getDocuments()) {
                        List<String> categorias = (List<String>) doc.get("categorias");
                        if (categorias != null) {
                            lista.add(categorias);
                        }
                    }
                    callback.onSucesso(lista);
                })
                .addOnFailureListener(callback::onErro);
    }
}

