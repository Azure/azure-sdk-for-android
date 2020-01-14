package com.azure.android.core.http.rest;

import com.azure.android.core.implementation.util.serializer.SerializerAdapter;
import com.azure.android.core.implementation.util.serializer.SerializerEncoding;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Objects;
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

public final class RestAPIClient {
    private final OkHttpClient httpClient;
    private final Retrofit retrofit;
    private final RestAPIClient.Builder builder;

    private RestAPIClient(OkHttpClient httpClient,
                          Retrofit retrofit,
                          RestAPIClient.Builder builder) {
        this.httpClient = httpClient;
        this.retrofit = retrofit;
        this.builder = builder;
    }

    public String getBaseUrl() {
        return this.retrofit.baseUrl().toString();
    }

    public SerializerAdapter getSerializerAdapter() {
        return this.builder.serializerAdapter;
    }

    public <T> T createServiceClient(final Class<T> service) {
        return this.retrofit.create(service);
    }

    public RestAPIClient.Builder newBuilder() {
        return new Builder(this);
    }

    public void close() {
        httpClient.dispatcher().executorService().shutdown();
        httpClient.connectionPool().evictAll();
        synchronized (httpClient.connectionPool()) {
            httpClient.connectionPool().notifyAll();
        }
        synchronized (AsyncTimeout.class) {
            AsyncTimeout.class.notifyAll();
        }
    }

    public static class Builder {
        private static MediaType XML_MEDIA_TYPE = MediaType.parse("application/xml; charset=UTF-8");
        private static MediaType JSON_MEDIA_TYPE = MediaType.parse("application/json; charset=UTF-8");

        private String baseUrl;
        private OkHttpClient.Builder httpClientBuilder;
        private Retrofit.Builder retrofitBuilder;
        private SerializerEncoding serializerEncoding;
        private SerializerAdapter serializerAdapter;
        private Interceptor credentialsInterceptor;
        private Dispatcher dispatcher;
        private ConnectionPool connectionPool;

        public Builder() {
            this(new OkHttpClient.Builder());
            this.httpClientBuilder
                    .readTimeout(120, TimeUnit.SECONDS)
                    .connectTimeout(60, TimeUnit.SECONDS);
        }

        public Builder(OkHttpClient.Builder httpClientBuilder) {
            Objects.requireNonNull(httpClientBuilder, "httpClientBuilder cannot be null.");
            this.httpClientBuilder = httpClientBuilder;
            this.retrofitBuilder = new Retrofit.Builder();
        }

        private Builder(final RestAPIClient restAPIClient) {
            this(restAPIClient.httpClient.newBuilder());
            this.httpClientBuilder.readTimeout(restAPIClient.httpClient.readTimeoutMillis(), TimeUnit.MILLISECONDS);
            this.httpClientBuilder.connectTimeout(restAPIClient.httpClient.connectTimeoutMillis(), TimeUnit.MILLISECONDS);
            this.httpClientBuilder.interceptors().clear();
            this.httpClientBuilder.networkInterceptors().clear();
            this.baseUrl = restAPIClient.getBaseUrl();
            this.serializerAdapter = restAPIClient.builder.serializerAdapter;
            this.serializerEncoding = restAPIClient.builder.serializerEncoding;
            if (restAPIClient.retrofit.callbackExecutor() != null) {
                this.setCallbackExecutor(restAPIClient.retrofit.callbackExecutor());
            }
            for (Interceptor interceptor : restAPIClient.httpClient.interceptors()) {
                if (interceptor != restAPIClient.builder.credentialsInterceptor) {
                    this.addInterceptor(interceptor);
                }
            }
            this.credentialsInterceptor = restAPIClient.builder.credentialsInterceptor;
            for (Interceptor interceptor : restAPIClient.httpClient.networkInterceptors()) {
                this.addNetworkInterceptor(interceptor);
            }
        }

        public Builder setBaseUrl(String baseUrl) {
            Objects.requireNonNull(baseUrl, "baseUrl cannot be null.");
            this.baseUrl = baseUrl;
            return this;
        }

        public Builder setSerializationFormat(SerializerEncoding encoding) {
            Objects.requireNonNull(encoding, "encoding cannot be null.");
            this.serializerEncoding = encoding;
            return this;
        }

        public Builder setCredentialsInterceptor(Interceptor credentialsInterceptor) {
            Objects.requireNonNull(credentialsInterceptor, "credentialsInterceptor cannot be null.");
            this.credentialsInterceptor = credentialsInterceptor;
            return this;
        }

        public Builder addInterceptor(Interceptor interceptor) {
            Objects.requireNonNull(interceptor, "interceptor cannot be null.");
            this.httpClientBuilder.addInterceptor(interceptor);
            return this;
        }

        public Builder addNetworkInterceptor(Interceptor networkInterceptor) {
            Objects.requireNonNull(networkInterceptor, "networkInterceptor cannot be null.");
            this.httpClientBuilder.addNetworkInterceptor(networkInterceptor);
            return this;
        }

        public Builder setReadTimeout(long timeout, TimeUnit unit) {
            this.httpClientBuilder.readTimeout(timeout, unit);
            return this;
        }

        public Builder setConnectionTimeout(long timeout, TimeUnit unit) {
            this.httpClientBuilder.connectTimeout(timeout, unit);
            return this;
        }

        public Builder setConnectionPool(ConnectionPool connectionPool) {
            this.connectionPool = connectionPool;
            return this;
        }

        public Builder setDispatcher(Dispatcher dispatcher) {
            this.dispatcher = dispatcher;
            return this;
        }

        public Builder setCallbackExecutor(Executor executor) {
            this.retrofitBuilder.callbackExecutor(executor);
            return this;
        }

        public RestAPIClient build() {
            if (this.baseUrl == null) {
                throw new IllegalArgumentException("baseUrl must be set.");
            }
            if (!this.baseUrl.endsWith("/")) {
                this.baseUrl += "/";
            }
            if (connectionPool != null) {
                this.httpClientBuilder.connectionPool(connectionPool);
            }
            if (dispatcher != null) {
                this.httpClientBuilder.dispatcher(dispatcher);
            }
            if (this.credentialsInterceptor != null) {
                this.httpClientBuilder.addInterceptor(this.credentialsInterceptor);
            }
            OkHttpClient httpClient = this.httpClientBuilder
                    .build();
            if (this.serializerEncoding == null) {
                throw new IllegalArgumentException("serializerEncoding must be set.");
            }
            if (this.serializerAdapter == null) {
                this.serializerAdapter = SerializerAdapter.createDefault();
            }
            Converter.Factory converterFactory
                    = wrapSerializerInRetrofitConverter(this.serializerAdapter, this.serializerEncoding);
            return new RestAPIClient(httpClient,
                    this.retrofitBuilder
                            .baseUrl(this.baseUrl)
                            .client(httpClient)
                            .addConverterFactory(converterFactory)
                            .build(),
                    this);
        }

        private static Converter.Factory wrapSerializerInRetrofitConverter(SerializerAdapter serializer,
                                                                           final SerializerEncoding encoding) {
            return new Converter.Factory() {
                @Override
                public Converter<?, RequestBody> requestBodyConverter(Type type,
                                                                      Annotation[] parameterAnnotations,
                                                                      Annotation[] methodAnnotations,
                                                                      Retrofit retrofit) {
                    return value -> RequestBody.create(encoding == SerializerEncoding.XML
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