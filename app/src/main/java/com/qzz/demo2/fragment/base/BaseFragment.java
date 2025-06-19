package com.qzz.demo2.fragment.base;

// BaseFragment.java
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.qzz.demo2.callback.FragmentCallback;
import com.qzz.demo2.R;

public abstract class BaseFragment extends Fragment {

    // 接口通信
    protected FragmentCallback callback;

    // UI组件
    protected TextView tvFragmentTitle, tvContent;
    protected Button btnSendMessage, btnCustomAction;
    protected FrameLayout flCustomContent;
    protected EditText etCustomInput;

    // 根视图
    protected View rootView;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        // 确保Activity实现了回调接口
        if (context instanceof FragmentCallback) {
            callback = (FragmentCallback) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement FragmentCallback");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // 使用现有的布局文件
        rootView = inflater.inflate(R.layout.fragment_base, container, false);
        initViews();
        setupListeners();
        setupFragmentContent();
        return rootView;
    }

    public void receiveMessageFromPrevious(String fromTag, String message) {
        // 处理从上一个Fragment传递过来的消息
        // 例如在HomeFragment中：
        if (tvContent != null) {
            tvContent.setText("从" + fromTag + "接收到消息: " + message);
        }
    }

    private void initViews() {
        tvFragmentTitle = rootView.findViewById(R.id.tv_fragment_title);
        tvContent = rootView.findViewById(R.id.tv_content);
        btnSendMessage = rootView.findViewById(R.id.btn_send_message);
        btnCustomAction = rootView.findViewById(R.id.btn_custom_action);
        flCustomContent = rootView.findViewById(R.id.fl_custom_content);
        etCustomInput = rootView.findViewById(R.id.editText);
    }

    private void setupListeners() {
        // 发送消息按钮监听
        btnSendMessage.setOnClickListener(v -> {
            if (callback != null) {
                String message = "来自 " + getFragmentTag() + " 的消息: " + getCustomMessage();
                callback.onFragmentMessage(getFragmentTag(), message);
            }
        });

        // 自定义操作按钮监听
        btnCustomAction.setOnClickListener(v -> {
            onCustomActionClick();
        });
    }

    // 抽象方法 - 子类必须实现
    protected abstract void setupFragmentContent();
    protected abstract String getFragmentTag();

    // 可选重写的方法
    protected String getCustomMessage() {
        return etCustomInput.getText().toString().trim();
    }

    protected void onCustomActionClick() {
        Toast.makeText(getContext(), getFragmentTag() + " 自定义操作被点击", Toast.LENGTH_SHORT).show();
    }

    // 工具方法 - 供子类使用
    protected void addCustomView(View customView) {
        if (flCustomContent != null) {
            flCustomContent.removeAllViews();
            flCustomContent.addView(customView);
        }
    }

    protected void setContentText(String text) {
        if (tvContent != null) {
            tvContent.setText(text);
        }
    }

    protected void setFragmentTitle(String title) {
        if (tvFragmentTitle != null) {
            tvFragmentTitle.setText(title);
        }
    }

    protected void setCustomButtonText(String text) {
        if (btnCustomAction != null) {
            btnCustomAction.setText(text);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        callback = null;
    }
}

