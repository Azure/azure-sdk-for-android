// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.http;

import androidx.annotation.NonNull;

import java.util.concurrent.TimeUnit;

import okhttp3.ConnectionPool;
import okhttp3.Dispatcher;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okio.AsyncTimeout;
import retrofit2.Retrofit;

/**
 * Type that stores information used to create REST API Client instances that share common resources such as an HTTP
 * connection pool, callback executor, etc.
 */
public class ServiceClient {
    private final OkHttpClient httpClient;
    private final Retrofit retrofit;
    private final ServiceClient.Builder builder;

    /**
     * PRIVATE CTR.
     * <p>
     * Creates ServiceClient.
     *
     * @param httpClient The HTTP client.
     * @param retrofit   The Retrofit to create an API Client.
     * @param builder    The builder.
     */
    private ServiceClient(OkHttpClient httpClient, Retrofit retrofit, ServiceClient.Builder builder) {
        this.httpClient = httpClient;
        this.retrofit = retrofit;
        this.builder = builder;
    }

    /**
     * Gets this {@link ServiceClient}'s Retrofit instance, which can be used to create API Clients.
     *
     * @return The Retrofit instance.
     */
    public Retrofit getRetrofit() {
        return this.retrofit;
    }

    /**
     * Gets a base URL that will be used for any API Client created using a configured Retrofit, which can be accessed
     * using {@link ServiceClient#getRetrofit()}.
     *
     * @return The Retrofit base URL.
     */
    public String getBaseUrl() {
        return this.retrofit.baseUrl().toString();
    }

    /**
     * @return A new builder with configurations copied from this {@link ServiceClient}.
     */
    public ServiceClient.Builder newBuilder() {
        return new Builder(this);
    }

    /**
     * Close and release any resources reserved for the {@link ServiceClient}.
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
     * A builder to configure and build a {@link ServiceClient}.
     */
    public static final class Builder {
        private ConnectionPool connectionPool;
        private Dispatcher dispatcher;
        private Interceptor credentialsInterceptor;
        private OkHttpClient.Builder httpClientBuilder;
        private Retrofit.Builder retrofitBuilder;
        private String baseUrl;

        /**
         * Create a new {@link ServiceClient} builder.
         */
        public Builder() {
            this(new OkHttpClient.Builder());
        }

        /**
         * Create a new {@link ServiceClient} builder that uses a provided {@link OkHttpClient.Builder} for the
         * underlying {@link OkHttpClient}.
         *
         * @param httpClientBuilder {@link OkHttpClient.Builder} with initial configurations applied.
         */
        public Builder(@NonNull OkHttpClient.Builder httpClientBuilder) {
            this.httpClientBuilder = httpClientBuilder;
            this.retrofitBuilder = new Retrofit.Builder();
        }

        /**
         * PRIVATE CTR.
         * <p>
         * Create a builder with configurations copied from the given {@link ServiceClient}.
         *
         * @param serviceClient The service client to use as base.
         */
        private Builder(final ServiceClient serviceClient) {
            this(serviceClient.httpClient.newBuilder());
            this.httpClientBuilder.readTimeout(serviceClient.httpClient.readTimeoutMillis(), TimeUnit.MILLISECONDS);
            this.httpClientBuilder.connectTimeout(serviceClient.httpClient.connectTimeoutMillis(), TimeUnit.MILLISECONDS);
            this.httpClientBuilder.interceptors().clear();
            this.httpClientBuilder.networkInterceptors().clear();

            this.baseUrl = serviceClient.getBaseUrl();

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
         * Set the base URL for any API Client created through the configured Retrofit.
         * <p>
         * The configured Retrofit is accessed using {@link ServiceClient#getRetrofit()}.
         *
         * @param baseUrl The base url
         * @return Builder with base URL applied.
         */
        public Builder setBaseUrl(@NonNull String baseUrl) {
            this.baseUrl = baseUrl;

            return this;
        }

        /**
         * Add an interceptor that gets called for authentication when invoking APIs using any API Client created
         * through the configured Retrofit.
         * <p>
         * The configured Retrofit is accessed using {@link ServiceClient#getRetrofit()}.
         *
         * @param credentialsInterceptor The credential interceptor.
         * @return Builder with credential interceptor applied.
         */
        public Builder setCredentialsInterceptor(@NonNull Interceptor credentialsInterceptor) {
            this.credentialsInterceptor = credentialsInterceptor;

            return this;
        }

        /**
         * Add an interceptor that gets called when invoking APIs using any API Client created through the configured
         * Retrofit.
         * <p>
         * The configured Retrofit is accessed using {@link ServiceClient#getRetrofit()}.
         *
         * @param interceptor The interceptor.
         * @return Builder with interceptor applied.
         */
        public Builder addInterceptor(@NonNull Interceptor interceptor) {
            this.httpClientBuilder.addInterceptor(interceptor);

            return this;
        }

        /**
         * Add a network interceptor that gets called when invoking APIs using any API Client created through the
         * configured Retrofit.
         * <p>
         * The configured Retrofit is accessed using {@link ServiceClient#getRetrofit()}.
         *
         * @param networkInterceptor The interceptor.
         * @return Builder with network interceptor applied.
         */
        public Builder addNetworkInterceptor(@NonNull Interceptor networkInterceptor) {
            this.httpClientBuilder.addNetworkInterceptor(networkInterceptor);

            return this;
        }

        /**
         * Sets the read timeout for the API Client created through the configured Retrofit.
         * <p>
         * The configured Retrofit is accessed using {@link ServiceClient#getRetrofit()}.
         *
         * @param timeout The read timeout.
         * @param unit    The timeout unit.
         * @return Builder with read timeout applied.
         */
        public Builder setReadTimeout(long timeout, @NonNull TimeUnit unit) {
            this.httpClientBuilder.readTimeout(timeout, unit);

            return this;
        }

        /**
         * Sets the connection timeout for the API Client created through the configured Retrofit.
         * <p>
         * The configured Retrofit is accessed using {@link ServiceClient#getRetrofit()}.
         *
         * @param timeout The connection timeout.
         * @param unit    The timeout unit.
         * @return Builder with connection timeout applied.
         */
        public Builder setConnectionTimeout(long timeout, @NonNull TimeUnit unit) {
            this.httpClientBuilder.connectTimeout(timeout, unit);

            return this;
        }

        /**
         * Sets the call timeout for the API Client created through the configured Retrofit.
         * <p>
         * The configured Retrofit is accessed using {@link ServiceClient#getRetrofit()}.
         *
         * @param timeout The call timeout.
         * @param unit    The timeout unit.
         * @return Builder with connection timeout applied.
         */
        public Builder setCallTimeout(long timeout, @NonNull TimeUnit unit) {
            this.httpClientBuilder.callTimeout(timeout, unit);

            return this;
        }

        /**
         * Sets the pool providing connections for APIs invoked on any API Client created through the configured
         * Retrofit.
         * <p>
         * The configured Retrofit is accessed using {@link ServiceClient#getRetrofit()}.
         *
         * @param connectionPool The connection pool.
         * @return Builder with connection pool applied.
         */
        public Builder setConnectionPool(@NonNull ConnectionPool connectionPool) {
            this.connectionPool = connectionPool;

            return this;
        }

        /**
         * Sets the dispatcher used by any API Client created through the configured Retrofit.
         * <p>
         * The configured Retrofit is accessed using {@link ServiceClient#getRetrofit()}.
         *
         * @param dispatcher The dispatcher.
         * @return Builder with dispatcher applied.
         */
        public Builder setDispatcher(@NonNull Dispatcher dispatcher) {
            this.dispatcher = dispatcher;

            return this;
        }

        /**
         * @return A {@link ServiceClient} configured with settings applied through this builder.
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

            OkHttpClient httpClient = this.httpClientBuilder.build();

            return new ServiceClient(
                httpClient,
                this.retrofitBuilder
                    .baseUrl(this.baseUrl)
                    .client(httpClient)
                    // Use same executor instance for OkHttp and Retrofit callback.
                    .callbackExecutor(httpClient.dispatcher().executorService())
                    .build(),
                this);
        }
    }
}
