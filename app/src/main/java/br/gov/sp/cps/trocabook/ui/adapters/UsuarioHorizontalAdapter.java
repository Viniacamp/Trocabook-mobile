package br.gov.sp.cps.trocabook.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import br.gov.sp.cps.trocabook.R;
import br.gov.sp.cps.trocabook.model.Usuario;
import br.gov.sp.cps.trocabook.util.ImageUtils;
import br.gov.sp.cps.trocabook.util.SvgLoader;

public class UsuarioHorizontalAdapter
        extends RecyclerView.Adapter<UsuarioHorizontalAdapter.UsuarioViewHolder> {

    private List<Usuario> lista;
    private OnUsuarioClick listener;

    public interface OnUsuarioClick {
        void onClick(Usuario usuario);
    }

    public UsuarioHorizontalAdapter(
            List<Usuario> lista,
            OnUsuarioClick listener
    ) {

        this.lista = lista;
        this.listener = listener;
    }

    @NonNull
    @Override
    public UsuarioViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_usuario_home, parent, false);

        return new UsuarioViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull UsuarioViewHolder holder,
            int position
    ) {

        Usuario usuario = lista.get(position);

        String caminhoImagem = ImageUtils.obterUrlImagem(usuario.getFotoPerfil());

        SvgLoader.carregar(
                holder.itemView.getContext(),
                caminhoImagem,
                holder.foto,
                R.drawable.placeholder_usuario
        );

        holder.itemView.setOnClickListener(v -> {

            if (listener != null) {
                listener.onClick(usuario);
            }
        });
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }

    static class UsuarioViewHolder extends RecyclerView.ViewHolder {

        ImageView foto;

        public UsuarioViewHolder(@NonNull View itemView) {
            super(itemView);

            foto = itemView.findViewById(R.id.imagePerfil);
        }
    }
}