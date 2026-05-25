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

    private List<MensagemDTO> mensagens;
    private String usuarioAtual;

    private static final int ENVIADA = 1;
    private static final int RECEBIDA = 2;

    public ChatAdapter(
            List<MensagemDTO> mensagens,
            String usuarioAtual
    ) {
        this.mensagens = mensagens;
        this.usuarioAtual = usuarioAtual;
    }

    @Override
    public int getItemViewType(int position) {

        MensagemDTO mensagem = mensagens.get(position);

        if (mensagem.getUidRemetente()
                .equals(usuarioAtual)) {

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

        View view = LayoutInflater.from(parent.getContext())
                .inflate(layout, parent, false);

        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ChatViewHolder holder,
            int position
    ) {

        MensagemDTO mensagem = mensagens.get(position);

        holder.textMensagem.setText(
                mensagem.getConteudo()
        );
    }

    @Override
    public int getItemCount() {
        return mensagens.size();
    }

    static class ChatViewHolder
            extends RecyclerView.ViewHolder {

        TextView textMensagem;

        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);

            textMensagem =
                    itemView.findViewById(R.id.textMensagem);
        }
    }
}