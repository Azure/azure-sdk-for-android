package com.azure.core.implementation;

import com.azure.core.implementation.serializer.SerializerAdapter;
import com.azure.core.implementation.serializer.SerializerEncoding;
import com.azure.core.implementation.serializer.jackson.JacksonAdapter;

import java.util.List;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;

public class RetrofitAPIClient {
    private static Retrofit retrofit = null;

    private static Retrofit getClient(String baseUri, SerializerAdapter serializerAdapter, List<Interceptor> interceptors) {
        HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor();
        httpLoggingInterceptor.level(HttpLoggingInterceptor.Level.BODY);
        //
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .addInterceptor(httpLoggingInterceptor);
        if (interceptors != null) {
            for (Interceptor interceptor : interceptors) {
                builder = builder.addInterceptor(interceptor);
            }
        }
        OkHttpClient client = builder.build();
        retrofit = new Retrofit.Builder()
                .baseUrl(baseUri)
                .addConverterFactory(serializerAdapter.retrofitConverterFactory(SerializerEncoding.JSON))
                .client(client)
                .build();
        return retrofit;
    }

    public static <T> T createAPIService(String baseUri, SerializerAdapter serializerAdapter, List<Interceptor> interceptors, Class<T> service) {
        return getClient(baseUri, serializerAdapter, interceptors).create(service);
    }


    public static <T> T createAPIService(String baseUri, List<Interceptor> interceptors, Class<T> service) {
        return getClient(baseUri, JacksonAdapter.createDefaultSerializerAdapter(), interceptors).create(service);
    }
}
