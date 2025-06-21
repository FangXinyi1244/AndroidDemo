package com.qzz.compenenttest;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.qzz.compenenttest.adapter.SimpleImageAdapter;
import com.qzz.compenenttest.model.ImageItem;
import com.qzz.compenenttest.network.RetrofitClientManager;
import com.scwang.smart.refresh.layout.SmartRefreshLayout;
import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.scwang.smart.refresh.layout.listener.OnLoadMoreListener;
import com.scwang.smart.refresh.layout.listener.OnRefreshListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class ImageWaterfallActivity extends AppCompatActivity {
    private static final String TAG = "ImageWaterfallActivity";

    private SmartRefreshLayout smartRefreshLayout;
    private RecyclerView recyclerView;
    private SimpleImageAdapter adapter;
    private StaggeredGridLayoutManager layoutManager;

    private ExecutorService executorService;
    private Handler mainHandler; // 关键修复：确保正确初始化
    private Future<?> currentLoadTask;
    private volatile boolean isActivityDestroyed = false;

    private int currentPage = 1;
    private volatile boolean isLoading = false; // 使用volatile确保线程安全
    private static final int PAGE_SIZE = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 关键修复：首先初始化Handler和ExecutorService
        initThreadingComponents();
        initViews();
        setupAdapter();
        setupSmartRefreshLayout();

        // 初始加载数据
        loadData(true);
    }

    /**
     * 关键修复：初始化线程相关组件
     */
    private void initThreadingComponents() {
        // 初始化主线程Handler
        mainHandler = new Handler(Looper.getMainLooper());
        // 初始化线程池
        executorService = Executors.newFixedThreadPool(2);

        Log.d(TAG, "线程组件初始化完成");
    }

    private void initViews() {
        try {
            smartRefreshLayout = findViewById(R.id.smart_refresh_layout);
            recyclerView = findViewById(R.id.recycler_view);

            // 添加空指针检查
            if (smartRefreshLayout == null) {
                throw new IllegalStateException("SmartRefreshLayout not found in layout");
            }

            if (recyclerView == null) {
                throw new IllegalStateException("RecyclerView not found in layout");
            }

            // 设置瀑布流布局管理器
            layoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
            layoutManager.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_NONE);
            recyclerView.setLayoutManager(layoutManager);

            Log.d(TAG, "视图组件初始化完成");

        } catch (Exception e) {
            Log.e(TAG, "初始化视图失败", e);
            finish();
        }
    }

    private void setupAdapter() {
        try {
            // 使用无参构造函数，确保初始化为空列表
            adapter = new SimpleImageAdapter();

            // 确保adapter内部的items不为null
            if (adapter.getItems() == null) {
                // 如果SimpleImageAdapter没有初始化items，我们需要确保它有一个空列表
                Log.w(TAG, "Adapter items为null，需要检查SimpleImageAdapter实现");
            }

            recyclerView.setAdapter(adapter);
            Log.d(TAG, "适配器设置完成");

        } catch (Exception e) {
            Log.e(TAG, "设置适配器失败", e);
            // 创建默认适配器避免崩溃
            adapter = new SimpleImageAdapter();
            recyclerView.setAdapter(adapter);
        }
    }

    private void setupSmartRefreshLayout() {
        if (smartRefreshLayout == null) {
            Log.e(TAG, "SmartRefreshLayout为null，无法设置");
            return;
        }

        try {
            smartRefreshLayout.setEnableRefresh(true);
            smartRefreshLayout.setEnableLoadMore(true);
            smartRefreshLayout.setEnableAutoLoadMore(true);
            smartRefreshLayout.setEnableScrollContentWhenLoaded(true);

            // 下拉刷新监听
            smartRefreshLayout.setOnRefreshListener(new OnRefreshListener() {
                @Override
                public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                    Log.d(TAG, "触发下拉刷新");
                    refreshData();
                }
            });

            // 上拉加载监听
            smartRefreshLayout.setOnLoadMoreListener(new OnLoadMoreListener() {
                @Override
                public void onLoadMore(@NonNull RefreshLayout refreshLayout) {
                    Log.d(TAG, "触发上拉加载更多");
                    loadMoreData();
                }
            });

            smartRefreshLayout.setPrimaryColorsId(android.R.color.white, android.R.color.black);
            Log.d(TAG, "SmartRefreshLayout设置完成");

        } catch (Exception e) {
            Log.e(TAG, "设置SmartRefreshLayout失败", e);
        }
    }

    private void refreshData() {
        Log.d(TAG, "开始刷新数据");
        currentPage = 1;
        if (smartRefreshLayout != null) {
            smartRefreshLayout.setEnableLoadMore(true);
        }
        loadData(true);
    }

    private void loadMoreData() {
        Log.d(TAG, "开始加载更多数据，当前页码: " + currentPage);
        loadData(false);
    }

    /**
     * 关键修复：完善的数据加载方法
     */
    private void loadData(boolean isRefresh) {
        // 状态检查
        if (isLoading) {
            Log.w(TAG, "正在加载中，跳过本次请求");
            return;
        }

        if (isActivityDestroyed || isFinishing()) {
            Log.w(TAG, "Activity已销毁或正在销毁，取消加载");
            return;
        }

        // 关键组件检查
        if (mainHandler == null) {
            Log.e(TAG, "mainHandler为null，无法继续");
            handleComponentError("主线程Handler未初始化");
            return;
        }

        if (executorService == null || executorService.isShutdown()) {
            Log.e(TAG, "ExecutorService不可用");
            handleComponentError("线程池不可用");
            return;
        }

        if (adapter == null) {
            Log.e(TAG, "adapter为null，重新初始化");
            setupAdapter();
            if (adapter == null) {
                handleComponentError("适配器初始化失败");
                return;
            }
        }

        isLoading = true;
        Log.d(TAG, "开始加载数据，页码: " + currentPage + ", 刷新模式: " + isRefresh);

        // 关键修复：改进CompletableFuture的使用
        currentLoadTask = CompletableFuture
                .supplyAsync(this::performNetworkRequest, executorService)
                .whenCompleteAsync((result, throwable) -> {
                    if (throwable != null) {
                        Log.e(TAG, "异步任务失败", throwable);
                        handleLoadFailure(throwable.getMessage());
                    } else {
                        Log.d(TAG, "网络请求成功，数据大小: " +
                                (result != null ? result.size() : "null"));
                        handleLoadSuccess(result, isRefresh);
                    }
                }, mainHandler::post);
    }

    /**
     * 关键修复：网络请求的同步封装
     */
    private List<ImageItem> performNetworkRequest() {
        try {
            CountDownLatch latch = new CountDownLatch(1);
            AtomicReference<List<ImageItem>> result = new AtomicReference<>();
            AtomicReference<String> error = new AtomicReference<>();

            RetrofitClientManager.getImageList(currentPage, PAGE_SIZE,
                    new RetrofitClientManager.ApiCallback<List<ImageItem>>() {
                        @Override
                        public void onSuccess(List<ImageItem> response) {
                            result.set(response);
                            latch.countDown();
                        }

                        @Override
                        public void onFailure(String errorMessage) {
                            error.set(errorMessage);
                            latch.countDown();
                        }
                    });

            // 等待请求完成，添加超时保护
            boolean completed = latch.await(30, TimeUnit.SECONDS);
            if (!completed) {
                throw new RuntimeException("网络请求超时");
            }

            if (error.get() != null) {
                throw new RuntimeException(error.get());
            }

            List<ImageItem> responseData = result.get();
            return responseData != null ? responseData : new ArrayList<>();

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("请求被中断");
        } catch (Exception e) {
            Log.e(TAG, "网络请求执行失败", e);
            throw new RuntimeException("网络请求失败: " + e.getMessage());
        }
    }

    /**
     * 处理加载成功的响应
     */
    private void handleLoadSuccess(List<ImageItem> response, boolean isRefresh) {
        // 再次检查Activity状态
        if (isActivityDestroyed || isFinishing()) {
            Log.w(TAG, "Activity已销毁，忽略加载成功回调");
            return;
        }

        try {
            isLoading = false;

            // 检查关键组件
            if (adapter == null) {
                Log.e(TAG, "处理成功回调时adapter为null");
                return;
            }

            if (smartRefreshLayout == null) {
                Log.e(TAG, "处理成功回调时smartRefreshLayout为null");
                return;
            }

            if (response != null && !response.isEmpty()) {
                if (isRefresh) {
                    // 刷新：使用setNewData方法
                    adapter.setNewData(response);
                    smartRefreshLayout.finishRefresh();
                    Log.d(TAG, "刷新完成，新数据大小: " + response.size());
                } else {
                    // 加载更多：使用addData方法
                    adapter.addData(response);
                    smartRefreshLayout.finishLoadMore();
                    Log.d(TAG, "加载更多完成，新增数据: " + response.size());
                }

                currentPage++;

                // 检查是否还有更多数据
                if (response.size() < PAGE_SIZE) {
                    smartRefreshLayout.finishLoadMoreWithNoMoreData();
                    showToast("没有更多数据了");
                }
            } else {
                // 没有数据的情况
                if (isRefresh) {
                    smartRefreshLayout.finishRefresh();
                    if (adapter.isEmpty()) {
                        showToast("暂无图片数据");
                    }
                } else {
                    smartRefreshLayout.finishLoadMoreWithNoMoreData();
                    showToast("没有更多数据了");
                }
                Log.d(TAG, "没有获取到新数据");
            }

        } catch (Exception e) {
            Log.e(TAG, "处理加载成功回调时出错", e);
            handleLoadFailure("数据处理失败");
        }
    }

    /**
     * 关键修复：安全的失败处理
     */
    private void handleLoadFailure(String errorMessage) {
        // 检查Activity状态
        if (isActivityDestroyed || isFinishing()) {
            Log.w(TAG, "Activity已销毁，忽略加载失败回调");
            return;
        }

        try {
            isLoading = false;

            if (smartRefreshLayout != null) {
                smartRefreshLayout.finishRefresh(false);
                smartRefreshLayout.finishLoadMore(false);
            }

            String message = "加载失败: " + (errorMessage != null ? errorMessage : "未知错误");
            showToast(message);
            Log.e(TAG, message);

        } catch (Exception e) {
            Log.e(TAG, "处理加载失败回调时出错", e);
        }
    }

    /**
     * 关键修复：组件错误处理
     */
    private void handleComponentError(String message) {
        Log.e(TAG, "组件错误: " + message);
        isLoading = false;

        if (mainHandler != null) {
            mainHandler.post(() -> {
                if (!isActivityDestroyed && !isFinishing()) {
                    showToast("初始化错误: " + message);
                    // 可以考虑重新初始化或者finish Activity
                }
            });
        }
    }

    /**
     * 安全的Toast显示
     */
    private void showToast(String message) {
        if (!isActivityDestroyed && !isFinishing()) {
            try {
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Log.e(TAG, "显示Toast失败", e);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "Activity暂停");
        // 暂停时可以选择取消当前任务
        if (currentLoadTask != null && !currentLoadTask.isDone()) {
            Log.d(TAG, "取消当前加载任务");
            currentLoadTask.cancel(true);
            isLoading = false;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "Activity销毁开始");
        isActivityDestroyed = true;

        // 取消所有未完成的任务
        if (currentLoadTask != null && !currentLoadTask.isDone()) {
            Log.d(TAG, "取消未完成的加载任务");
            currentLoadTask.cancel(true);
        }

        // 关闭线程池
        if (executorService != null && !executorService.isShutdown()) {
            Log.d(TAG, "关闭线程池");
            executorService.shutdown();
            try {
                if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                    Log.w(TAG, "线程池未在5秒内关闭，强制关闭");
                    executorService.shutdownNow();
                }
            } catch (InterruptedException e) {
                Log.w(TAG, "等待线程池关闭被中断");
                executorService.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }

        // 清理Handler引用
        mainHandler = null;
        Log.d(TAG, "Activity销毁完成");
    }
}
