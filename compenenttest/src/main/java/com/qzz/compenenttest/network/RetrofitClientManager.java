package com.qzz.compenenttest.network;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import com.qzz.compenenttest.model.ImageItem;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RetrofitClientManager {
    private static final String TAG = "RetrofitClientManager";
    private static final String BASE_URL = "https://picsum.photos";
    private static final Random random = new Random();
    private static final ExecutorService executor = Executors.newCachedThreadPool();
    private static final Handler mainHandler = new Handler(Looper.getMainLooper());

    public interface ApiCallback<T> {
        void onSuccess(T response);
        void onFailure(String errorMessage);
    }

    public static void getImageList(int page, int limit, ApiCallback<List<ImageItem>> callback) {
        if (callback == null) {
            Log.w(TAG, "回调为null，跳过请求");
            return;
        }

        if (executor.isShutdown()) {
            Log.e(TAG, "线程池已关闭，无法执行请求");
            mainHandler.post(() -> callback.onFailure("线程池已关闭"));
            return;
        }

        executor.execute(() -> {
            try {
                Log.d(TAG, "开始请求图片列表，页码: " + page + ", 数量: " + limit);

                // 模拟网络延迟
                Thread.sleep(1000 + random.nextInt(1000));

                List<ImageItem> imageList = new ArrayList<>();

                for (int i = 0; i < limit; i++) {
                    int width = 400;
                    int height = random.nextInt(600) + 200; // 高度在200-800之间

                    // 添加随机参数避免重复
                    String seed = String.valueOf(System.currentTimeMillis() + i + page * limit);
                    String imageUrl = String.format("%s/%d/%d?random=%s", BASE_URL, width, height, seed);
                    String id = "img_" + page + "_" + i;

                    ImageItem item = new ImageItem(imageUrl, id, width, height);
                    imageList.add(item);
                }

                Log.d(TAG, "成功生成 " + imageList.size() + " 张图片数据，页码: " + page);

                // 安全地切换到主线程回调
                if (mainHandler != null) {
                    mainHandler.post(() -> {
                        try {
                            callback.onSuccess(imageList);
                        } catch (Exception e) {
                            Log.e(TAG, "成功回调执行失败", e);
                        }
                    });
                } else {
                    Log.e(TAG, "mainHandler为null，无法回调");
                }

            } catch (InterruptedException e) {
                Log.w(TAG, "请求被中断");
                Thread.currentThread().interrupt();
                if (mainHandler != null) {
                    mainHandler.post(() -> {
                        try {
                            callback.onFailure("请求被中断");
                        } catch (Exception ex) {
                            Log.e(TAG, "失败回调执行失败", ex);
                        }
                    });
                }
            } catch (Exception e) {
                Log.e(TAG, "获取图片列表失败", e);
                if (mainHandler != null) {
                    mainHandler.post(() -> {
                        try {
                            callback.onFailure("网络请求失败: " + e.getMessage());
                        } catch (Exception ex) {
                            Log.e(TAG, "失败回调执行失败", ex);
                        }
                    });
                }
            }
        });
    }
}
