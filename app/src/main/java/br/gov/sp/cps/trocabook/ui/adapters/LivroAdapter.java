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
import br.gov.sp.cps.trocabook.model.Livro;

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
                .placeholder(R.drawable.placeholder_capa)
                .into(holder.capa);

        holder.itemView.setOnClickListener(v -> listener.onItemClick(livro));
    }

    @Override
    public int getItemCount() {
        return listaLivros.size();
    }

    public void atualizarLista(List<Livro> novaLista) {
        androidx.recyclerview.widget.DiffUtil.DiffResult diffResult =
                androidx.recyclerview.widget.DiffUtil.calculateDiff(new DiffUtilCallback(this.listaLivros, novaLista));

        // Se você mudou a referência na BuscaActivity, é bom atualizar aqui também
        this.listaLivros.clear();
        this.listaLivros.addAll(novaLista);

        // Atualiza cirurgicamente apenas o que mudou, eliminando o aviso do Lint!
        diffResult.dispatchUpdatesTo(this);
    }

    // Adicione esta classe interna logo abaixo do método atualizarLista
    private static class DiffUtilCallback extends androidx.recyclerview.widget.DiffUtil.Callback {
        private final List<Livro> antigaLista;
        private final List<Livro> novaLista;

        public DiffUtilCallback(List<Livro> antigaLista, List<Livro> novaLista) {
            this.antigaLista = antigaLista;
            this.novaLista = novaLista;
        }

        @Override
        public int getOldListSize() {
            return antigaLista.size();
        }

        @Override
        public int getNewListSize() {
            return novaLista.size();
        }

        @Override
        public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
            // Se o seu modelo Livro tiver um ID único (ex: do Firestore), use ele aqui:
            // return antigaLista.get(oldItemPosition).getId().equals(novaLista.get(newItemPosition).getId());

            // Caso não tenha ID ainda, vamos comparar pelo título:
            return antigaLista.get(oldItemPosition).getTitulo().equals(novaLista.get(newItemPosition).getTitulo());
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            Livro antigo = antigaLista.get(oldItemPosition);
            Livro novo = novaLista.get(newItemPosition);

            // Compara se o conteúdo interno mudou (título ou imagem da capa)
            return antigo.getTitulo().equals(novo.getTitulo())
                    && antigo.getCapa().equals(novo.getCapa());
        }
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