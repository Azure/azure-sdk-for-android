package com.azure.core.http;

import com.azure.core.implementation.serializer.SerializerAdapter;
import com.azure.core.implementation.serializer.SerializerEncoding;
import com.azure.core.implementation.serializer.jackson.JacksonAdapter;

import okhttp3.OkHttpClient;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;

public class RetrofitAPIClient {
    private static Retrofit retrofit = null;

    private static Retrofit getClient(String baseUri, SerializerAdapter serializerAdapter, HttpPipeline httpPipeline) {
        HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor();
        httpLoggingInterceptor.level(HttpLoggingInterceptor.Level.BODY);
        //
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .addInterceptor(httpLoggingInterceptor);
        //
        if (httpPipeline != null) {
            builder.addInterceptor(chain -> {
                Response response = httpPipeline.send(chain.request(), request -> chain.proceed(request));
                return response;
            });
        }
        //
        OkHttpClient client = builder.build();
        retrofit = new Retrofit.Builder()
                .baseUrl(baseUri)
                .addConverterFactory(serializerAdapter.retrofitConverterFactory(SerializerEncoding.JSON))
                .client(client)
                .build();
        return retrofit;
    }

    public static <T> T createAPIService(String baseUri, SerializerAdapter serializerAdapter, HttpPipeline httpPipeline, Class<T> service) {
        return getClient(baseUri, serializerAdapter, httpPipeline).create(service);
    }

    public static <T> T createAPIService(String baseUri, HttpPipeline httpPipeline, Class<T> service) {
        return getClient(baseUri, JacksonAdapter.createDefaultSerializerAdapter(), httpPipeline).create(service);
    }
}
