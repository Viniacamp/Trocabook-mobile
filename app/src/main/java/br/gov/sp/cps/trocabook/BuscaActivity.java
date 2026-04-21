package br.gov.sp.cps.trocabook;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class BuscaActivity extends AppCompatActivity {

    private TextInputLayout textLayoutTitulo;
    private TextInputEditText textEditTitulo;
    private Button btnBuscar;
    private RecyclerView recyclerViewLivros;

    private LivroAdapter livroAdapter;
    private List<Livro> listaLivros = new ArrayList<>();

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_busca_livro);


        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        if (mAuth.getCurrentUser() == null) {
            finish();
            return;
        }

        textLayoutTitulo = findViewById(R.id.textLayoutTitulo);
        textEditTitulo = findViewById(R.id.textEditTitulo);
        btnBuscar = findViewById(R.id.btnBuscar);
        recyclerViewLivros = findViewById(R.id.recyclerViewLivros);


        recyclerViewLivros.setLayoutManager(new LinearLayoutManager(this));

        livroAdapter = new LivroAdapter(listaLivros, livro -> {


            Intent intent = new Intent(BuscaActivity.this, AnuncioActivity.class);
            intent.putExtra("livro", (Serializable) livro);
            startActivity(intent);
        });

        recyclerViewLivros.setAdapter(livroAdapter);


        btnBuscar.setOnClickListener(v -> {

            String tituloBuscado = textEditTitulo.getText().toString().trim();

            if (tituloBuscado.isEmpty()) {
                textLayoutTitulo.setError("Digite um título");
                return;
            }

            textLayoutTitulo.setError(null);

            buscarNoFirebaseOuApi(tituloBuscado);
        });
    }


    private void buscarNoFirebaseOuApi(String titulo) {

        db.collection("livros")
                .whereEqualTo("titulo", titulo)
                .get()
                .addOnSuccessListener(query -> {

                    if (!query.isEmpty()) {

                        List<Livro> lista = new ArrayList<>();

                        for (DocumentSnapshot doc : query.getDocuments()) {
                            Livro livro = doc.toObject(Livro.class);
                            lista.add(livro);
                        }

                        atualizarLista(lista);

                    } else {

                        buscarNaApiESalvar(titulo);
                    }
                })
                .addOnFailureListener(e ->
                        Log.e("FIREBASE", "Erro ao buscar", e)
                );
    }


    private void buscarNaApiESalvar(String titulo) {

        new Thread(() -> {
            HttpURLConnection conn = null;
            BufferedReader reader = null;

            try {
                if (titulo == null || titulo.trim().isEmpty()) {
                    Log.e("API", "Título vazio");
                    return;
                }

                String tituloFormatado = URLEncoder.encode(titulo, "UTF-8");

                String url = "https://www.googleapis.com/books/v1/volumes?q="
                        + tituloFormatado
                        + "&key=" + BuildConfig.GOOGLE_API_KEY;

                conn = (HttpURLConnection) new URL(url).openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(10000);
                conn.setReadTimeout(10000);

                int responseCode = conn.getResponseCode();

                InputStream stream;

                // Se der erro na API
                if (responseCode < 200 || responseCode >= 300) {

                    stream = conn.getErrorStream();

                    if (stream != null) {
                        reader = new BufferedReader(new InputStreamReader(stream));
                        StringBuilder error = new StringBuilder();

                        String line;
                        while ((line = reader.readLine()) != null) {
                            error.append(line);
                        }

                        Log.e("API", "Erro HTTP " + responseCode + ": " + error.toString());
                    } else {
                        Log.e("API", "Erro HTTP " + responseCode + " sem resposta");
                    }

                    return;
                }

                // Sucesso
                stream = conn.getInputStream();

                reader = new BufferedReader(new InputStreamReader(stream));
                StringBuilder result = new StringBuilder();

                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }

                JSONObject json = new JSONObject(result.toString());

                JSONArray itens = json.optJSONArray("items");

                if (itens == null) {
                    Log.e("API", "Nenhum livro encontrado: " + json.toString());
                    return;
                }

                List<Livro> novosLivros = new ArrayList<>();

                for (int i = 0; i < itens.length(); i++) {

                    JSONObject obj = itens.getJSONObject(i);

                    String googleId = obj.optString("id", "");

                    JSONObject volume = obj.getJSONObject("volumeInfo");

                    Livro livro = new Livro();

                    // ID seguro
                    if (googleId.isEmpty()) {
                        googleId = db.collection("livros").document().getId();
                    }

                    livro.setId(googleId);
                    livro.setTitulo(volume.optString("title", "Sem título"));

                    // CAPA
                    String capa = "";
                    if (volume.has("imageLinks")) {
                        capa = volume.getJSONObject("imageLinks")
                                .optString("thumbnail", "")
                                .replace("http", "https");
                    }
                    livro.setCapa(capa);

                    // AUTORES
                    List<String> autores = new ArrayList<>();
                    if (volume.has("authors")) {
                        JSONArray autoresJson = volume.getJSONArray("authors");
                        for (int j = 0; j < autoresJson.length(); j++) {
                            autores.add(autoresJson.getString(j));
                        }
                    }
                    livro.setAutores(autores);

                    // CATEGORIAS
                    List<String> categorias = new ArrayList<>();
                    if (volume.has("categories")) {
                        JSONArray categoriasJson = volume.getJSONArray("categories");
                        for (int j = 0; j < categoriasJson.length(); j++) {
                            categorias.add(categoriasJson.getString(j));
                        }
                    }
                    livro.setCategorias(categorias);

                    novosLivros.add(livro);

                    // SALVAR NO FIRESTORE
                    db.collection("livros")
                            .document(googleId)
                            .set(livro)
                            .addOnFailureListener(e ->
                                    Log.e("FIRESTORE", "Erro ao salvar livro", e)
                            );
                }

                runOnUiThread(() -> atualizarLista(novosLivros));

            } catch (Exception e) {
                Log.e("API", "Erro na requisição", e);

            } finally {
                try {
                    if (reader != null) reader.close();
                    if (conn != null) conn.disconnect();
                } catch (Exception ignored) {}
            }
        }).start();
    }


    private void atualizarLista(List<Livro> livros) {
        listaLivros.clear();
        listaLivros.addAll(livros);
        livroAdapter.notifyDataSetChanged();
    }
}