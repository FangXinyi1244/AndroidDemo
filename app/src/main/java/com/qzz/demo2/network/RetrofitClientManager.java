package com.qzz.demo2.network;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.qzz.demo2.model.dto.ApiResponse;
import com.qzz.demo2.model.dto.Game;
import com.qzz.demo2.model.dto.PageResult;
import com.qzz.demo2.network.interceptor.LoggingInterceptor;

import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Query;


public class RetrofitClientManager {
    private static final String TAG = "RetrofitClientManager";
    private static final String BASE_URL = "https://hotfix-service-prod.g.mi.com";

    private static volatile Retrofit instance;
    private static volatile ApiService apiService;
    private static final Gson gson = new GsonBuilder().create();

    // 自定义回调接口，保持与原代码相同
    public interface ApiCallback<T> {
        void onSuccess(T response);
        void onFailure(String errorMessage);
    }

    // 定义Retrofit API接口
    public interface ApiService {
        @GET("/quick-game/game/search")
        Call<ApiResponse<PageResult<Game>>> searchGames(
                @Query("search") String search,
                @Query("current") int current,
                @Query("size") int size
        );
    }

    public static Retrofit getInstance() {
        if (instance == null) {
            synchronized (RetrofitClientManager.class) {
                if (instance == null) {
                    // 配置SSL
                    X509TrustManager trustManager = new X509TrustManager() {
                        @Override
                        public void checkClientTrusted(X509Certificate[] chain, String authType) {}

                        @Override
                        public void checkServerTrusted(X509Certificate[] chain, String authType) {}

                        @Override
                        public X509Certificate[] getAcceptedIssuers() {
                            return new X509Certificate[0];
                        }
                    };

                    SSLContext sslContext;
                    try {
                        sslContext = SSLContext.getInstance("TLS");
                        sslContext.init(null, new TrustManager[]{trustManager}, new SecureRandom());
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }

                    // 创建OkHttpClient，配置与原代码一致
                    OkHttpClient okHttpClient = new OkHttpClient.Builder()
                            .connectTimeout(15, TimeUnit.SECONDS)
                            .readTimeout(15, TimeUnit.SECONDS)
                            .writeTimeout(15, TimeUnit.SECONDS)
                            .sslSocketFactory(sslContext.getSocketFactory(), trustManager)
                            .hostnameVerifier((hostname, session) -> true)
                            .addInterceptor(new LoggingInterceptor())
                            .build();

                    // 创建Retrofit实例
                    instance = new Retrofit.Builder()
                            .baseUrl(BASE_URL)
                            .client(okHttpClient)
                            .addConverterFactory(GsonConverterFactory.create(gson))
                            .build();
                }
            }
        }
        return instance;
    }

    // 获取API服务实例
    public static ApiService getApiService() {
        if (apiService == null) {
            apiService = getInstance().create(ApiService.class);
        }
        return apiService;
    }

    // 搜索游戏方法，保持与原代码相同的方法签名
    public static void searchGames(String search, int current, int size,
                                   ApiCallback<ApiResponse<PageResult<Game>>> callback) {
        try {
            Log.d(TAG, "Searching games with: search=" + search + ", current=" + current + ", size=" + size);

            // 使用Retrofit执行请求
            Call<ApiResponse<PageResult<Game>>> call = getApiService().searchGames(search, current, size);
            call.enqueue(new Callback<ApiResponse<PageResult<Game>>>() {
                @Override
                public void onResponse(@NonNull Call<ApiResponse<PageResult<Game>>> call,
                                       @NonNull Response<ApiResponse<PageResult<Game>>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        ApiResponse<PageResult<Game>> apiResponse = response.body();
                        Log.d(TAG, "Response: " + apiResponse);

                        if (apiResponse.getCode() == 200) {
                            callback.onSuccess(apiResponse);
                        } else {
                            callback.onFailure("API error: " + apiResponse.getMsg());
                        }
                    } else {
                        callback.onFailure("Response unsuccessful: " +
                                (response.code() + (response.errorBody() != null ?
                                        " - " + response.errorBody().toString() : "")));
                    }
                }

                @Override
                public void onFailure(@NonNull Call<ApiResponse<PageResult<Game>>> call, @NonNull Throwable t) {
                    Log.e(TAG, "Request failed", t);
                    callback.onFailure(t.getMessage());
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Create request failed", e);
            callback.onFailure("Create request failed: " + e.getMessage());
        }
    }
}
