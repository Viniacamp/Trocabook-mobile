package br.gov.sp.cps.trocabook.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

import br.gov.sp.cps.trocabook.R;
import br.gov.sp.cps.trocabook.model.Anuncio;
import br.gov.sp.cps.trocabook.util.ImageUtils;
import br.gov.sp.cps.trocabook.util.SvgLoader;

public class CatalogoAdapter extends RecyclerView.Adapter<CatalogoAdapter.AnuncioViewHolder> {

    public interface OnAnuncioClickListener {
        void onAnuncioClick(Anuncio anuncio);
    }

    private final List<Anuncio> anuncios = new ArrayList<>();
    private final OnAnuncioClickListener listener;

    public CatalogoAdapter(OnAnuncioClickListener listener) {
        this.listener = listener;
    }

    public void atualizarLista(List<Anuncio> novosAnuncios) {
        anuncios.clear();
        if (novosAnuncios != null) {
            anuncios.addAll(novosAnuncios);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public AnuncioViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View item = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_catalogo_anuncio, parent, false);
        return new AnuncioViewHolder(item);
    }

    @Override
    public void onBindViewHolder(@NonNull AnuncioViewHolder holder, int position) {
        Anuncio anuncio = anuncios.get(position);
        holder.titulo.setText(anuncio.getTitulo());
        holder.usuario.setText(anuncio.getNomeUsuario() == null
                ? "Usuário Trocabook"
                : anuncio.getNomeUsuario());

        SvgLoader.carregar(
                holder.itemView.getContext(),
                ImageUtils.obterUrlImagem(anuncio.getCapa()),
                holder.capa,
                R.drawable.placeholder_capa
        );
        SvgLoader.carregar(
                holder.itemView.getContext(),
                ImageUtils.obterUrlImagem(anuncio.getFotoPerfil()),
                holder.fotoUsuario,
                R.drawable.placeholder_usuario,
                true
        );

        holder.detalhes.setOnClickListener(view -> listener.onAnuncioClick(anuncio));
        holder.itemView.setOnClickListener(view -> listener.onAnuncioClick(anuncio));
    }

    @Override
    public int getItemCount() {
        return anuncios.size();
    }

    static class AnuncioViewHolder extends RecyclerView.ViewHolder {
        final ImageView capa;
        final ImageView fotoUsuario;
        final TextView titulo;
        final TextView usuario;
        final MaterialButton detalhes;

        AnuncioViewHolder(@NonNull View itemView) {
            super(itemView);
            capa = itemView.findViewById(R.id.imgCapaCatalogo);
            fotoUsuario = itemView.findViewById(R.id.imgUsuarioCatalogo);
            titulo = itemView.findViewById(R.id.textTituloCatalogo);
            usuario = itemView.findViewById(R.id.textUsuarioCatalogo);
            detalhes = itemView.findViewById(R.id.btnDetalhesCatalogo);
        }
    }
}
