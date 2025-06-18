package com.qzz.demo2;

public class ProfileFragment extends BaseFragment {

    public static ProfileFragment newInstance() {
        return new ProfileFragment();
    }

    @Override
    protected void setupFragmentContent() {
        setFragmentTitle("我的");
        setContentText("个人中心\n\n用户信息管理\n系统设置\n帮助反馈\n\n管理您的个人信息和应用设置");
        setCustomButtonText("编辑资料");
    }

    @Override
    protected String getFragmentTag() {
        return "PROFILE";
    }

    @Override
    protected void onCustomActionClick() {
        if (callback != null) {
            callback.onFragmentDataChanged(getFragmentTag(), "profile_edit_requested");
        }
        setContentText("资料编辑功能\n\n这里可以修改：\n• 头像\n• 昵称\n• 个人简介\n• 联系方式");
    }
}
