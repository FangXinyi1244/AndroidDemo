package com.qzz.demo2.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.qzz.demo2.R;
import com.qzz.demo2.model.dto.Game;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class LocalStorageAdapter extends RecyclerView.Adapter<LocalStorageAdapter.LocalGameViewHolder> {
    private List<Game> gameList;
    private OnItemActionListener listener;

    public interface OnItemActionListener {
        void onDeleteGame(Game game, int position);
        void onGameClick(Game game);
    }

    public LocalStorageAdapter(List<Game> gameList, OnItemActionListener listener) {
        this.gameList = gameList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public LocalGameViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_local_storage, parent, false);
        return new LocalGameViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LocalGameViewHolder holder, int position) {
        Game game = gameList.get(position);
        holder.bind(game, position);
    }

    @Override
    public int getItemCount() {
        return gameList.size();
    }

    class LocalGameViewHolder extends RecyclerView.ViewHolder {
        private ImageView gameImage;
        private TextView gameTitle;
        private TextView gameDescription;
        private TextView gameCategory;
        private TextView saveTime;
        private ImageButton deleteButton;

        public LocalGameViewHolder(@NonNull View itemView) {
            super(itemView);
            gameImage = itemView.findViewById(R.id.game_image);
            gameTitle = itemView.findViewById(R.id.game_title);
            gameDescription = itemView.findViewById(R.id.game_description);
            gameCategory = itemView.findViewById(R.id.game_category);
            saveTime = itemView.findViewById(R.id.save_time);
            deleteButton = itemView.findViewById(R.id.delete_button);
        }

        public void bind(Game game, int position) {
            // 修正属性名称调用
            gameTitle.setText(game.getGameName());
            gameDescription.setText(game.getIntroduction());
            gameCategory.setText(game.getTags());

            // 显示保存时间 - createTime是String类型，需要解析
            if (game.getCreateTime() != null && !game.getCreateTime().isEmpty()) {
                String timeStr = formatCreateTime(game.getCreateTime());
                saveTime.setText("保存于: " + timeStr);
                saveTime.setVisibility(View.VISIBLE);
            } else {
                saveTime.setText("保存时间未知");
                saveTime.setVisibility(View.VISIBLE);
            }

            // 设置游戏图片
            if (game.getIcon() != null && !game.getIcon().isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(game.getIcon())
                        .placeholder(R.drawable.bg_game_image)
                        .error(R.drawable.bg_game_image)
                        .into(gameImage);
            } else {
                gameImage.setImageResource(R.drawable.bg_game_image);
            }

            // 设置其他游戏信息显示
            setupGameInfo(game);

            // 点击事件
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onGameClick(game);
                }
            });

            deleteButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeleteGame(game, position);
                }
            });
        }

        /**
         * 格式化创建时间字符串
         * @param createTimeStr 创建时间字符串
         * @return 格式化后的时间字符串
         */
        private String formatCreateTime(String createTimeStr) {
            try {
                // 假设createTime格式为 "yyyy-MM-dd HH:mm:ss" 或类似格式
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                SimpleDateFormat outputFormat = new SimpleDateFormat("MM-dd HH:mm", Locale.getDefault());

                Date date = inputFormat.parse(createTimeStr);
                if (date != null) {
                    return outputFormat.format(date);
                }
            } catch (ParseException e) {
                // 解析失败，尝试其他可能的格式
                try {
                    SimpleDateFormat alternativeFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
                    SimpleDateFormat outputFormat = new SimpleDateFormat("MM-dd HH:mm", Locale.getDefault());

                    Date date = alternativeFormat.parse(createTimeStr);
                    if (date != null) {
                        return outputFormat.format(date);
                    }
                } catch (ParseException ex) {
                    // 如果都解析失败，返回原始字符串的简化版本
                    if (createTimeStr.length() > 10) {
                        return createTimeStr.substring(5, Math.min(createTimeStr.length(), 16));
                    }
                }
            }
            return createTimeStr; // 返回原始字符串
        }

        /**
         * 设置其他游戏信息的辅助方法
         */
        private void setupGameInfo(Game game) {
            // 设置描述文本，如果太长则截断
            String description = game.getIntroduction();
            if (description != null && description.length() > 100) {
                description = description.substring(0, 100) + "...";
            }
            gameDescription.setText(description != null ? description : "暂无描述");

            // 设置标签，如果为空显示默认文本
            String tags = game.getTags();
            gameCategory.setText(tags != null && !tags.isEmpty() ? tags : "未分类");
        }
    }
}
