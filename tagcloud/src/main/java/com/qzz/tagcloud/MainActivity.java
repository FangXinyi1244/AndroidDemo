package com.qzz.tagcloud;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.qzz.tagcloud.adapter.TagAdapter;
import com.qzz.tagcloud.widget.TagCloudView;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private TagCloudView mTagCloudView;
    private MyTagAdapter mAdapter;
    private List<String> mTags;
    private Random mRandom;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        initData();
        setupTagCloud();
        setupButtons();
    }

    private void initViews() {
        mTagCloudView = findViewById(R.id.tag_cloud_view);
        mRandom = new Random();
    }

    private void initData() {
        mTags = new ArrayList<>(Arrays.asList(
                "Android", "Java", "Kotlin", "Flutter", "iOS", "Swift",
                "React Native", "Vue.js", "Angular", "Node.js", "Python",
                "JavaScript", "TypeScript", "Go", "Rust", "Docker",
                "Kubernetes", "AWS", "Firebase", "MongoDB", "MySQL",
                "Redis", "Nginx", "Apache", "Linux", "Git", "GitHub",
                "Machine Learning", "AI", "Deep Learning", "TensorFlow",
                "PyTorch", "OpenCV", "Data Science", "Big Data", "Spark"
        ));
    }

    private void setupTagCloud() {
        mAdapter = new MyTagAdapter();
        mTagCloudView.setAdapter(mAdapter);

        // 设置点击监听
        mTagCloudView.setOnTagClickListener((view, position) -> {
            String tag = mTags.get(position);
            Toast.makeText(this, "点击了标签: " + tag, Toast.LENGTH_SHORT).show();

            // 添加选中效果
            TextView tagView = (TextView) view;
            if (tagView.getTag() != null && (Boolean) tagView.getTag()) {
                // 取消选中
                tagView.setBackgroundResource(R.drawable.tag_background);
                tagView.setTextColor(ContextCompat.getColor(this, R.color.tag_text_color));
                tagView.setTag(false);
            } else {
                // 选中
                tagView.setBackgroundResource(R.drawable.tag_background_selected);
                tagView.setTextColor(Color.WHITE);
                tagView.setTag(true);
            }
        });
    }

    private void setupButtons() {
        Button btnAddTag = findViewById(R.id.btn_add_tag);
        Button btnRemoveTag = findViewById(R.id.btn_remove_tag);
        Button btnShuffle = findViewById(R.id.btn_shuffle);
        Button btnClear = findViewById(R.id.btn_clear);

        btnAddTag.setOnClickListener(v -> addRandomTag());
        btnRemoveTag.setOnClickListener(v -> removeLastTag());
        btnShuffle.setOnClickListener(v -> shuffleTags());
        btnClear.setOnClickListener(v -> clearAllTags());
    }

    private void addRandomTag() {
        String[] newTags = {
                "新标签", "Cool", "Awesome", "Amazing", "Fantastic",
                "编程", "开发", "技术", "创新", "未来"
        };
        String randomTag = newTags[mRandom.nextInt(newTags.length)] + mRandom.nextInt(100);
        mTags.add(randomTag);
        mAdapter.notifyDataSetChanged();
    }

    private void removeLastTag() {
        if (!mTags.isEmpty()) {
            mTags.remove(mTags.size() - 1);
            mAdapter.notifyDataSetChanged();
        }
    }

    private void shuffleTags() {
        for (int i = mTags.size() - 1; i > 0; i--) {
            int j = mRandom.nextInt(i + 1);
            String temp = mTags.get(i);
            mTags.set(i, mTags.get(j));
            mTags.set(j, temp);
        }
        mAdapter.notifyDataSetChanged();
    }

    private void clearAllTags() {
        mTags.clear();
        mAdapter.notifyDataSetChanged();
    }

    // 自定义适配器实现
    private class MyTagAdapter extends TagAdapter {

        @Override
        public int getCount() {
            return mTags.size();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TextView tagView;

            if (convertView instanceof TextView) {
                tagView = (TextView) convertView;
            } else {
                tagView = (TextView) LayoutInflater.from(MainActivity.this)
                        .inflate(R.layout.item_tag, parent, false);
            }

            String tag = mTags.get(position);
            tagView.setText(tag);
            tagView.setTag(false); // 初始状态为未选中

            // 随机颜色效果（可选）
            if (mRandom.nextFloat() < 0.3f) {
                tagView.setBackgroundResource(R.drawable.tag_background_colored);
            } else {
                tagView.setBackgroundResource(R.drawable.tag_background);
            }

            return tagView;
        }
    }
}
