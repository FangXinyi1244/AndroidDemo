package com.qzz.demo2.fragment;

import com.qzz.demo2.fragment.base.BaseFragment;

public class HomeFragment extends BaseFragment {

    public static HomeFragment newInstance() {
        return new HomeFragment();
    }

    @Override
    protected void setupFragmentContent() {
        setFragmentTitle("首页");
        setContentText("欢迎来到首页！\n\n这里展示最新的内容和推荐信息。\n\n您可以浏览最热门的文章和活动。");
        setCustomButtonText("刷新内容");
    }

    @Override
    protected String getFragmentTag() {
        return "HOME";
    }

//    @Override
//    protected String getCustomMessage() {
//        return "首页数据已加载完成，当前有 " + (int)(Math.random() * 100) + " 条新内容";
//    }

    @Override
    protected void onCustomActionClick() {
        setContentText("内容已刷新！\n\n最新数据已加载\n时间: " + java.text.DateFormat.getTimeInstance().format(new java.util.Date()));

        if (callback != null) {
            callback.onFragmentDataChanged(getFragmentTag(), "refresh_completed");
        }
    }
}
