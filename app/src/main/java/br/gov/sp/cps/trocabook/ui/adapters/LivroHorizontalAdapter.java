package br.gov.sp.cps.trocabook.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

import br.gov.sp.cps.trocabook.R;
import br.gov.sp.cps.trocabook.model.utils.ItemComCapa;

public class LivroHorizontalAdapter<T extends ItemComCapa>
        extends RecyclerView.Adapter<LivroHorizontalAdapter.LivroViewHolder> {

    private List<T> listaLivros;
    private OnItemClick<T> listener;

    public interface OnItemClick<T> {
        void onClick(T item);
    }

    public LivroHorizontalAdapter(
            List<T> listaLivros,
            OnItemClick<T> listener
    ) {
        this.listaLivros = listaLivros;
        this.listener = listener;
    }

    @NonNull
    @Override
    public LivroViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.livro_card, parent, false);

        return new LivroViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull LivroViewHolder holder,
            int position
    ) {

        T livro = listaLivros.get(position);

        Glide.with(holder.itemView.getContext())
                .load(livro.getCapa())
                .placeholder(R.drawable.placeholder_capa)
                .into(holder.capa);

        holder.itemView.setOnClickListener(v -> {

            if (listener != null) {
                listener.onClick(livro);
            }
        });
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