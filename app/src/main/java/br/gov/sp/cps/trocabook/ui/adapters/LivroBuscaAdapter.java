package br.gov.sp.cps.trocabook.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

import br.gov.sp.cps.trocabook.R;
import br.gov.sp.cps.trocabook.model.dto.LivroBuscaOutput;

public class LivroBuscaAdapter
        extends RecyclerView.Adapter<LivroBuscaAdapter.LivroBuscaViewHolder> {

    private final List<LivroBuscaOutput> listaLivros;
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(LivroBuscaOutput livro);
    }

    public LivroBuscaAdapter(
            List<LivroBuscaOutput> listaLivros,
            OnItemClickListener listener) {

        this.listaLivros = listaLivros;
        this.listener = listener;
    }

    @NonNull
    @Override
    public LivroBuscaViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.livro, parent, false);

        return new LivroBuscaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull LivroBuscaViewHolder holder,
            int position) {

        LivroBuscaOutput livro = listaLivros.get(position);

        holder.titulo.setText(livro.getTitulo());

        Glide.with(holder.itemView.getContext())
                .load(livro.getUrlImagem())
                .placeholder(R.drawable.placeholder_capa)
                .into(holder.capa);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(livro);
            }
        });
    }

    @Override
    public int getItemCount() {
        return listaLivros.size();
    }

    public void atualizarLista(List<LivroBuscaOutput> novaLista) {

        listaLivros.clear();

        if (novaLista != null) {
            listaLivros.addAll(novaLista);
        }

        notifyDataSetChanged();
    }

    static class LivroBuscaViewHolder extends RecyclerView.ViewHolder {

        ImageView capa;
        TextView titulo;

        public LivroBuscaViewHolder(@NonNull View itemView) {
            super(itemView);

            capa = itemView.findViewById(R.id.capaLivro);
            titulo = itemView.findViewById(R.id.tituloLivro);
        }
    }
}