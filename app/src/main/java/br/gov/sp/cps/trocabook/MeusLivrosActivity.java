package br.gov.sp.cps.trocabook;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class MeusLivrosActivity extends AppCompatActivity {

    private RecyclerView recyclerAnuncios;
    private RecyclerView recyclerNegociados;

    private LivroAdapter adapterAnuncios;
    private LivroAdapter adapterNegociados;

    private List<Livro> listaAnuncios = new ArrayList<>();
    private List<Livro> listaNegociados = new ArrayList<>();

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    private TextView txtVazioAnuncios;
    private TextView txtVazioNegociados;
    private Button btnVoltar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meuslivros);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        if (mAuth.getCurrentUser() == null) {
            finish();
            return;
        }

        String userId = mAuth.getCurrentUser().getUid();

        recyclerAnuncios = findViewById(R.id.recyclerAnuncios);
        recyclerNegociados = findViewById(R.id.recyclerNegociados);

        txtVazioAnuncios = findViewById(R.id.txtVazioAnuncios);
        txtVazioNegociados = findViewById(R.id.txtVazioNegociados);

        recyclerAnuncios.setLayoutManager(new LinearLayoutManager(this));
        recyclerNegociados.setLayoutManager(new LinearLayoutManager(this));

        adapterAnuncios = new LivroAdapter(listaAnuncios, livro -> {});
        adapterNegociados = new LivroAdapter(listaNegociados, livro -> {});

        recyclerAnuncios.setAdapter(adapterAnuncios);
        recyclerNegociados.setAdapter(adapterNegociados);

        btnVoltar = findViewById(R.id.btnVoltar);

        carregarAnuncios(userId);
        carregarNegociacoes(userId);

        btnVoltar.setOnClickListener(v ->{
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        });
    }

    private void carregarAnuncios(String userId) {

        db.collection("anuncios")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(snapshot -> {

                    listaAnuncios.clear();

                    if (snapshot.isEmpty()) {
                        txtVazioAnuncios.setVisibility(View.VISIBLE);
                        return;
                    }

                    txtVazioAnuncios.setVisibility(View.GONE);

                    snapshot.forEach(doc -> {
                        Livro livro = doc.toObject(Livro.class);
                        listaAnuncios.add(livro);
                    });

                    adapterAnuncios.notifyDataSetChanged();
                });
    }

    private void carregarNegociacoes(String userId) {

        db.collection("negociacoes")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(snapshot -> {

                    listaNegociados.clear();

                    if (snapshot.isEmpty()) {
                        txtVazioNegociados.setVisibility(View.VISIBLE);
                        return;
                    }

                    txtVazioNegociados.setVisibility(View.GONE);

                    snapshot.forEach(doc -> {
                        Livro livro = doc.toObject(Livro.class);
                        listaNegociados.add(livro);
                    });

                    adapterNegociados.notifyDataSetChanged();
                });
    }
}