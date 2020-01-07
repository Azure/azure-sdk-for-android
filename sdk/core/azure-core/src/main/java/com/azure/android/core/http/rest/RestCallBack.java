package com.azure.android.core.http.rest;

public interface RestCallBack<T> {
    void onResponse(T response);
    void onFailure(Throwable t);
}
