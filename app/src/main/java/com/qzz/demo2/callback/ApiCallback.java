package com.qzz.demo2.callback;

import com.qzz.demo2.model.dto.ApiResponse;

public interface ApiCallback<T>     {
    void onSuccess(ApiResponse<T> response);
    void onFailure(Exception e);
}
