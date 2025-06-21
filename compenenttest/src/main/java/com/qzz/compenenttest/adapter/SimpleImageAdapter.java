package com.qzz.compenenttest.adapter;

import android.content.Context;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.chad.library.adapter4.BaseQuickAdapter;
import com.chad.library.adapter4.viewholder.QuickViewHolder;
import com.qzz.compenenttest.R;
import com.qzz.compenenttest.model.ImageItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SimpleImageAdapter extends BaseQuickAdapter<ImageItem, QuickViewHolder> {
    private static final Random random = new Random();

    // 内部维护一个可变列表
    private final List<ImageItem> mutableData = new ArrayList<>();

    // 构造函数 - 只传入数据列表
    public SimpleImageAdapter(@Nullable List<ImageItem> data) {
        super(new ArrayList<>()); // 传入空的可变列表
        if (data != null) {
            setNewData(data);
        }
    }

    // 无参构造函数
    public SimpleImageAdapter() {
        super(new ArrayList<>()); // 传入空的可变列表
    }

    /**
     * 设置新数据（用于刷新）
     * @param newData 新的数据列表
     */
    public void setNewData(@Nullable List<ImageItem> newData) {
        mutableData.clear();
        if (newData != null) {
            mutableData.addAll(newData);
        }

        // 调用父类的submitList方法更新数据
        submitList(new ArrayList<>(mutableData));
    }

    /**
     * 添加数据（用于加载更多）
     * @param additionalData 要添加的数据
     */
    public void addData(@Nullable List<ImageItem> additionalData) {
        if (additionalData != null && !additionalData.isEmpty()) {
            int oldSize = mutableData.size();
            mutableData.addAll(additionalData);

            // 更新整个列表
            submitList(new ArrayList<>(mutableData));

            // 如果BaseQuickAdapter支持局部更新，可以使用：
            // notifyItemRangeInserted(oldSize, additionalData.size());
        }
    }

    /**
     * 获取当前数据的副本
     * @return 数据列表的副本
     */
    public List<ImageItem> getDataCopy() {
        return new ArrayList<>(mutableData);
    }

    /**
     * 获取数据项数量
     * @return 数据项数量
     */
    public int getDataSize() {
        return mutableData.size();
    }

    /**
     * 判断是否为空
     * @return 是否为空
     */
    public boolean isEmpty() {
        return mutableData.isEmpty();
    }

    /**
     * 清空数据
     */
    public void clearData() {
        mutableData.clear();
        submitList(new ArrayList<>());
    }

    // 实现抽象方法 - 注意这里有三个参数：Context, ViewGroup, int
    @NonNull
    @Override
    protected QuickViewHolder onCreateViewHolder(@NonNull Context context, @NonNull ViewGroup parent, int viewType) {
        return new QuickViewHolder(R.layout.item_simple_image, parent);
    }

    @Override
    protected void onBindViewHolder(@NonNull QuickViewHolder holder, int position, @Nullable ImageItem item) {
        if (item != null) {
            ImageView imageView = holder.getView(R.id.iv_image);

            // 动态设置高度实现瀑布流效果
            ViewGroup.LayoutParams layoutParams = imageView.getLayoutParams();
            if (item.getHeight() > 0 && item.getWidth() > 0) {
                // 根据图片尺寸计算高度（保持宽高比）
                int screenWidth = imageView.getContext().getResources().getDisplayMetrics().widthPixels;
                int itemWidth = (screenWidth - 48) / 2; // 减去margin和padding
                layoutParams.height = (int) ((float) item.getHeight() / item.getWidth() * itemWidth);

                // 限制最小和最大高度
                int minHeight = 300;
                int maxHeight = 800;
                if (layoutParams.height < minHeight) layoutParams.height = minHeight;
                if (layoutParams.height > maxHeight) layoutParams.height = maxHeight;
            } else {
                // 随机高度作为备选方案
                int[] heights = {400, 500, 600, 700, 800};
                layoutParams.height = heights[random.nextInt(heights.length)];
            }
            imageView.setLayoutParams(layoutParams);

            // 使用Glide加载图片
            RequestOptions options = new RequestOptions()
                    .placeholder(R.drawable.ic_placeholder)
                    .error(R.drawable.ic_error)
                    .transform(new RoundedCorners(16))
                    .centerCrop()
                    .timeout(10000);

            Glide.with(imageView.getContext())
                    .load(item.getImageUrl())
                    .apply(options)
                    .into(imageView);
        }
    }
}

