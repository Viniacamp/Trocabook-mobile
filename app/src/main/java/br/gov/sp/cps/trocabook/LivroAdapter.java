package br.gov.sp.cps.trocabook;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class LivroAdapter extends RecyclerView.Adapter<LivroAdapter.LivroViewHolder> {

    private List<Livro> listaLivros;
    private OnItemClickListener listener;


    public interface OnItemClickListener {
        void onItemClick(Livro livro);
    }

    public LivroAdapter(List<Livro> listaLivros, OnItemClickListener listener) {
        this.listaLivros = listaLivros;
        this.listener = listener;
    }

    @NonNull
    @Override
    public LivroViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.livro, parent, false);
        return new LivroViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LivroViewHolder holder, int position) {
        Livro livro = listaLivros.get(position);

        holder.titulo.setText(livro.getTitulo());

        Glide.with(holder.itemView.getContext())
                .load(livro.getCapa())
                .placeholder(R.drawable.ic_launcher_foreground)
                .into(holder.capa);

        holder.itemView.setOnClickListener(v -> listener.onItemClick(livro));
    }

    @Override
    public int getItemCount() {
        return listaLivros.size();
    }

    public void atualizarLista(List<Livro> novaLista) {
        this.listaLivros = novaLista;
        notifyDataSetChanged();
    }

    static class LivroViewHolder extends RecyclerView.ViewHolder {

        ImageView capa;
        TextView titulo;

        public LivroViewHolder(@NonNull View itemView) {
            super(itemView);
            capa = itemView.findViewById(R.id.capaLivro);
            titulo = itemView.findViewById(R.id.tituloLivro);
        }
    }
}