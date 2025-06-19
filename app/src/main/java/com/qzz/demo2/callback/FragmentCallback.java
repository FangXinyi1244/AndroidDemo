package com.qzz.demo2.callback;

// Fragment与Activity通信的接口
public interface FragmentCallback {
    void onFragmentMessage(String tag, String message);
    void onFragmentDataChanged(String tag, Object data);
}


