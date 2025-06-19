package com.qzz.demo2.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.qzz.demo2.R;
import com.qzz.demo2.model.vo.CalculatorKey;

import java.util.List;

public class CalculatorKeyboardAdapter extends RecyclerView.Adapter<CalculatorKeyboardAdapter.KeyViewHolder> {
    private List<CalculatorKey> keys;
    private OnKeyClickListener listener;

    public interface OnKeyClickListener {
        void onKeyClick(CalculatorKey key);
    }

    public CalculatorKeyboardAdapter(List<CalculatorKey> keys, OnKeyClickListener listener) {
        this.keys = keys;
        this.listener = listener;
    }

    @Override
    public KeyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_calculator_key, parent, false);
        return new KeyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(KeyViewHolder holder, int position) {
        CalculatorKey key = keys.get(position);
        holder.bind(key);
    }

    @Override
    public int getItemCount() {
        return keys.size();
    }

    class KeyViewHolder extends RecyclerView.ViewHolder {
        private TextView textView;

        public KeyViewHolder(View itemView) {
            super(itemView);
            textView = (TextView) itemView;
        }

        public void bind(final CalculatorKey key) {
            textView.setText(key.getText());

            switch (key.getType()) {
                case CalculatorKey.TYPE_NUMBER:
                    textView.setBackground(ContextCompat.getDrawable(itemView.getContext(),
                            R.drawable.btn_calculator));
                    break;
                case CalculatorKey.TYPE_OPERATOR:
                case CalculatorKey.TYPE_FUNCTION:
                    textView.setBackground(ContextCompat.getDrawable(itemView.getContext(),
                            R.drawable.btn_operator));
                    break;
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onKeyClick(key);
                }
            });
        }
    }
}

