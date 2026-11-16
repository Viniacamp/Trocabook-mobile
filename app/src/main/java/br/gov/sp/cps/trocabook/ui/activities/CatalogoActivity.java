package br.gov.sp.cps.trocabook.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import br.gov.sp.cps.trocabook.R;
import br.gov.sp.cps.trocabook.model.Anuncio;
import br.gov.sp.cps.trocabook.service.AnuncioService;
import br.gov.sp.cps.trocabook.service.AuthService;
import br.gov.sp.cps.trocabook.service.IaService;
import br.gov.sp.cps.trocabook.ui.adapters.CatalogoAdapter;
import br.gov.sp.cps.trocabook.util.BottomNavigationHelper;

public class CatalogoActivity extends AppCompatActivity {

    private final ExecutorService executorIa = Executors.newSingleThreadExecutor();
    private TextInputEditText campoBusca;
    private Spinner filtroNegociacao;
    private MaterialButton botaoBuscar;
    private RecyclerView recyclerCatalogo;
    private TextView textoVazio;
    private View overlayLoading;
    private CatalogoAdapter adapter;
    private AnuncioService anuncioService;
    private IaService iaService;
    private int identificadorBusca;
    private boolean ignorarSelecaoInicial = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalogo);

        FirebaseUser usuario = new AuthService(this).getUsuarioAtual();
        if (usuario == null) {
            finish();
            return;
        }

        anuncioService = new AnuncioService();
        iaService = new IaService();
        inicializarViews();
        configurarMenu();
        configurarFiltros();
        configurarEventos();
        carregarAnuncios();
    }

    private void inicializarViews() {
        campoBusca = findViewById(R.id.campoBuscaCatalogo);
        filtroNegociacao = findViewById(R.id.spinnerFiltroCatalogo);
        botaoBuscar = findViewById(R.id.btnBuscarCatalogo);
        recyclerCatalogo = findViewById(R.id.recyclerCatalogo);
        textoVazio = findViewById(R.id.textoVazioCatalogo);
        overlayLoading = findViewById(R.id.overlayLoadingCatalogo);

        adapter = new CatalogoAdapter(this::abrirDetalhes);
        recyclerCatalogo.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerCatalogo.setAdapter(adapter);
    }

    private void configurarMenu() {
        BottomNavigationView navigation = findViewById(R.id.bottomNavigation);
        BottomNavigationHelper.configurar(this, navigation, R.id.menu_catalogo);
    }

    private void configurarFiltros() {
        ArrayAdapter<String> opcoes = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                new String[]{"Todos", "Troca", "Venda", "Ambos"}
        );
        opcoes.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        filtroNegociacao.setAdapter(opcoes);
        filtroNegociacao.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(
                    android.widget.AdapterView<?> parent,
                    View view,
                    int position,
                    long id
            ) {
                if (ignorarSelecaoInicial) {
                    ignorarSelecaoInicial = false;
                    return;
                }
                carregarAnuncios();
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
            }
        });
    }

    private void configurarEventos() {
        botaoBuscar.setOnClickListener(view -> carregarAnuncios());
        campoBusca.setOnEditorActionListener((view, actionId, event) -> {
            boolean teclaEnter = event != null
                    && event.getKeyCode() == KeyEvent.KEYCODE_ENTER
                    && event.getAction() == KeyEvent.ACTION_DOWN;
            if (actionId == EditorInfo.IME_ACTION_SEARCH || teclaEnter) {
                carregarAnuncios();
                return true;
            }
            return false;
        });
    }

    private void carregarAnuncios() {
        String consulta = campoBusca.getText() == null
                ? ""
                : campoBusca.getText().toString().trim();
        Anuncio.TipoNegociacao tipo = tipoSelecionado();
        int buscaAtual = ++identificadorBusca;
        mostrarLoading(true);
        textoVazio.setVisibility(View.GONE);
        recyclerCatalogo.setVisibility(View.GONE);

        AnuncioService.Callback<List<Anuncio>> callback = new AnuncioService.Callback<List<Anuncio>>() {
            @Override
            public void onSuccess(List<Anuncio> anuncios) {
                if (isFinishing() || isDestroyed()) {
                    return;
                }
                List<Anuncio> resultado = anuncios == null
                        ? new ArrayList<>()
                        : new ArrayList<>(anuncios);
                if (consulta.isEmpty()) {
                    exibirResultados(buscaAtual, resultado);
                    return;
                }
                executorIa.execute(() -> {
                    List<Anuncio> ranqueados = ranquearAnuncios(consulta, resultado);
                    runOnUiThread(() -> exibirResultados(buscaAtual, ranqueados));
                });
            }

            @Override
            public void onError(String mensagem) {
                runOnUiThread(() -> {
                    if (buscaAtual != identificadorBusca) {
                        return;
                    }
                    mostrarLoading(false);
                    Toast.makeText(CatalogoActivity.this, mensagem, Toast.LENGTH_LONG).show();
                    mostrarVazio(true);
                });
            }
        };

        if (tipo == null) {
            anuncioService.buscarTodosOsAnuncios(callback);
        } else {
            anuncioService.buscarAnunciosPorTipo(tipo, callback);
        }
    }

    private List<Anuncio> ranquearAnuncios(String consulta, List<Anuncio> anuncios) {
        String consultaNormalizada = iaService.normalizarTexto(consulta);
        boolean fraseSemantica = consultaNormalizada.split("\\s+").length >= 3
                || consultaNormalizada.contains("?");
        List<String> termos = fraseSemantica
                ? iaService.extrairTermosBuscaSemantica(consulta)
                : iaService.extrairTokensLocais(consulta);

        List<AnuncioPontuado> resultados = pontuarAnuncios(
                anuncios, consultaNormalizada, termos, false
        );

        if (resultados.isEmpty() && !fraseSemantica) {
            List<String> termosIa = iaService.extrairTermosBuscaSemantica(consulta);
            resultados = pontuarAnuncios(anuncios, consultaNormalizada, termosIa, true);
        }

        resultados.sort(Comparator.comparingInt(AnuncioPontuado::pontuacao).reversed());
        List<Anuncio> ordenados = new ArrayList<>();
        for (AnuncioPontuado resultado : resultados) {
            ordenados.add(resultado.anuncio());
        }
        return ordenados;
    }

    private List<AnuncioPontuado> pontuarAnuncios(
            List<Anuncio> anuncios,
            String consulta,
            List<String> termos,
            boolean buscaExpandida
    ) {
        List<AnuncioPontuado> resultados = new ArrayList<>();
        for (Anuncio anuncio : anuncios) {
            String titulo = iaService.normalizarTexto(anuncio.getTitulo());
            String descricao = iaService.normalizarTexto(anuncio.getDescricao());
            int pontos = 0;
            if (!buscaExpandida) {
                if (titulo.contains(consulta)) pontos += 100;
                if (descricao.contains(consulta)) pontos += 40;
            }
            for (String termo : termos) {
                if (termo.length() < 2) {
                    continue;
                }
                if (buscaExpandida) {
                    if (titulo.contains(termo) || descricao.contains(termo)) {
                        pontos += 15;
                    }
                } else {
                    if (titulo.contains(termo)) pontos += 25;
                    if (descricao.contains(termo)) pontos += 10;
                }
            }
            if (pontos > 0) {
                resultados.add(new AnuncioPontuado(anuncio, pontos));
            }
        }
        return resultados;
    }

    private Anuncio.TipoNegociacao tipoSelecionado() {
        switch (filtroNegociacao.getSelectedItemPosition()) {
            case 1:
                return Anuncio.TipoNegociacao.TROCA;
            case 2:
                return Anuncio.TipoNegociacao.VENDA;
            case 3:
                return Anuncio.TipoNegociacao.AMBOS;
            default:
                return null;
        }
    }

    private void exibirResultados(int buscaAtual, List<Anuncio> anuncios) {
        if (buscaAtual != identificadorBusca || isFinishing()) {
            return;
        }
        mostrarLoading(false);
        adapter.atualizarLista(anuncios);
        mostrarVazio(anuncios.isEmpty());
    }

    private void mostrarVazio(boolean vazio) {
        textoVazio.setVisibility(vazio ? View.VISIBLE : View.GONE);
        recyclerCatalogo.setVisibility(vazio ? View.GONE : View.VISIBLE);
    }

    private void mostrarLoading(boolean mostrar) {
        overlayLoading.setVisibility(mostrar ? View.VISIBLE : View.GONE);
        botaoBuscar.setEnabled(!mostrar);
        filtroNegociacao.setEnabled(!mostrar);
    }

    private void abrirDetalhes(Anuncio anuncio) {
        Intent intent = new Intent(this, DetalhesAnuncioActivity.class);
        intent.putExtra("anuncioId", anuncio.getId());
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        executorIa.shutdownNow();
        super.onDestroy();
    }

    private static class AnuncioPontuado {
        private final Anuncio anuncio;
        private final int pontuacao;

        AnuncioPontuado(Anuncio anuncio, int pontuacao) {
            this.anuncio = anuncio;
            this.pontuacao = pontuacao;
        }

        Anuncio anuncio() {
            return anuncio;
        }

        int pontuacao() {
            return pontuacao;
        }
    }
}
