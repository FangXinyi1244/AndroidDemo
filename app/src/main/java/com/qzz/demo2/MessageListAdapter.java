package com.qzz.demo2;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.qzz.demo2.item.MessageItem;

import java.util.List;

public class MessageListAdapter extends RecyclerView.Adapter<MessageListAdapter.MessageViewHolder> {
    private List<MessageItem> messageList;
    private OnMessageClickListener clickListener;
    private OnMessageDeleteListener deleteListener;
    private boolean isDarkTheme = false;

    public interface OnMessageClickListener {
        void onMessageClick(MessageItem message);
    }

    public interface OnMessageDeleteListener {
        void onMessageDelete(int position);
    }

    public MessageListAdapter(List<MessageItem> messageList,
                              OnMessageClickListener clickListener,
                              OnMessageDeleteListener deleteListener) {
        this.messageList = messageList;
        this.clickListener = clickListener;
        this.deleteListener = deleteListener;
    }

    @Override
    public MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_message, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MessageViewHolder holder, int position) {
        MessageItem message = messageList.get(position);
        holder.bind(message, isDarkTheme);

        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onMessageClick(message);
            }
        });

        holder.deleteButton.setOnClickListener(v -> {
            if (deleteListener != null) {
                deleteListener.onMessageDelete(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    public void removeItem(int position) {
        if (position >= 0 && position < messageList.size()) {
            messageList.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, messageList.size());
        }
    }

    public void updateTheme(boolean isDarkTheme) {
        this.isDarkTheme = isDarkTheme;
        notifyDataSetChanged();
    }

    static class MessageViewHolder extends RecyclerView.ViewHolder {
        private TextView contentText;
        private Button deleteButton;
        private RelativeLayout rootLayout;

        public MessageViewHolder(View itemView) {
            super(itemView);
            contentText = itemView.findViewById(R.id.message_content);
            deleteButton = itemView.findViewById(R.id.delete_button);
            rootLayout = itemView.findViewById(R.id.message_root);
        }

        public void bind(MessageItem message, boolean isDarkTheme) {
            contentText.setText(message.getContent());

            int backgroundColor = isDarkTheme ? Color.parseColor("#404040") : Color.parseColor("#F5F5F5");
            int textColor = isDarkTheme ? Color.WHITE : Color.BLACK;

            rootLayout.setBackgroundColor(backgroundColor);
            contentText.setTextColor(textColor);
        }
    }
}
