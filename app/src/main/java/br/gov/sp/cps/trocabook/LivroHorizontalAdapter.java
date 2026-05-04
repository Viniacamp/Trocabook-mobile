package br.gov.sp.cps.trocabook;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class LivroHorizontalAdapter extends RecyclerView.Adapter<LivroHorizontalAdapter.LivroViewHolder> {

    private List<Livro> listaLivros;

    public LivroHorizontalAdapter(List<Livro> listaLivros) {
        this.listaLivros = listaLivros;
    }

    @NonNull
    @Override
    public LivroViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.livro_card, parent, false); // 👈 layout novo
        return new LivroViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LivroViewHolder holder, int position) {
        Livro livro = listaLivros.get(position);

        Glide.with(holder.itemView.getContext())
                .load(livro.getCapa())
                .placeholder(R.drawable.ic_launcher_foreground)
                .into(holder.capa);
    }

    @Override
    public int getItemCount() {
        return listaLivros.size();
    }

    static class LivroViewHolder extends RecyclerView.ViewHolder {

        ImageView capa;


        public LivroViewHolder(@NonNull View itemView) {
            super(itemView);
            capa = itemView.findViewById(R.id.capaLivro);
        }
    }
}