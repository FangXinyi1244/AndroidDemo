package com.qzz.demo2.network;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.qzz.demo2.model.dto.ApiResponse;
import com.qzz.demo2.model.dto.Game;
import com.qzz.demo2.model.dto.PageResult;
import com.qzz.demo2.network.interceptor.LoggingInterceptor;

import java.io.IOException;
import java.lang.reflect.Type;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;

public class OkHttpClientManager {
    private static final String TAG = "OkHttpClientManager";
    private static final String BASE_URL = "https://hotfix-service-prod.g.mi.com";
    private static final String API_PATH = "/quick-game/game/search";

    private static volatile OkHttpClient instance;
    private static final Gson gson = new Gson();

    // 自定义回调接口
    public interface ApiCallback<T> {
        void onSuccess(T response);
        void onFailure(String errorMessage);
    }

    public static OkHttpClient getInstance() {
        if (instance == null) {
            synchronized (OkHttpClientManager.class) {
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

                    instance = new OkHttpClient.Builder()
                            .connectTimeout(15, TimeUnit.SECONDS)
                            .readTimeout(15, TimeUnit.SECONDS)
                            .writeTimeout(15, TimeUnit.SECONDS)
                            .sslSocketFactory(sslContext.getSocketFactory(), trustManager)
                            .hostnameVerifier((hostname, session) -> true)
                            .addInterceptor(new LoggingInterceptor())
                            .build();
                }
            }
        }
        return instance;
    }

    public static void searchGames(String search, int current, int size,
                                   ApiCallback<ApiResponse<PageResult<Game>>> callback) {
        try {
            // 构建完整URL
            String fullUrl = BASE_URL + API_PATH;
            HttpUrl httpUrl = HttpUrl.parse(fullUrl);
            if (httpUrl == null) {
                throw new IllegalArgumentException("Invalid URL: " + fullUrl);
            }

            HttpUrl.Builder urlBuilder = httpUrl.newBuilder();

            // 添加查询参数
            if (search != null && !search.isEmpty()) {
                urlBuilder.addQueryParameter("search", search);
            }
            urlBuilder.addQueryParameter("current", String.valueOf(current));
            urlBuilder.addQueryParameter("size", String.valueOf(size));

            // 构建请求
            Request request = new Request.Builder()
                    .url(urlBuilder.build())
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Accept", "application/json")
                    .get()
                    .build();

            Log.d(TAG, "Request URL: " + request.url());

            // 执行异步请求
            getInstance().newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    Log.e(TAG, "Request failed", e);
                    callback.onFailure(e.getMessage());
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) {
                    try {
                        if (!response.isSuccessful() || response.body() == null) {
                            callback.onFailure("Response unsuccessful: " + response.code());
                            return;
                        }

                        String responseData = response.body().string();
                        Log.d(TAG, "Response: " + responseData);

                        // 解析响应数据
                        Type type = new TypeToken<ApiResponse<PageResult<Game>>>(){}.getType();
                        ApiResponse<PageResult<Game>> apiResponse = gson.fromJson(responseData, type);

                        if (apiResponse != null && apiResponse.getCode() == 200) {
                            callback.onSuccess(apiResponse);
                        } else {
                            callback.onFailure("API error: " +
                                    (apiResponse != null ? apiResponse.getMsg() : "Unknown error"));
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Parse response failed", e);
                        callback.onFailure("Parse response failed: " + e.getMessage());
                    }
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Create request failed", e);
            callback.onFailure("Create request failed: " + e.getMessage());
        }
    }
}
