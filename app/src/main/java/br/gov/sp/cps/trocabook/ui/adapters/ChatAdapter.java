package br.gov.sp.cps.trocabook.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import br.gov.sp.cps.trocabook.R;
import br.gov.sp.cps.trocabook.model.dto.MensagemDTO;

public class ChatAdapter
        extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {

    private final List<MensagemDTO> mensagens;

    private final String usuarioAtual;

    private final OnMensagemLongClickListener longClickListener;

    private static final int ENVIADA = 1;

    private static final int RECEBIDA = 2;

    public interface OnMensagemLongClickListener {

        void onLongClick(MensagemDTO mensagem);
    }

    public ChatAdapter(
            List<MensagemDTO> mensagens,
            String usuarioAtual,
            OnMensagemLongClickListener longClickListener
    ) {

        this.mensagens = mensagens;

        this.usuarioAtual = usuarioAtual;

        this.longClickListener = longClickListener;
    }

    @Override
    public int getItemViewType(int position) {

        MensagemDTO mensagem = mensagens.get(position);

        if (usuarioAtual != null
                && usuarioAtual.equals(
                mensagem.getUidRemetente()
        )) {

            return ENVIADA;
        }

        return RECEBIDA;
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {

        int layout;

        if (viewType == ENVIADA) {

            layout = R.layout.item_mensagem_enviada;

        } else {

            layout = R.layout.item_mensagem_recebida;
        }

        View view =
                LayoutInflater
                        .from(parent.getContext())
                        .inflate(
                                layout,
                                parent,
                                false
                        );

        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ChatViewHolder holder,
            int position
    ) {

        MensagemDTO mensagem =
                mensagens.get(position);

        holder.textMensagem.setText(
                mensagem.getConteudo()
        );

        boolean mensagemPropria =
                usuarioAtual != null
                        && usuarioAtual.equals(
                        mensagem.getUidRemetente()
                );

        if (mensagemPropria) {

            holder.itemView.setOnLongClickListener(v -> {

                if (longClickListener != null) {

                    longClickListener.onLongClick(
                            mensagem
                    );
                }

                return true;
            });

        } else {

            holder.itemView.setOnLongClickListener(null);
        }
    }

    @Override
    public int getItemCount() {

        return mensagens.size();
    }

    static class ChatViewHolder
            extends RecyclerView.ViewHolder {

        TextView textMensagem;

        public ChatViewHolder(
                @NonNull View itemView
        ) {

            super(itemView);

            textMensagem =
                    itemView.findViewById(
                            R.id.textMensagem
                    );
        }
    }
}