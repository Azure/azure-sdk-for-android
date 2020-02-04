package com.azure.android.storage.blob.interceptor;

import com.azure.android.storage.blob.credentials.SasTokenCredential;

import java.io.IOException;

import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class SasTokenCredentialInterceptor implements Interceptor {
    private final SasTokenCredential credential;

    public SasTokenCredentialInterceptor(SasTokenCredential credential) {
        this.credential = credential;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        HttpUrl requestURL = chain.request().url();
        String delimiter = (requestURL.query() != null && requestURL.query() != "") ? "&" : "?";
        requestURL.toString();
        String newURL = requestURL.toString() + delimiter + this.credential.getSasToken();
        Request newRequest = chain.request()
                .newBuilder()
                .url(newURL)
                .build();
        return chain.proceed(newRequest);
    }
}
