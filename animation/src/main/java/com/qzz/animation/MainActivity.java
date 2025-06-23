package com.qzz.animation;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.AnticipateOvershootInterpolator;
import android.view.animation.BounceInterpolator;
import android.view.animation.CycleInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.OvershootInterpolator;
import android.view.animation.RotateAnimation;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private final String TAG = "MainActivity";

    // 视图组件
    private ImageView targetImageView;
    private SeekBar scaleSeekBar, rotationSeekBar, alphaSeekBar;
    private TextView scaleValueText, rotationValueText, alphaValueText;

    // 动画参数
    private float currentScale = 1.5f;
    private float currentRotation = -720f; // 逆时针
    private float currentAlpha = 0.8f;

    // 动画状态
    private boolean isAnimating = false;
    private int repeatCount = 0;
    private final int MAX_REPEAT = 3;

    private View propertyTargetView;
    private Button resetPropertyAnimationBtn;
    private TextView animationCountText, currentAnimationTypeText;
    private TextView animationInfoText; // 新增：显示动画序列信息
    private TextView nextAnimationText; // 新增：显示下一个动画

    // 属性动画管理器
    private PropertyAnimationManager animationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        setupSeekBars();
        setupClickListeners();
        setupPropertyAnimation();
    }

    private void initViews() {
        targetImageView = findViewById(R.id.target_imageview);
        scaleSeekBar = findViewById(R.id.scale_seekbar);
        rotationSeekBar = findViewById(R.id.rotation_seekbar);
        alphaSeekBar = findViewById(R.id.alpha_seekbar);
        scaleValueText = findViewById(R.id.scale_value_text);
        rotationValueText = findViewById(R.id.rotation_value_text);
        alphaValueText = findViewById(R.id.alpha_value_text);

        propertyTargetView = findViewById(R.id.target_view);
        resetPropertyAnimationBtn = findViewById(R.id.reset_property_animation_btn);
        animationCountText = findViewById(R.id.animation_count_text);
        currentAnimationTypeText = findViewById(R.id.current_animation_type_text);

    }

    private void setupSeekBars() {
        // 缩放控制 (0.5x - 2.5x)
        scaleSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                currentScale = 0.5f + (progress / 100.0f) * 2.0f; // 修正计算公式：0.5 到 2.5
                scaleValueText.setText(String.format("%.1fx", currentScale));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // 旋转控制 (0° - 1080°)
        rotationSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                currentRotation = -progress * 10.8f; // 修正计算：SeekBar最大100，映射到1080度
                rotationValueText.setText(String.format("%.0f°", Math.abs(currentRotation)));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // 透明度控制 (0% - 100%)
        alphaSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                currentAlpha = progress / 100.0f;
                alphaValueText.setText(String.format("%d%%", progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // 设置SeekBar初始值
        scaleSeekBar.setProgress(50); // 对应1.5x缩放
        rotationSeekBar.setProgress(67); // 对应720度
        alphaSeekBar.setProgress(80);    // 对应80%透明度
    }

    private void setupClickListeners() {
        targetImageView.setOnClickListener(v -> startCustomAnimation());
    }

    private void startCustomAnimation() {
        if (isAnimating) {
            return; // 防止重复触发
        }

        // 重置状态
        repeatCount = 0;
        isAnimating = true;

        // 执行动画
        executeAnimation();
    }

    private void executeAnimation() {
        // 动态创建动画
        Animation customAnimation = createCustomAnimation();
        customAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                Log.d(TAG,"animation start - " + (repeatCount + 1));
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                Log.d(TAG,"animation end - " + (repeatCount + 1));
                repeatCount++;

                if (repeatCount < MAX_REPEAT) {
                    // 继续重复
                    executeAnimation();
                } else {
                    // 动画完成
                    animationComplete();
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                Log.d(TAG,"animation repeat");
            }
        });

        targetImageView.startAnimation(customAnimation);
    }

    private Animation createCustomAnimation() {
        AnimationSet animationSet = new AnimationSet(true);

        // 缩放动画
        ScaleAnimation scaleAnimation = new ScaleAnimation(
                1.0f, currentScale, 1.0f, currentScale,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f
        );
        scaleAnimation.setDuration(200);
        scaleAnimation.setFillAfter(false);

        // 旋转动画
        RotateAnimation rotateAnimation = new RotateAnimation(
                0, currentRotation,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f
        );
        rotateAnimation.setDuration(2000);
        rotateAnimation.setStartOffset(200);

        // 透明度动画
        AlphaAnimation alphaAnimation = new AlphaAnimation(1.0f, currentAlpha);
        alphaAnimation.setDuration(2000);
        alphaAnimation.setStartOffset(200);

        // 组合动画
        animationSet.addAnimation(scaleAnimation);
        animationSet.addAnimation(rotateAnimation);
        animationSet.addAnimation(alphaAnimation);

        return animationSet;
    }

    private void animationComplete() {
        isAnimating = false;
        Log.d(TAG, "所有动画执行完成");
    }

    private void setupPropertyAnimation() {
        // 初始化属性动画管理器
        animationManager = new PropertyAnimationManager(propertyTargetView, this::updatePropertyAnimationUI);

        setupPropertyClickListeners();
        updatePropertyAnimationUI();
    }

    private void setupPropertyClickListeners() {
        // 点击目标视图：执行当前动画并自动递增
        propertyTargetView.setOnClickListener(v -> {
            // 检查是否已达到最大动画数量
            if (animationManager.hasReachedMaxAnimations()) {
                Log.d(TAG, "已达到最大动画数量");
                // 可以显示提示信息
                return;
            }

            // 如果动画正在执行，不响应点击
            if (animationManager.isAnimating()) {
                Log.d(TAG, "动画正在执行中，请稍候...");
                return;
            }

            animationManager.executeCurrentAnimationAndIncrement();
            updatePropertyAnimationUI();
        });

        // 重置按钮：重置动画状态
        resetPropertyAnimationBtn.setOnClickListener(v -> {
            animationManager.reset();
            updatePropertyAnimationUI();
        });
    }

    private void updatePropertyAnimationUI() {
        // 更新动画数量显示
        int count = animationManager.getAnimationCount();
        animationCountText.setText(String.valueOf(count));

        // 更新当前动画序列描述
        String currentType = animationManager.getCurrentAnimationType();
        currentAnimationTypeText.setText("🎯 " + currentType);

        // 显示动画信息（如果有对应的TextView）
        if (animationInfoText != null) {
            animationInfoText.setText(animationManager.getAnimationInfo());
        }

        // 显示下一个动画类型（如果有对应的TextView）
        if (nextAnimationText != null && !animationManager.hasReachedMaxAnimations()) {
            nextAnimationText.setText("下一个: " + animationManager.getNextAnimationType());
            nextAnimationText.setVisibility(View.VISIBLE);
        } else if (nextAnimationText != null) {
            nextAnimationText.setVisibility(View.GONE);
        }

        // 控制重置按钮状态
        resetPropertyAnimationBtn.setEnabled(count > 0 && !animationManager.isAnimating());

        // 记录动画序列列表（用于调试）
        List<String> sequenceList = animationManager.getAnimationSequenceList();
        if (sequenceList.size() > 0) {
            Log.d(TAG, "当前动画序列:");
            for (String step : sequenceList) {
                Log.d(TAG, "  " + step);
            }
        }

        Log.d(TAG, "UI更新完成 - 动画计数: " + count + ", 当前类型: " + currentType);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 清理资源，重置所有动画状态
        if (animationManager != null) {
            animationManager.reset();
        }
    }
}
