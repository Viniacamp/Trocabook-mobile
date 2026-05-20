package br.gov.sp.cps.trocabook.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.io.File;
import java.util.List;

import br.gov.sp.cps.trocabook.R;
import br.gov.sp.cps.trocabook.model.Usuario;

public class UsuarioHorizontalAdapter
        extends RecyclerView.Adapter<UsuarioHorizontalAdapter.UsuarioViewHolder> {

    private List<Usuario> lista;
    private OnUsuarioClick listener;

    public interface OnUsuarioClick {
        void onClick(Usuario usuario);
    }

    public UsuarioHorizontalAdapter(List<Usuario> lista,
                                    OnUsuarioClick listener) {
        this.lista = lista;
        this.listener = listener;
    }

    @NonNull
    @Override
    public UsuarioViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
                                                int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_usuario_home, parent, false);

        return new UsuarioViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UsuarioViewHolder holder, int position) {

        Usuario usuario = lista.get(position);

        String nomeFoto = usuario.getFoto_perfil();

        int resId = holder.itemView.getContext()
                .getResources()
                .getIdentifier(
                        nomeFoto,
                        "drawable",
                        holder.itemView.getContext().getPackageName()
                );

        if (resId == 0) {
            resId = R.drawable.placeholder_usuario;
        }

        Glide.with(holder.itemView.getContext())
                .load(resId)
                .placeholder(R.drawable.placeholder_usuario)
                .into(holder.foto);
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