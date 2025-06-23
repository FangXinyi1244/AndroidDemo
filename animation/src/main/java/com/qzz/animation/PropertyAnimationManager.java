package com.qzz.animation;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AnticipateOvershootInterpolator;
import android.view.animation.BounceInterpolator;
import android.view.animation.CycleInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.OvershootInterpolator;

import java.util.ArrayList;
import java.util.List;

public class PropertyAnimationManager {
    private View targetView;
    private int animationCounter;
    private AnimatorSet currentAnimatorSet;
    private Runnable uiUpdateCallback;
    private List<Animator> animatorSequence;  // 存储要顺序执行的动画

    // 动画类型枚举
    public enum AnimationType {
        ROTATION_X(0, "X轴旋转"),
        ROTATION_Y(1, "Y轴旋转"),
        ROTATION_Z(2, "Z轴旋转"),
        TRANSLATION_X(3, "X轴平移"),
        TRANSLATION_Y(4, "Y轴平移"),
        SCALE_X(5, "X轴缩放"),
        SCALE_Y(6, "Y轴缩放"),
        ALPHA(7, "透明度变化"),
        ELEVATION(8, "阴影高度");

        private final int value;
        private final String description;

        AnimationType(int value, String description) {
            this.value = value;
            this.description = description;
        }

        public int getValue() { return value; }
        public String getDescription() { return description; }
    }

    /**
     * 构造函数
     * @param targetView 目标动画视图
     * @param uiUpdateCallback UI更新回调
     */
    public PropertyAnimationManager(View targetView, Runnable uiUpdateCallback) {
        this.targetView = targetView;
        this.animationCounter = 0;
        this.uiUpdateCallback = uiUpdateCallback;
        this.animatorSequence = new ArrayList<>();
    }

    /**
     * 执行顺序累加的组合动画
     * 核心方法：每次点击都会按顺序执行从第1个到第N个的所有动画
     */
    public void executeCurrentAnimationAndIncrement() {
        // 停止当前正在运行的动画集
        if (currentAnimatorSet != null && currentAnimatorSet.isRunning()) {
            currentAnimatorSet.cancel();
        }

        // 递增计数器
        animationCounter++;

        // 创建动画序列
        animatorSequence.clear();

        // 根据计数器创建动画序列
        AnimationType[] types = AnimationType.values();
        int animationCount = Math.min(animationCounter, types.length);

        // 创建每个动画并添加到序列中
        for (int i = 0; i < animationCount; i++) {
            // 每个动画执行后都重置到初始状态，为下一个动画做准备
            AnimatorSet singleAnimationSet = createAnimationWithReset(types[i]);
            if (singleAnimationSet != null) {
                animatorSequence.add(singleAnimationSet);
            }
        }

        // 创建主动画集并设置顺序执行
        currentAnimatorSet = new AnimatorSet();
        currentAnimatorSet.playSequentially(animatorSequence);

        // 设置动画集监听器
        setupAnimatorSetListener();

        // 记录日志
        Log.d("PropertyAnimationManager",
                String.format("执行顺序动画序列: 包含 %d 个动画", animatorSequence.size()));

        // 启动动画集
        currentAnimatorSet.start();
    }

    /**
     * 创建包含动画和重置的动画集
     * 每个动画执行完后会重置相应的属性
     */
    private AnimatorSet createAnimationWithReset(AnimationType type) {
        ObjectAnimator mainAnimator = createAnimatorByType(type);
        if (mainAnimator == null) {
            return null;
        }

        AnimatorSet animatorSet = new AnimatorSet();

        // 创建重置动画
        ObjectAnimator resetAnimator = createResetAnimatorForType(type);

        if (resetAnimator != null) {
            // 先执行主动画，然后执行重置动画
            animatorSet.playSequentially(mainAnimator, resetAnimator);
        } else {
            // 如果没有重置动画，只执行主动画
            animatorSet.play(mainAnimator);
        }

        return animatorSet;
    }

    /**
     * 为特定动画类型创建重置动画
     * 快速重置到初始状态，为下一个动画做准备
     */
    private ObjectAnimator createResetAnimatorForType(AnimationType type) {
        ObjectAnimator resetAnimator = null;
        int resetDuration = 200; // 快速重置

        switch (type) {
            case ROTATION_X:
                resetAnimator = ObjectAnimator.ofFloat(targetView, "rotationX", 0f);
                break;
            case ROTATION_Y:
                resetAnimator = ObjectAnimator.ofFloat(targetView, "rotationY", 0f);
                break;
            case ROTATION_Z:
                resetAnimator = ObjectAnimator.ofFloat(targetView, "rotation", 0f);
                break;
            case TRANSLATION_X:
                resetAnimator = ObjectAnimator.ofFloat(targetView, "translationX", 0f);
                break;
            case TRANSLATION_Y:
                resetAnimator = ObjectAnimator.ofFloat(targetView, "translationY", 0f);
                break;
            case SCALE_X:
                resetAnimator = ObjectAnimator.ofFloat(targetView, "scaleX", 1f);
                break;
            case SCALE_Y:
                resetAnimator = ObjectAnimator.ofFloat(targetView, "scaleY", 1f);
                break;
            case ALPHA:
                resetAnimator = ObjectAnimator.ofFloat(targetView, "alpha", 1f);
                break;
            case ELEVATION:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    resetAnimator = ObjectAnimator.ofFloat(targetView, "elevation", 0f);
                }
                break;
        }

        if (resetAnimator != null) {
            resetAnimator.setDuration(resetDuration);
            resetAnimator.setInterpolator(new LinearInterpolator());
        }

        return resetAnimator;
    }

    /**
     * 获取当前动画序列的描述
     */
    public String getCurrentAnimationType() {
        if (animationCounter == 0) {
            return "尚未开始";
        }

        AnimationType[] types = AnimationType.values();
        int activeCount = Math.min(animationCounter, types.length);

        StringBuilder sb = new StringBuilder();
        sb.append("顺序动画: ");
        for (int i = 0; i < activeCount; i++) {
            if (i > 0) sb.append(" → ");
            sb.append(types[i].getDescription());
        }

        return sb.toString();
    }

    /**
     * 获取当前动画序列列表
     */
    public List<String> getAnimationSequenceList() {
        List<String> sequence = new ArrayList<>();
        AnimationType[] types = AnimationType.values();
        int activeCount = Math.min(animationCounter, types.length);

        for (int i = 0; i < activeCount; i++) {
            sequence.add(String.format("第%d步: %s", i + 1, types[i].getDescription()));
        }

        return sequence;
    }

    /**
     * 根据动画类型创建对应的ObjectAnimator
     */
    private ObjectAnimator createAnimatorByType(AnimationType type) {
        switch (type) {
            case ROTATION_X:
                return createRotationXAnimator();
            case ROTATION_Y:
                return createRotationYAnimator();
            case ROTATION_Z:
                return createRotationZAnimator();
            case TRANSLATION_X:
                return createTranslationXAnimator();
            case TRANSLATION_Y:
                return createTranslationYAnimator();
            case SCALE_X:
                return createScaleXAnimator();
            case SCALE_Y:
                return createScaleYAnimator();
            case ALPHA:
                return createAlphaAnimator();
            case ELEVATION:
                return createElevationAnimator();
            default:
                Log.w("PropertyAnimationManager", "未知的动画类型: " + type);
                return null;
        }
    }

    // ========== 具体动画创建方法 ==========
    // 每个动画有不同的持续时间和效果

    private ObjectAnimator createRotationXAnimator() {
        ObjectAnimator animator = ObjectAnimator.ofFloat(targetView, "rotationX", 0f, 360f);
        animator.setDuration(1000);
        animator.setInterpolator(new LinearInterpolator());
        return animator;
    }

    private ObjectAnimator createRotationYAnimator() {
        ObjectAnimator animator = ObjectAnimator.ofFloat(targetView, "rotationY", 0f, 360f);
        animator.setDuration(1200);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        return animator;
    }

    private ObjectAnimator createRotationZAnimator() {
        ObjectAnimator animator = ObjectAnimator.ofFloat(targetView, "rotation", 0f, 360f);
        animator.setDuration(800);
        animator.setInterpolator(new OvershootInterpolator());
        return animator;
    }

    private ObjectAnimator createTranslationXAnimator() {
        float[] values = {0f, 200f, -100f, 0f};
        ObjectAnimator animator = ObjectAnimator.ofFloat(targetView, "translationX", values);
        animator.setDuration(1500);
        animator.setInterpolator(new BounceInterpolator());
        return animator;
    }

    private ObjectAnimator createTranslationYAnimator() {
        ObjectAnimator animator = ObjectAnimator.ofFloat(targetView, "translationY", 0f, -150f, 0f);
        animator.setDuration(1000);
        animator.setInterpolator(new CycleInterpolator(2));
        return animator;
    }

    private ObjectAnimator createScaleXAnimator() {
        ObjectAnimator animator = ObjectAnimator.ofFloat(targetView, "scaleX", 1f, 1.5f, 0.8f, 1f);
        animator.setDuration(1200);
        animator.setInterpolator(new AnticipateOvershootInterpolator());
        return animator;
    }

    private ObjectAnimator createScaleYAnimator() {
        ObjectAnimator animator = ObjectAnimator.ofFloat(targetView, "scaleY", 1f, 0.5f, 1.2f, 1f);
        animator.setDuration(1000);
        animator.setInterpolator(new DecelerateInterpolator());
        return animator;
    }

    private ObjectAnimator createAlphaAnimator() {
        ObjectAnimator animator = ObjectAnimator.ofFloat(targetView, "alpha", 1f, 0.3f, 1f);
        animator.setDuration(800);
        animator.setInterpolator(new AccelerateInterpolator());
        return animator;
    }

    private ObjectAnimator createElevationAnimator() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ObjectAnimator animator = ObjectAnimator.ofFloat(targetView, "elevation", 0f, 20f, 0f);
            animator.setDuration(1000);
            animator.setInterpolator(new LinearInterpolator());
            return animator;
        }
        return null;
    }

    /**
     * 设置动画集监听器
     */
    private void setupAnimatorSetListener() {
        currentAnimatorSet.addListener(new AnimatorListenerAdapter() {
            private int currentStep = 0;

            @Override
            public void onAnimationStart(Animator animation) {
                currentStep = 0;
                Log.d("PropertyAnimationManager", "开始执行动画序列");
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                Log.d("PropertyAnimationManager", "动画序列执行完成");

                // 最终重置所有属性
                resetAllProperties();

                // 通知UI更新
                if (uiUpdateCallback != null) {
                    uiUpdateCallback.run();
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                Log.d("PropertyAnimationManager", "动画序列被取消");
                resetAllProperties();
            }
        });

        // 为每个子动画添加监听器以跟踪进度
        for (int i = 0; i < animatorSequence.size(); i++) {
            final int stepIndex = i;
            Animator animator = animatorSequence.get(i);
            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    AnimationType[] types = AnimationType.values();
                    if (stepIndex < types.length) {
                        Log.d("PropertyAnimationManager",
                                String.format("执行第 %d 步: %s", stepIndex + 1, types[stepIndex].getDescription()));
                    }
                }
            });
        }
    }

    /**
     * 重置所有视图属性
     */
    private void resetAllProperties() {
        targetView.setRotation(0f);
        targetView.setRotationX(0f);
        targetView.setRotationY(0f);
        targetView.setTranslationX(0f);
        targetView.setTranslationY(0f);
        targetView.setScaleX(1f);
        targetView.setScaleY(1f);
        targetView.setAlpha(1f);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            targetView.setElevation(0f);
        }
    }

    /**
     * 获取动画序列的总时长（毫秒）
     */
    public long getTotalDuration() {
        long totalDuration = 0;
        AnimationType[] types = AnimationType.values();
        int count = Math.min(animationCounter, types.length);

        for (int i = 0; i < count; i++) {
            // 添加动画时长和重置时长
            switch (types[i]) {
                case ROTATION_X: totalDuration += 1000 + 200; break;
                case ROTATION_Y: totalDuration += 1200 + 200; break;
                case ROTATION_Z: totalDuration += 800 + 200; break;
                case TRANSLATION_X: totalDuration += 1500 + 200; break;
                case TRANSLATION_Y: totalDuration += 1000 + 200; break;
                case SCALE_X: totalDuration += 1200 + 200; break;
                case SCALE_Y: totalDuration += 1000 + 200; break;
                case ALPHA: totalDuration += 800 + 200; break;
                case ELEVATION: totalDuration += 1000 + 200; break;
            }
        }

        return totalDuration;
    }

    // ========== 其他公共方法 ==========

    public int getAnimationCount() {
        return animationCounter;
    }

    public void reset() {
        if (currentAnimatorSet != null && currentAnimatorSet.isRunning()) {
            currentAnimatorSet.cancel();
        }

        animatorSequence.clear();
        animationCounter = 0;
        resetAllProperties();

        Log.d("PropertyAnimationManager", "动画状态已重置");

        if (uiUpdateCallback != null) {
            uiUpdateCallback.run();
        }
    }

    public boolean isAnimating() {
        return currentAnimatorSet != null && currentAnimatorSet.isRunning();
    }

    public String getNextAnimationType() {
        AnimationType[] types = AnimationType.values();
        if (animationCounter >= types.length) {
            return "已达到最大动画数量";
        }
        return types[animationCounter].getDescription();
    }

    public boolean hasReachedMaxAnimations() {
        return animationCounter >= AnimationType.values().length;
    }

    public String getAnimationInfo() {
        if (animationCounter == 0) {
            return "点击开始添加动画";
        }

        long totalDuration = getTotalDuration();
        return String.format("动画序列: %d 步 | 总时长: %.1f 秒",
                Math.min(animationCounter, AnimationType.values().length),
                totalDuration / 1000.0f);
    }
}
