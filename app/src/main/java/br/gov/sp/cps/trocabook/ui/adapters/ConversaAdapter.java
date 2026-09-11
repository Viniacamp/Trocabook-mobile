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
import br.gov.sp.cps.trocabook.model.Usuario;
import br.gov.sp.cps.trocabook.model.dto.ConversaDTO;
import br.gov.sp.cps.trocabook.model.dto.MensagemDTO;

public class ConversaAdapter
        extends RecyclerView.Adapter<ConversaAdapter.ConversaViewHolder> {

    private List<ConversaDTO> conversas;

    private String uidUsuarioLogado;

    private OnConversaClick listener;

    public interface OnConversaClick {

        void onClick(ConversaDTO conversa);
    }

    public ConversaAdapter(
            List<ConversaDTO> conversas,
            String uidUsuarioLogado,
            OnConversaClick listener
    ) {

        this.conversas = conversas;

        this.uidUsuarioLogado = uidUsuarioLogado;

        this.listener = listener;
    }

    @NonNull
    @Override
    public ConversaViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(
                        R.layout.item_conversa,
                        parent,
                        false
                );

        return new ConversaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ConversaViewHolder holder,
            int position
    ) {

        ConversaDTO conversa = conversas.get(position);

        Usuario usuario = conversa.getUsuario();

        MensagemDTO mensagem =
                conversa.getUltimaMensagem();

        holder.textNome.setText(
                usuario.getEmailPrincipal()
        );

        String ultimaMensagem;

        if (mensagem.getUidRemetente()
                .equals(uidUsuarioLogado)) {

            ultimaMensagem =
                    "Você: " + mensagem.getConteudo();

        } else {

            ultimaMensagem =
                    usuario.getEmailPrincipal()
                            + ": "
                            + mensagem.getConteudo();
        }

        holder.textUltimaMensagem
                .setText(ultimaMensagem);

        Glide.with(holder.itemView.getContext())
                .load(usuario.getFotoPerfil())
                .placeholder(
                        R.drawable.placeholder_usuario
                )
                .into(holder.imgUsuario);

        holder.itemView.setOnClickListener(v -> {

            if (listener != null) {

                listener.onClick(conversa);
            }
        });
    }

    @Override
    public int getItemCount() {

        return conversas.size();
    }

    static class ConversaViewHolder
            extends RecyclerView.ViewHolder {

        ImageView imgUsuario;

        TextView textNome;

        TextView textUltimaMensagem;

        public ConversaViewHolder(
                @NonNull View itemView
        ) {

            super(itemView);

            imgUsuario =
                    itemView.findViewById(R.id.imgUsuario);

            textNome =
                    itemView.findViewById(R.id.textNome);

            textUltimaMensagem =
                    itemView.findViewById(
                            R.id.textUltimaMensagem
                    );
        }
    }
}