package com.qzz.demo2.fragment;

import android.widget.TextView;

import com.qzz.demo2.fragment.base.BaseFragment;

public class CategoryFragment extends BaseFragment {

    public static CategoryFragment newInstance() {
        return new CategoryFragment();
    }

    @Override
    protected void setupFragmentContent() {
        setFragmentTitle("分类");
        setContentText("分类页面\n\n• 热门分类\n• 推荐分类\n• 最新分类\n\n点击下方按钮查看更多分类");
        setCustomButtonText("查看全部");
    }

    @Override
    protected String getFragmentTag() {
        return "CATEGORY";
    }

    @Override
    protected void onCustomActionClick() {
        // 可以在这里添加自定义视图
        TextView customView = new TextView(getContext());
        customView.setText("分类列表：\n\n1. 科技\n2. 娱乐\n3. 体育\n4. 新闻\n5. 生活");
        customView.setPadding(16, 16, 16, 16);
        customView.setBackgroundColor(0xFFF5F5F5);
        customView.setTextSize(14);

        addCustomView(customView);
    }
}
