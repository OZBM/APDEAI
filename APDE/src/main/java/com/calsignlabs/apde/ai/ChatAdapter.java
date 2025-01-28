package com.calsignlabs.apde.ai;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.calsignlabs.apde.R;
import com.calsignlabs.apde.ai.model.ChatMessage;
import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.MessageViewHolder> {
    private static final int VIEW_TYPE_USER = 1;
    private static final int VIEW_TYPE_AI = 2;

    private List<ChatMessage> messages;

    public ChatAdapter(List<ChatMessage> messages) {
        this.messages = messages;
    }

    @Override
    public int getItemViewType(int position) {
        ChatMessage message = messages.get(position);
        return message.getType() == ChatMessage.TYPE_USER ? VIEW_TYPE_USER : VIEW_TYPE_AI;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layout = viewType == VIEW_TYPE_USER ? 
                R.layout.item_message_user : R.layout.item_message_ai;
        View view = LayoutInflater.from(parent.getContext())
                .inflate(layout, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        ChatMessage message = messages.get(position);
        // Apply markdown rendering for AI messages
        if (message.getType() == ChatMessage.TYPE_AI) {
            holder.messageText.setText(
                MarkdownRenderer.render(holder.itemView.getContext(), message.getMessage())
            );
        } else {
            holder.messageText.setText(message.getMessage());
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    static class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView messageText;

        MessageViewHolder(View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.messageText);
        }
    }
}