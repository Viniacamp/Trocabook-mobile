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
import br.gov.sp.cps.trocabook.util.ImageUtils;
import br.gov.sp.cps.trocabook.util.SvgLoader;

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
        String ownerUid = anuncio.getUidUsuario();
        holder.imgUsuario.setTag(R.id.profile_photo_owner_uid, ownerUid);

        SvgLoader.carregar(
                holder.itemView.getContext(),
                null,
                holder.imgUsuario,
                R.drawable.placeholder_usuario,
                true
        );
        holder.textTitulo.setText(
                anuncio.getTitulo()
        );

        String urlCapa = ImageUtils.obterUrlImagem(anuncio.getCapa());
        Glide.with(holder.itemView.getContext())
                .load(urlCapa)
                .placeholder(R.drawable.placeholder_capa)
                .into(holder.imgLivro);

        usuarioService.buscarUsuarioPorId(
                anuncio.getUidUsuario(),
                new UsuarioService.ServiceCallback<Usuario>() {

                    @Override
                    public void onSucesso(Usuario usuario) {
                        if (!java.util.Objects.equals(
                                ownerUid,
                                holder.imgUsuario.getTag(R.id.profile_photo_owner_uid)
                        )) {
                            return;
                        }
                        SvgLoader.carregar(
                                holder.itemView.getContext(),
                                usuario == null ? null
                                        : ImageUtils.obterUrlImagem(usuario.getFotoPerfil()),
                                holder.imgUsuario,
                                R.drawable.placeholder_usuario,
                                true
                        );
                    }

                    @Override
                    public void onErro(String erro) {
                        if (!java.util.Objects.equals(
                                ownerUid,
                                holder.imgUsuario.getTag(R.id.profile_photo_owner_uid)
                        )) {
                            return;
                        }
                        SvgLoader.carregar(
                                holder.itemView.getContext(),
                                null,
                                holder.imgUsuario,
                                R.drawable.placeholder_usuario,
                                true
                        );
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