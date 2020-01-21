// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.http;

import androidx.annotation.NonNull;

import com.azure.android.core.implementation.util.serializer.SerializerAdapter;
import com.azure.android.core.implementation.util.serializer.SerializerFormat;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

import okhttp3.ConnectionPool;
import okhttp3.Dispatcher;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import okio.AsyncTimeout;
import retrofit2.Converter;
import retrofit2.Retrofit;

/**
 * Type that stores the information to create Rest APIClient instances that share common
 * resources such as http connection pool, call back executor.
 */
public class ServiceClient {
    private final OkHttpClient httpClient;
    private final Retrofit retrofit;
    private final ServiceClient.Builder builder;

    /**
     * PRIVATE CTR.
     *
     * Creates ServiceClient.
     *
     * @param httpClient the http client
     * @param retrofit the retrofit to create APIClient
     * @param builder the builder
     */
    private ServiceClient(OkHttpClient httpClient,
                          Retrofit retrofit,
                          ServiceClient.Builder builder) {
        this.httpClient = httpClient;
        this.retrofit = retrofit;
        this.builder = builder;
    }

    /**
     * @return the Retrofit instance that can be used to create APIClients.
     */
    public Retrofit getRetrofit() {
        return this.retrofit;
    }

    /**
     * @return the baseUrl that will be used for any APIClient created using configured Retrofit,
     * the configured Retrofit is accessed using {@link ServiceClient#getRetrofit()}.
     */
    public String getBaseUrl() {
        return this.retrofit.baseUrl().toString();
    }

    /**
     * @return the request-response content serializer adapter that will be used by any APIClient
     * created using configured Retrofit, the configured Retrofit is accessed using {@link ServiceClient#getRetrofit()}.
     */
    public SerializerAdapter getSerializerAdapter() {
        return this.builder.serializerAdapter;
    }

    /**
     * @return a new builder with configurations copied from this {@link ServiceClient}.
     */
    public ServiceClient.Builder newBuilder() {
        return new Builder(this);
    }

    /**
     * Close and release any resources reserved for the ServiceClient.
     */
    public void close() {
        this.httpClient.dispatcher().executorService().shutdown();
        this.httpClient.connectionPool().evictAll();
        synchronized (this.httpClient.connectionPool()) {
            this.httpClient.connectionPool().notifyAll();
        }
        synchronized (AsyncTimeout.class) {
            AsyncTimeout.class.notifyAll();
        }
    }

    /**
     * A builder to configure and build {@link ServiceClient}.
     */
    public static final class Builder {
        private static MediaType XML_MEDIA_TYPE = MediaType.parse("application/xml; charset=UTF-8");
        private static MediaType JSON_MEDIA_TYPE = MediaType.parse("application/json; charset=UTF-8");

        private String baseUrl;
        private OkHttpClient.Builder httpClientBuilder;
        private Retrofit.Builder retrofitBuilder;
        private SerializerFormat serializerFormat;
        private SerializerAdapter serializerAdapter;
        private Interceptor credentialsInterceptor;
        private Dispatcher dispatcher;
        private ConnectionPool connectionPool;

        /**
         * Create a new {@link ServiceClient} builder.
         */
        public Builder() {
            this(new OkHttpClient.Builder());
        }

        /**
         * Create a new {@link ServiceClient} builder that uses provided {@link OkHttpClient.Builder}
         * for the underlying {@link OkHttpClient}.
         *
         * @param httpClientBuilder {@link OkHttpClient.Builder} with initial configurations applied
         */
        public Builder(@NonNull OkHttpClient.Builder httpClientBuilder) {
            this.httpClientBuilder = httpClientBuilder;
            this.retrofitBuilder = new Retrofit.Builder();
        }

        /**
         * PRIVATE CTR.
         *
         * Create a builder with configurations copied from the given {@link ServiceClient}.
         *
         *
         * @param serviceClient the service client to use as base
         */
        private Builder(final ServiceClient serviceClient) {
            this(serviceClient.httpClient.newBuilder());
            this.httpClientBuilder.readTimeout(serviceClient.httpClient.readTimeoutMillis(), TimeUnit.MILLISECONDS);
            this.httpClientBuilder.connectTimeout(serviceClient.httpClient.connectTimeoutMillis(), TimeUnit.MILLISECONDS);
            this.httpClientBuilder.interceptors().clear();
            this.httpClientBuilder.networkInterceptors().clear();
            this.baseUrl = serviceClient.getBaseUrl();
            this.serializerAdapter = serviceClient.builder.serializerAdapter;
            this.serializerFormat = serviceClient.builder.serializerFormat;
            if (serviceClient.retrofit.callbackExecutor() != null) {
                this.setCallbackExecutor(serviceClient.retrofit.callbackExecutor());
            }
            for (Interceptor interceptor : serviceClient.httpClient.interceptors()) {
                if (interceptor != serviceClient.builder.credentialsInterceptor) {
                    this.addInterceptor(interceptor);
                }
            }
            this.credentialsInterceptor = serviceClient.builder.credentialsInterceptor;
            for (Interceptor interceptor : serviceClient.httpClient.networkInterceptors()) {
                this.addNetworkInterceptor(interceptor);
            }
        }

        /**
         * Set the base url for any APIClient created through the configured Retrofit.
         *
         * The configured Retrofit is accessed using {@link ServiceClient#getRetrofit()}.
         *
         * @param baseUrl the base url
         * @return builder with base url applied
         */
        public Builder setBaseUrl(@NonNull String baseUrl) {
            this.baseUrl = baseUrl;
            return this;
        }

        /**
         * Set the request-response content serialization format for any APIClient
         * created through the configured Retrofit.
         *
         * The configured Retrofit is accessed using {@link ServiceClient#getRetrofit()}.
         *
         * @param format the serialization format
         * @return builder with serialization format applied
         */
        public Builder setSerializationFormat(@NonNull SerializerFormat format) {
            this.serializerFormat = format;
            return this;
        }

        /**
         * Add an interceptor that gets called for authentication when invoking APIs using any
         * APIClient created through the configured Retrofit.
         *
         * The configured Retrofit is accessed using {@link ServiceClient#getRetrofit()}.
         *
         * @param credentialsInterceptor the credential interceptor
         * @return builder with credential interceptor applied
         */
        public Builder setCredentialsInterceptor(@NonNull Interceptor credentialsInterceptor) {
            this.credentialsInterceptor = credentialsInterceptor;
            return this;
        }

        /**
         * Add an interceptor that gets called when invoking APIs using any APIClient created through
         * the configured Retrofit.
         *
         * The configured Retrofit is accessed using {@link ServiceClient#getRetrofit()}.
         *
         * @param interceptor the interceptor
         * @return builder with interceptor applied
         */
        public Builder addInterceptor(@NonNull Interceptor interceptor) {
            this.httpClientBuilder.addInterceptor(interceptor);
            return this;
        }

        /**
         * Add a network interceptor that gets called when invoking APIs using any APIClient created
         * through the configured Retrofit.
         *
         * The configured Retrofit is accessed using {@link ServiceClient#getRetrofit()}.
         *
         * @param networkInterceptor the interceptor
         * @return builder with interceptor applied
         */
        public Builder addNetworkInterceptor(@NonNull Interceptor networkInterceptor) {
            this.httpClientBuilder.addNetworkInterceptor(networkInterceptor);
            return this;
        }

        /**
         * Sets the read timeout for the APIClient created through the configured Retrofit.
         *
         * The configured Retrofit is accessed using {@link ServiceClient#getRetrofit()}.
         *
         * @param timeout the read timeout
         * @param unit the timeout unit
         * @return builder with read timeout applied
         */
        public Builder setReadTimeout(long timeout, @NonNull TimeUnit unit) {
            this.httpClientBuilder.readTimeout(timeout, unit);
            return this;
        }

        /**
         * Sets the connection timeout for the APIClient created through the configured Retrofit.
         *
         * The configured Retrofit is accessed using {@link ServiceClient#getRetrofit()}.
         *
         * @param timeout the connection timeout
         * @param unit the timeout unit
         * @return builder with connection timeout applied
         */
        public Builder setConnectionTimeout(long timeout, @NonNull TimeUnit unit) {
            this.httpClientBuilder.connectTimeout(timeout, unit);
            return this;
        }

        /**
         * Sets the pool providing connections for APIs invoked on any APIClient created
         * through the configured Retrofit.
         *
         * The configured Retrofit is accessed using {@link ServiceClient#getRetrofit()}.
         *
         * @param connectionPool the connection pool
         * @return builder with connection pool applied
         */
        public Builder setConnectionPool(@NonNull ConnectionPool connectionPool) {
            this.connectionPool = connectionPool;
            return this;
        }

        /**
         * Sets the dispatcher used by any APIClient created through the configured Retrofit.
         *
         * The configured Retrofit is accessed using {@link ServiceClient#getRetrofit()}.
         *
         * @param dispatcher the dispatcher
         * @return builder with dispatcher applied
         */
        public Builder setDispatcher(@NonNull Dispatcher dispatcher) {
            this.dispatcher = dispatcher;
            return this;
        }

        /**
         * Set the executor to run the callback to notify the result of APIs invoked on an APIClient
         * created through the configured Retrofit.
         *
         * The configured Retrofit is accessed using {@link ServiceClient#getRetrofit()}.
         *
         * @param executor the executor
         * @return builder with executor applied
         */
        public Builder setCallbackExecutor(@NonNull Executor executor) {
            this.retrofitBuilder.callbackExecutor(executor);
            return this;
        }

        /**
         * @return a {@link ServiceClient} with settings configured through this builder applied.
         */
        public ServiceClient build() {
            if (this.baseUrl == null) {
                throw new IllegalArgumentException("baseUrl must be set.");
            }
            if (!this.baseUrl.endsWith("/")) {
                this.baseUrl += "/";
            }
            if (this.connectionPool != null) {
                this.httpClientBuilder.connectionPool(this.connectionPool);
            }
            if (this.dispatcher != null) {
                this.httpClientBuilder.dispatcher(this.dispatcher);
            }
            if (this.credentialsInterceptor != null) {
                this.httpClientBuilder.addInterceptor(this.credentialsInterceptor);
            }
            OkHttpClient httpClient = this.httpClientBuilder
                    .build();
            if (this.serializerFormat == null) {
                throw new IllegalArgumentException("serializerFormat must be set.");
            }
            if (this.serializerAdapter == null) {
                this.serializerAdapter = SerializerAdapter.createDefault();
            }
            Converter.Factory converterFactory
                    = wrapSerializerInRetrofitConverter(this.serializerAdapter, this.serializerFormat);
            return new ServiceClient(httpClient,
                    this.retrofitBuilder
                            .baseUrl(this.baseUrl)
                            .client(httpClient)
                            .addConverterFactory(converterFactory)
                            .build(),
                    this);
        }

        private static Converter.Factory wrapSerializerInRetrofitConverter(SerializerAdapter serializer,
                                                                           final SerializerFormat encoding) {
            return new Converter.Factory() {
                @Override
                public Converter<?, RequestBody> requestBodyConverter(Type type,
                                                                      Annotation[] parameterAnnotations,
                                                                      Annotation[] methodAnnotations,
                                                                      Retrofit retrofit) {
                    return value -> RequestBody.create(encoding == SerializerFormat.XML
                            ? XML_MEDIA_TYPE
                            : JSON_MEDIA_TYPE, serializer.serialize(value, encoding));
                }

                @Override
                public Converter<ResponseBody, ?> responseBodyConverter(Type type,
                                                                        Annotation[] annotations,
                                                                        Retrofit retrofit) {
                    return (Converter<ResponseBody, Object>) body -> serializer.deserialize(body.string(),
                            type,
                            encoding);
                }
            };
        }
    }
}
