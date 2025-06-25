package com.qzz.anktest;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "ANRWatchDogDemo";
    private static final int ANR_TIMEOUT = 5000; // 5秒ANR检测超时

    private TextView anrInfoTextView;
    private ScrollView scrollView;
    private HandlerThread anrWatchThread;
    private Handler anrWatchHandler;
    private Handler mainHandler;
    private AtomicBoolean isAnrDetectionRunning = new AtomicBoolean(false);
    private volatile boolean isAnrWatchDogEnabled = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        initANRWatchDog();
    }

    private void initViews() {
        anrInfoTextView = findViewById(R.id.tv_anr_info);
        scrollView = findViewById(R.id.scroll_view);
        Button triggerUiAnrButton = findViewById(R.id.btn_trigger_anr_ui);
        Button triggerBroadcastAnrButton = findViewById(R.id.btn_trigger_anr_broadcast);
        Button triggerServiceAnrButton = findViewById(R.id.btn_trigger_anr_service);
        Button clearLogButton = findViewById(R.id.btn_clear_log);

        triggerUiAnrButton.setOnClickListener(v -> triggerUiAnr());
        triggerBroadcastAnrButton.setOnClickListener(v -> triggerBroadcastAnr());
        triggerServiceAnrButton.setOnClickListener(v -> triggerServiceAnr());
        clearLogButton.setOnClickListener(v -> clearAnrInfo());

        appendAnrInfo("ANR监测程序已启动\n" + getCurrentTime() + "\n");
    }

    /**
     * 初始化ANR监测狗 - 基于ANRWatchDog原理实现
     * 核心机制：向主线程发送心跳消息，监控线程等待心跳响应
     * 如果在指定时间内没有收到响应，则认为发生了ANR
     */
    private void initANRWatchDog() {
        anrWatchThread = new HandlerThread("ANRWatchDogThread", Thread.NORM_PRIORITY);
        anrWatchThread.start();
        anrWatchHandler = new Handler(anrWatchThread.getLooper());
        mainHandler = new Handler(Looper.getMainLooper());

        // 启动ANR监测循环
        startAnrDetection();
        appendAnrInfo("ANR监测狗已启动，监测间隔: " + ANR_TIMEOUT + "ms\n");
    }

    /**
     * 启动ANR检测
     * 原理：使用CountDownLatch机制，向主线程发送心跳，监测线程等待心跳响应
     */
    private void startAnrDetection() {
        if (!isAnrWatchDogEnabled) return;

        anrWatchHandler.post(() -> {
            while (isAnrWatchDogEnabled && !Thread.currentThread().isInterrupted()) {
                try {
                    // 创建同步计数器，用于等待主线程响应
                    CountDownLatch heartbeatLatch = new CountDownLatch(1);
                    long startTime = System.currentTimeMillis();

                    // 向主线程发送心跳消息
                    mainHandler.post(() -> {
                        // 主线程收到心跳后，释放计数器
                        heartbeatLatch.countDown();
                        // 可选：在UI上显示心跳状态
                        // appendAnrInfo("♥ " + getCurrentTime(true) + "\n");
                    });

                    // 等待主线程响应，超时时间为ANR_TIMEOUT
                    boolean heartbeatReceived = heartbeatLatch.await(ANR_TIMEOUT, TimeUnit.MILLISECONDS);

                    if (!heartbeatReceived && isAnrWatchDogEnabled) {
                        // 主线程在指定时间内没有响应，可能发生ANR
                        long anrDuration = System.currentTimeMillis() - startTime;
                        detectAnr(anrDuration);
                    }

                    // 检测间隔（可以设置更短的间隔以提高检测精度）
                    Thread.sleep(1000);

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
    }

    /**
     * 检测到ANR时的处理
     * 收集详细的堆栈信息并显示
     */
    private void detectAnr(long anrDuration) {
        if (isAnrDetectionRunning.compareAndSet(false, true)) {
            try {
                String anrInfo = buildAnrReport(anrDuration);

                // 通过Handler切换到主线程更新UI（如果主线程恢复响应）
                mainHandler.post(() -> {
                    appendAnrInfo("🚨 检测到ANR! 🚨\n");
                    appendAnrInfo(anrInfo);
                    appendAnrInfo("=====================================\n\n");
                });

                Log.w(TAG, "ANR Detected:\n" + anrInfo);

            } finally {
                // 延迟重置，避免重复检测同一个ANR
                anrWatchHandler.postDelayed(() ->
                        isAnrDetectionRunning.set(false), 3000);
            }
        }
    }

    /**
     * 构建ANR报告，包含详细的线程堆栈信息
     */
    private String buildAnrReport(long anrDuration) {
        StringBuilder report = new StringBuilder();

        // ANR基本信息
        report.append("ANR 检测时间: ").append(getCurrentTime()).append("\n");
        report.append("ANR 持续时间: ").append(anrDuration).append("ms\n");
        report.append("进程名: ").append(getPackageName()).append("\n");
        report.append("线程总数: ").append(Thread.activeCount()).append("\n\n");

        // 获取所有线程的堆栈信息
        Map<Thread, StackTraceElement[]> allThreads = Thread.getAllStackTraces();

        // 优先显示主线程堆栈
        for (Map.Entry<Thread, StackTraceElement[]> entry : allThreads.entrySet()) {
            Thread thread = entry.getKey();
            if (thread.getId() == Looper.getMainLooper().getThread().getId()) {
                report.append("🔥 主线程堆栈 (怀疑发生ANR的线程):\n");
                report.append(formatThreadInfo(thread, entry.getValue()));
                report.append("\n");
                break;
            }
        }

        // 显示其他重要线程堆栈
        report.append("其他线程堆栈信息:\n");
        for (Map.Entry<Thread, StackTraceElement[]> entry : allThreads.entrySet()) {
            Thread thread = entry.getKey();
            if (thread.getId() != Looper.getMainLooper().getThread().getId()) {
                report.append(formatThreadInfo(thread, entry.getValue()));
                report.append("\n");
            }
        }

        return report.toString();
    }

    /**
     * 格式化单个线程的信息
     */
    private String formatThreadInfo(Thread thread, StackTraceElement[] stackTrace) {
        StringBuilder sb = new StringBuilder();
        sb.append("线程名: ").append(thread.getName()).append("\n");
        sb.append("线程ID: ").append(thread.getId()).append("\n");
        sb.append("线程状态: ").append(thread.getState()).append("\n");
        sb.append("优先级: ").append(thread.getPriority()).append("\n");
        sb.append("是否守护线程: ").append(thread.isDaemon()).append("\n");

        if (stackTrace.length > 0) {
            sb.append("堆栈追踪:\n");
            for (int i = 0; i < Math.min(stackTrace.length, 15); i++) { // 限制显示条数
                sb.append("    at ").append(stackTrace[i].toString()).append("\n");
            }
            if (stackTrace.length > 15) {
                sb.append("    ... (还有 ").append(stackTrace.length - 15).append(" 行)\n");
            }
        }

        return sb.toString();
    }

    /**
     * 触发UI线程ANR - 修正后的实现
     */
    private void triggerUiAnr() {
        appendAnrInfo("准备触发UI线程ANR...\n");

        // 使用Handler延迟执行，确保UI先更新
        mainHandler.postDelayed(() -> {
            appendAnrInfo("正在执行UI线程阻塞操作...\n");

            // 在主线程中执行耗时操作，模拟ANR
            try {
                Thread.sleep(8000); // 8秒阻塞，超过ANR检测时间
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            appendAnrInfo("UI线程阻塞操作完成\n");
        }, 500);
    }

    /**
     * 触发广播ANR
     */
    private void triggerBroadcastAnr() {
        appendAnrInfo("准备触发广播ANR...\n");

        BroadcastReceiver anrReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                appendAnrInfo("广播接收器开始执行耗时操作...\n");
                try {
                    // 在onReceive中执行超时操作
                    Thread.sleep(12000); // 12秒，超过广播ANR检测时间(10秒)
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                appendAnrInfo("广播接收器操作完成\n");
                unregisterReceiver(this);
            }
        };

        registerReceiver(anrReceiver, new IntentFilter("com.qzz.anktest.TEST_BROADCAST"));
        sendBroadcast(new Intent("com.qzz.anktest.TEST_BROADCAST"));
    }

    /**
     * 触发Service ANR (模拟)
     */
    private void triggerServiceAnr() {
        appendAnrInfo("模拟Service ANR...\n");

        // 在后台线程模拟Service中的耗时操作
        new Thread(() -> {
            mainHandler.post(() -> {
                appendAnrInfo("Service开始执行耗时操作...\n");
                try {
                    Thread.sleep(25000); // 25秒，超过Service ANR检测时间(20秒)
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                appendAnrInfo("Service操作完成\n");
            });
        }).start();
    }

    /**
     * 清空ANR信息显示
     */
    private void clearAnrInfo() {
        anrInfoTextView.setText("");
        appendAnrInfo("日志已清空 - " + getCurrentTime() + "\n");
    }

    /**
     * 添加ANR信息到文本框
     */
    private void appendAnrInfo(String info) {
        if (anrInfoTextView != null) {
            anrInfoTextView.post(() -> {
                anrInfoTextView.append(info);
                // 自动滚动到底部
                scrollView.post(() -> scrollView.fullScroll(View.FOCUS_DOWN));
            });
        }
    }

    /**
     * 获取当前时间字符串
     */
    private String getCurrentTime() {
        return getCurrentTime(false);
    }

    private String getCurrentTime(boolean includeMillis) {
        String pattern = includeMillis ? "HH:mm:ss.SSS" : "yyyy-MM-dd HH:mm:ss";
        return new SimpleDateFormat(pattern, Locale.getDefault()).format(new Date());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 停止ANR监测
        isAnrWatchDogEnabled = false;

        if (anrWatchThread != null) {
            anrWatchThread.quitSafely();
            try {
                anrWatchThread.join(1000);
            } catch (InterruptedException e) {
                Log.w(TAG, "ANR watch thread interrupted during shutdown");
            }
        }
    }
}


