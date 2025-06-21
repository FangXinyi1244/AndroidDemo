package com.qzz.demo2;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.chip.Chip;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.qzz.demo2.Service.SearchService;
import com.qzz.demo2.callback.FragmentInteractionListener;
import com.qzz.demo2.database.DatabaseManager;
import com.qzz.demo2.fragment.LocalStorageFragment;
import com.qzz.demo2.fragment.SearchResultsFragment;
import com.qzz.demo2.model.dao.GameDAO;
import com.qzz.demo2.model.dto.Game;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class HttpActivity extends AppCompatActivity implements FragmentInteractionListener {
    private static final String TAG = "HttpActivity";
    private static final int LOCAL_STORAGE_REFRESH_INTERVAL = 5000; // 5秒刷新间隔
    private static final int LOCAL_STORAGE_DISPLAY_COUNT = 5; // 每次显示5条数据

    // 线程池用于数据库操作
    private ExecutorService databaseExecutor;

    // 数据库管理器
    private DatabaseManager databaseManager;

    // UI组件
    private ImageView searchView;
    private EditText searchEditText;
    private ImageButton searchClearButton;
    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private Chip searchChip;
    // Fragment相关
    private SearchResultsFragment searchResultsFragment;
    private LocalStorageFragment localStorageFragment;
    private MainPagerAdapter pagerAdapter;
    // Handler和数据相关
    private Handler mainHandler;
    private GameDAO gameDAO;
    private SearchService searchService;
    private boolean isServiceBound = false;
    private boolean isLocalStorageRefreshing = false;
//    private int currentPage = 1; // 当前页码
    // 本地存储数据定时刷新任务
    private Runnable localStorageRefreshTask = new Runnable() {
        @Override
        public void run() {
            refreshLocalStorageData();
            mainHandler.postDelayed(this, LOCAL_STORAGE_REFRESH_INTERVAL);
        }


    };
    // Service连接管理
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            SearchService.LocalBinder binder = (SearchService.LocalBinder) service;
            searchService = binder.getService();
            isServiceBound = true;
            Log.d(TAG, "SearchService connected");
        }
        @Override
        public void onServiceDisconnected(ComponentName name) {
            searchService = null;
            isServiceBound = false;
            Log.d(TAG, "SearchService disconnected");
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_http);
        initializeComponents();
        setupViewPager();
        setupSearchFunctionality();
        bindSearchService();
        stopLocalStorageRefresh();
//        startLocalStorageRefresh();
    }
    private void initializeComponents() {
        // 初始化线程池
        databaseExecutor = Executors.newFixedThreadPool(3);

        // 初始化Handler
        mainHandler = new Handler(Looper.getMainLooper());

        // 初始化数据库管理器
        databaseManager = DatabaseManager.getInstance();
        databaseManager.initialize(this);

        // 初始化数据库DAO
        gameDAO = new GameDAO();

        // 初始化UI组件
        searchView = findViewById(R.id.search_icon);
        searchEditText = findViewById(R.id.search_edit_text);
        searchClearButton = findViewById(R.id.search_clear_button);
        tabLayout = findViewById(R.id.tab_layout);
        viewPager = findViewById(R.id.view_pager);
        searchChip = findViewById(R.id.chip);

        // 设置搜索Chip点击事件
        searchChip.setOnClickListener(v -> {
            if(isLocalStorageRefreshing){
                isLocalStorageRefreshing = false;
                stopLocalStorageRefresh();
            }
            else{
                isLocalStorageRefreshing = true;
                startLocalStorageRefresh();
            }
        });

        searchView.setOnClickListener(v -> {
            performSearch();
        });


    }
    private void setupViewPager() {
        // 创建Fragment实例
        searchResultsFragment = new SearchResultsFragment();
        localStorageFragment = new LocalStorageFragment();
        // 创建适配器
        pagerAdapter = new MainPagerAdapter(this, searchResultsFragment, localStorageFragment);
        viewPager.setAdapter(pagerAdapter);
        // 连接TabLayout和ViewPager2
        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> {
                    switch (position) {
                        case 0:
                            tab.setText("搜索结果");
                            break;
                        case 1:
                            tab.setText("本地存储");
                            break;
                    }
                }).attach();
    }
    private void setupSearchFunctionality() {
        // 搜索输入框监听
        searchEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
//                currentPage = 1; // 重置页码
                performSearch();
                return true;
            }
            return false;
        });
        // 搜索输入变化监听
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchClearButton.setVisibility(s.length() > 0 ? View.VISIBLE : View.GONE);
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });
        // 清除搜索按钮
        searchClearButton.setOnClickListener(v -> {
            searchEditText.setText("");
            hideKeyboard();
            if (searchResultsFragment != null) {
                searchResultsFragment.clearResults();
            }
        });
    }
    private void performSearch() {
        String query = searchEditText.getText().toString().trim();
//        if (query.isEmpty()) {
//            Toast.makeText(this, "请输入搜索关键词", Toast.LENGTH_SHORT).show();
//            return;
//        }
        hideKeyboard();


        if (isServiceBound && searchService != null) {
            // 通过Service执行搜索
            searchService.searchGames(query, 1, 20, new SearchService.SearchCallback() {
                @Override
                public void onSearchSuccess(List<Game> games) {
                    mainHandler.post(() -> {
                        // 保存搜索结果到本地数据库
//                        saveGamesToDatabase(games);

                        // 更新搜索结果Fragment
                        if (searchResultsFragment != null) {
                            searchResultsFragment.updateSearchResults(games);
                        }

                        // 切换到搜索结果页面
                        viewPager.setCurrentItem(0);

                        Toast.makeText(HttpActivity.this,
                                "搜索完成，找到 " + games.size() + " 个结果",
                                Toast.LENGTH_SHORT).show();
                    });
                }
                @Override
                public void onSearchFailure(String error) {
                    mainHandler.post(() -> {
                        Log.e(TAG, "搜索失败: " + error);
                        Toast.makeText(HttpActivity.this,
                                "搜索失败: " + error,
                                Toast.LENGTH_SHORT).show();
                    });
                }
                @Override
                public void onLoadMoreSuccess(List<Game> games) {
                    mainHandler.post(() -> {
                        if (searchResultsFragment != null) {
                            searchResultsFragment.appendSearchResults(games);
                        }
                    });
                }
            });
        } else {
            Toast.makeText(this, "搜索服务未准备就绪", Toast.LENGTH_SHORT).show();
        }
    }
    private void saveGamesToDatabase(List<Game> games) {
        if (games != null && !games.isEmpty()) {
            // 在后台线程保存数据
            databaseExecutor.execute(() -> {
                try {
                    int savedCount = gameDAO.insertGames(games);
                    mainHandler.post(() -> {
                        Log.d(TAG, "成功保存 " + savedCount + " 条游戏数据到数据库");
                    });
                } catch (Exception e) {
                    Log.e(TAG, "保存数据到数据库失败", e);
                }
            });
        }
    }
    private void refreshLocalStorageData() {
        // 在后台线程查询数据库
        databaseExecutor.execute(() -> {
            try {
                List<Game> localGames = gameDAO.getRandomGames(LOCAL_STORAGE_DISPLAY_COUNT);

                mainHandler.post(() -> {
                    if (localStorageFragment != null) {
                        localStorageFragment.updateLocalData(localGames);
                    }
                });

            } catch (Exception e) {
                Log.e(TAG, "刷新本地存储数据失败", e);
            }
        });
    }
    private void startLocalStorageRefresh() {
        // 立即执行一次，然后开始定时刷新
        refreshLocalStorageData();
        mainHandler.postDelayed(localStorageRefreshTask, LOCAL_STORAGE_REFRESH_INTERVAL);
    }
    private void stopLocalStorageRefresh() {
        mainHandler.removeCallbacks(localStorageRefreshTask);
    }
    private void bindSearchService() {
        Intent serviceIntent = new Intent(this, SearchService.class);
        bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
    }
    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        // 恢复定时刷新
        if (mainHandler != null) {
            startLocalStorageRefresh();
        }
    }
    @Override
    protected void onPause() {
        super.onPause();
        // 暂停定时刷新以节省资源
        stopLocalStorageRefresh();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();

        // 停止定时任务
        stopLocalStorageRefresh();

        // 解绑Service
        if (isServiceBound) {
            unbindService(serviceConnection);
            isServiceBound = false;
        }

        // 清理数据库资源
        if (databaseManager != null) {
            databaseManager.cleanup();
            Log.d(TAG, "Database resources cleaned up");
        }

        // 清理资源
        if (mainHandler != null) {
            mainHandler.removeCallbacksAndMessages(null);
        }

        if (databaseExecutor != null && !databaseExecutor.isShutdown()) {
            // 立即终止所有任务（慎用shutdownNow，可能丢失数据）
            databaseExecutor.shutdownNow();

            // 或优雅关闭（等待现有任务完成）
            // dbExecutor.shutdown();
            // try {
            //     if (!dbExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
            //         dbExecutor.shutdownNow();
            //     }
            // } catch (InterruptedException e) {
            //     dbExecutor.shutdownNow();
            // }
        }
    }
    // ViewPager2适配器
    private static class MainPagerAdapter extends FragmentStateAdapter {
        private final SearchResultsFragment searchFragment;
        private final LocalStorageFragment localFragment;
        public MainPagerAdapter(@NonNull FragmentActivity fragmentActivity,
                                SearchResultsFragment searchFragment,
                                LocalStorageFragment localFragment) {
            super(fragmentActivity);
            this.searchFragment = searchFragment;
            this.localFragment = localFragment;
        }
        @NonNull
        @Override
        public Fragment createFragment(int position) {
            switch (position) {
                case 0:
                    return searchFragment;
                case 1:
                    return localFragment;
                default:
                    return searchFragment;
            }
        }
        @Override
        public int getItemCount() {
            return 2;
        }
    }


    // 实现接口方法
    @Override
    public void onSearchRequested(String query) {
        // 设置搜索输入框内容并执行搜索
//        currentPage = 1; // 重置页码
        searchEditText.setText(query);
        performSearch();
    }
    @Override
    public void onLoadMoreRequested() {
//        currentPage++;
        loadMoreSearchResults();
    }
    @Override
    public void onGameSaved(Game game, OnSaveResultCallback callback) {
        Toast.makeText(this, "正在下载游戏: " + game.getGameName(), Toast.LENGTH_SHORT).show();
        databaseExecutor.execute(() -> {
            try {
                List<Game> gameToSave = Arrays.asList(game);
                int result = gameDAO.insertGames(gameToSave);

                mainHandler.post(() -> {
                    if (result > 0) {
                        callback.onSaveSuccess();
                    } else {
                        callback.onSaveFailure("游戏可能已存在");
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "保存游戏失败", e);
                mainHandler.post(() -> callback.onSaveFailure(e.getMessage()));
            }
        });
    }
    @Override
    public void onGameDeleted(long gameId, OnDeleteResultCallback callback) {
        databaseExecutor.execute(() -> {
            try {
                int result = gameDAO.deleteGame(gameId);

                mainHandler.post(() -> {
                    if (result > 0) {
                        callback.onDeleteSuccess();
                    } else {
                        callback.onDeleteFailure("删除失败");
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "删除游戏失败", e);
                mainHandler.post(() -> callback.onDeleteFailure(e.getMessage()));
            }
        });
    }

    // 公共方法供Fragment调用
    public void loadMoreSearchResults() {
        String query = searchEditText.getText().toString().trim();
        if (isServiceBound && searchService != null) {
            searchService.loadMoreGames(query);
        }
    }
    public Handler getMainHandler() {
        return mainHandler;
    }
    public GameDAO getGameDAO() {
        return gameDAO;
    }
}
