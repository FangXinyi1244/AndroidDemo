package com.qzz.demo2.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.qzz.demo2.R;
import com.qzz.demo2.model.dto.Game;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SearchResultsAdapter extends RecyclerView.Adapter<SearchResultsAdapter.GameViewHolder> {
    private List<Game> gameList;
    private OnItemActionListener listener;
    private Set<Integer> savedGamePositions = new HashSet<>();
    public interface OnItemActionListener {
        void onSaveGame(Game game, int position);
        void onGameClick(Game game);
    }
    public SearchResultsAdapter(List<Game> gameList, OnItemActionListener listener) {
        this.gameList = gameList;
        this.listener = listener;
    }
    @NonNull
    @Override
    public GameViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_search_result, parent, false);
        return new GameViewHolder(view);
    }
    @Override
    public void onBindViewHolder(@NonNull GameViewHolder holder, int position) {
        Game game = gameList.get(position);
        holder.bind(game, position);
    }
    @Override
    public int getItemCount() {
        return gameList.size();
    }
    public void markGameAsSaved(int position) {
        savedGamePositions.add(position);
        notifyItemChanged(position);
    }
    class GameViewHolder extends RecyclerView.ViewHolder {
        private ImageView gameImage;
        private TextView gameTitle;
        private TextView gameDescription;
        private TextView gameCategory;
        private TextView gameScore;
        private Button saveButton;
        public GameViewHolder(@NonNull View itemView) {
            super(itemView);
            gameImage = itemView.findViewById(R.id.game_image);
            gameTitle = itemView.findViewById(R.id.game_title);
            gameDescription = itemView.findViewById(R.id.game_description);
            gameCategory = itemView.findViewById(R.id.game_category);
            gameScore = itemView.findViewById(R.id.game_score);
            saveButton = itemView.findViewById(R.id.save_button);
        }
        public void bind(Game game, int position) {
            // 修正属性名称
            gameTitle.setText(game.getGameName());
            gameDescription.setText(game.getIntroduction());
            gameCategory.setText(game.getTags());

            // 显示评分
            if (game.getScore() != null) {
                gameScore.setText(String.format("%.1f分", game.getScore()));
                gameScore.setVisibility(View.VISIBLE);
            } else {
                gameScore.setVisibility(View.GONE);
            }
            // 加载游戏图片
            if (game.getIcon() != null && !game.getIcon().isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(game.getIcon())
                        .placeholder(R.drawable.bg_game_image)
                        .error(R.drawable.bg_game_image)
                        .into(gameImage);
            } else {
                gameImage.setImageResource(R.drawable.bg_game_image);
            }
            // 设置保存按钮状态
            boolean isSaved = savedGamePositions.contains(position);
            if (isSaved) {
                saveButton.setText("已保存");
                saveButton.setEnabled(false);
                saveButton.setBackgroundColor(ContextCompat.getColor(itemView.getContext(), R.color.saved_button_color));
            } else {
                saveButton.setText("保存");
                saveButton.setEnabled(true);
                saveButton.setBackgroundColor(ContextCompat.getColor(itemView.getContext(), R.color.primary_color));
            }
            // 点击事件
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onGameClick(game);
                }
            });
            saveButton.setOnClickListener(v -> {
                if (listener != null && !isSaved) {
                    listener.onSaveGame(game, position);
                }
            });
        }
    }
}

