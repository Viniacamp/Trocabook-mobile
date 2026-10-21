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
import br.gov.sp.cps.trocabook.model.Anuncio;

public class AnuncioTrocaAdapter
        extends RecyclerView.Adapter<AnuncioTrocaAdapter.ViewHolder> {

    private final List<Anuncio> anuncios;
    private final OnAnuncioClickListener listener;

    public interface OnAnuncioClickListener {
        void onClick(Anuncio anuncio);
    }

    public AnuncioTrocaAdapter(
            List<Anuncio> anuncios,
            OnAnuncioClickListener listener
    ) {
        this.anuncios = anuncios;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {

        View view =
                LayoutInflater
                        .from(parent.getContext())
                        .inflate(
                                R.layout.item_livro_troca,
                                parent,
                                false
                        );

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ViewHolder holder,
            int position
    ) {

        Anuncio anuncio =
                anuncios.get(position);

        holder.txtTitulo.setText(
                anuncio.getTitulo()
        );

        holder.txtTipo.setText(
                obterTextoTipo(
                        anuncio.getTipoNegociacao()
                )
        );

        Glide.with(holder.itemView.getContext())
                .load(anuncio.getCapa())
                .placeholder(R.drawable.placeholder_capa)
                .into(holder.imgCapa);

        holder.itemView.setOnClickListener(v -> {

            if (listener != null) {
                listener.onClick(anuncio);
            }
        });
    }

    private String obterTextoTipo(
            Anuncio.TipoNegociacao tipo
    ) {

        if (tipo == null) {
            return "";
        }

        switch (tipo) {

            case TROCA:
                return "Troca";

            case VENDA:
                return "Venda";

            case AMBOS:
                return "Troca ou venda";

            default:
                return "";
        }
    }

    @Override
    public int getItemCount() {
        return anuncios.size();
    }

    static class ViewHolder
            extends RecyclerView.ViewHolder {

        ImageView imgCapa;
        TextView txtTitulo;
        TextView txtTipo;

        public ViewHolder(
                @NonNull View itemView
        ) {
            super(itemView);

            imgCapa =
                    itemView.findViewById(
                            R.id.imgCapaLivroTroca
                    );

            txtTitulo =
                    itemView.findViewById(
                            R.id.txtTituloLivroTroca
                    );

            txtTipo =
                    itemView.findViewById(
                            R.id.txtTipoLivroTroca
                    );
        }
    }
}