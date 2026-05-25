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
import br.gov.sp.cps.trocabook.model.Usuario;
import br.gov.sp.cps.trocabook.service.UsuarioService;

public class ResultadoBuscaAdapter
        extends RecyclerView.Adapter<ResultadoBuscaAdapter.ViewHolder> {

    private List<Anuncio> anuncios;

    private UsuarioService usuarioService;

    private OnResultadoClick listener;

    public interface OnResultadoClick {

        void onClick(Anuncio anuncio);
    }

    public ResultadoBuscaAdapter(
            List<Anuncio> anuncios,
            OnResultadoClick listener
    ) {

        this.anuncios = anuncios;

        this.listener = listener;

        this.usuarioService = new UsuarioService();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {

        View view = LayoutInflater
                .from(parent.getContext())
                .inflate(
                        R.layout.item_resultado_busca,
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

        Anuncio anuncio = anuncios.get(position);

        holder.textTitulo.setText(
                anuncio.getTitulo()
        );

        Glide.with(holder.itemView.getContext())
                .load(anuncio.getCapa())
                .placeholder(R.drawable.placeholder_capa)
                .into(holder.imgLivro);

        usuarioService.buscarUsuarioPorId(
                anuncio.getUserId(),
                new UsuarioService.ServiceCallback<Usuario>() {

                    @Override
                    public void onSuccess(Usuario usuario) {

                        Glide.with(holder.itemView.getContext())
                                .load(usuario.getFoto_perfil())
                                .placeholder(R.drawable.placeholder_usuario)
                                .circleCrop()
                                .into(holder.imgUsuario);
                    }

                    @Override
                    public void onError(String erro) {

                    }
                }
        );

        holder.itemView.setOnClickListener(v ->
                listener.onClick(anuncio)
        );
    }

    @Override
    public int getItemCount() {

        return anuncios.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        ImageView imgLivro;

        ImageView imgUsuario;

        TextView textTitulo;

        public ViewHolder(@NonNull View itemView) {

            super(itemView);

            imgLivro =
                    itemView.findViewById(R.id.imgLivro);

            imgUsuario =
                    itemView.findViewById(R.id.imgUsuario);

            textTitulo =
                    itemView.findViewById(R.id.textTituloLivro);
        }
    }
}