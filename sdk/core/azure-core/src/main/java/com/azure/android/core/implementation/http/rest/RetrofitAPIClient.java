// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.implementation.http.rest;

import com.azure.android.core.implementation.util.serializer.SerializerAdapter;
import com.azure.android.core.implementation.util.serializer.SerializerEncoding;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.List;

import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Converter;
import retrofit2.Retrofit;

/**
 * Type to create Retrofit based service-interface implementation.
 */
public class RetrofitAPIClient {
    // OkHttp Request XML Media Type
    private static MediaType XML_MEDIA_TYPE = MediaType.parse("application/xml; charset=UTF-8");
    // OkHttp Request Json Media Type
    private static MediaType JSON_MEDIA_TYPE = MediaType.parse("application/json; charset=UTF-8");

    /**
     * Create a Retrofit based API client implementation for a given service-interface.
     *
     * Note: anuchan: This method takes minimal parameters needed to create service-interface
     * implementation, when needed we can add overloads that takes custom
     * SerializerAdapter, OkHttpClient, which is needed when we enable user to provide
     * an existing OkHttpClient with SSL/Proxy/SocketPool etc.. configured.
     *
     * @param baseUri the base uri to to use for service-interface method calls
     * @param encoding the http content-type for request-response associated with the
     *                 service-interface method calls
     * @param interceptors the interceptors to intercept request-response associated with the
     *                     service-interface method calls
     * @param serviceInterface the service interface class
     * @param <T> the type of the service-interface
     * @return a (proxy based) implementation for the service-interface
     */
    public static <T> T createAPIClient(String baseUri,
                                        SerializerEncoding encoding,
                                        List<Interceptor> interceptors,
                                        Class<T> serviceInterface) {
        return createRetrofit(baseUri,
                encoding,
                SerializerAdapter.createDefault(),
                interceptors,
                new OkHttpClient.Builder().build()).create(serviceInterface);
    }

    /**
     * Creates a Retrofit instance that can be used to create proxies for service interface.
     *
     * @param baseUri the base uri
     * @param encoding the http content-type for request-response
     * @param serializerAdapter the serializer-deserializer for request-response content
     * @param interceptors the interceptors to intercept request-response
     * @param httpClient the OkHttpClient for network calls
     * @return the Retrofit instance
     */
    private static Retrofit createRetrofit(String baseUri,
                                           SerializerEncoding encoding,
                                           SerializerAdapter serializerAdapter,
                                           List<Interceptor> interceptors,
                                           OkHttpClient httpClient) {
        OkHttpClient.Builder builder = httpClient.newBuilder();
        for (Interceptor interceptor : interceptors) {
            builder.addInterceptor(interceptor);
        }
        return new Retrofit.Builder()
                .baseUrl(baseUri)
                .addConverterFactory(wrapSerializerInRetrofitConverter(serializerAdapter,
                        encoding))
                .callFactory(builder.build())
                .build();
    }

    /**
     * Given azure-core SerializerAdapter, wrap that in Retrofit Converter so that it can be
     * plugged into Retrofit serialization-deserialization pipeline.
     *
     * @param serializer azure-core Serializer adapter
     * @param encoding the encoding format
     * @return Retrofit Converter
     */
    private static Converter.Factory wrapSerializerInRetrofitConverter(SerializerAdapter serializer,
                                                                       final SerializerEncoding encoding) {
        return new Converter.Factory() {
            @Override
            public Converter<?, RequestBody> requestBodyConverter(Type type,
                                                                  Annotation[] parameterAnnotations,
                                                                  Annotation[] methodAnnotations,
                                                                  Retrofit retrofit) {
                return value -> RequestBody.create(serializer.serialize(value, encoding),
                        encoding == SerializerEncoding.XML
                                ? XML_MEDIA_TYPE
                                : JSON_MEDIA_TYPE);
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