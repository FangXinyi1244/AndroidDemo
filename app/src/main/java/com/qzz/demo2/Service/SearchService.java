package com.qzz.demo2.Service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.qzz.demo2.model.dto.ApiResponse;
import com.qzz.demo2.model.dto.Game;
import com.qzz.demo2.model.dto.PageResult;
import com.qzz.demo2.network.RetrofitClientManager;

import java.util.List;

public class SearchService extends Service {
    private static final String TAG = "SearchService";
    private final IBinder binder = new LocalBinder();

    private int currentPage = 1;
    private String currentQuery = "";
    private SearchCallback currentCallback;

    public interface SearchCallback {
        void onSearchSuccess(List<Game> games);
        void onSearchFailure(String error);
        void onLoadMoreSuccess(List<Game> games);
    }

    public class LocalBinder extends Binder {
        public SearchService getService() {
            return SearchService.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public void searchGames(String query, int page, int pageSize, SearchCallback callback) {
        this.currentQuery = query;
        this.currentPage = page;
        this.currentCallback = callback;

        Log.d(TAG, "开始搜索游戏: " + query + ", 页码: " + page);

        // 使用Retrofit进行网络请求
        RetrofitClientManager.searchGames(query, page, pageSize,
                new RetrofitClientManager.ApiCallback<ApiResponse<PageResult<Game>>>() {
                    @Override
                    public void onSuccess(ApiResponse<PageResult<Game>> response) {
                        if (response != null && response.getData() != null) {
                            PageResult<Game> pageResult = response.getData();
                            List<Game> games = pageResult.getRecords();

                            if (callback != null) {
                                if (page == 1) {
                                    callback.onSearchSuccess(games);
                                } else {
                                    callback.onLoadMoreSuccess(games);
                                }
                            }

                            Log.d(TAG, "搜索成功，获取到 " + games.size() + " 个游戏");
                        } else {
                            if (callback != null) {
                                callback.onSearchFailure("响应数据为空");
                            }
                        }
                    }

                    @Override
                    public void onFailure(String errorMessage) {
                        Log.e(TAG, "搜索失败: " + errorMessage);
                        if (callback != null) {
                            callback.onSearchFailure(errorMessage);
                        }
                    }
                });
    }

    public void loadMoreGames(String query) {
        if (query.equals(currentQuery)) {
            searchGames(query, currentPage + 1, 20, currentCallback);
            currentPage++;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "SearchService destroyed");
    }
}
