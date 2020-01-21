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
     * <p>
     * Note(@anuchan): This method takes the minimal parameters needed to create a service-interface implementation,
     * when needed we can add overloads that take a custom {@link SerializerAdapter}, {@link OkHttpClient}, etc.,
     * which is needed when we enable the user to provide an existing {@link OkHttpClient} configured with SSL, Proxy,
     * SocketPool, etc.
     *
     * @param baseUri          The base URI to to use for service-interface method calls.
     * @param encoding         The HTTP Content-Type for a request or response associated with the service-interface
     *                         method calls.
     * @param interceptors     The interceptors to intercept the request or response associated with the
     *                         service-interface method calls.
     * @param serviceInterface The service-interface class.
     * @param <T>              The type of the service-interface.
     * @return A (proxy based) implementation for the service-interface.
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
     * Creates a {@link Retrofit} instance that can be used to create proxies for a service-interface.
     *
     * @param baseUri           The base URI.
     * @param encoding          The HTTP Content-Type for a request or response.
     * @param serializerAdapter The serializer-deserializer for request or response content.
     * @param interceptors      The interceptors to intercept the request or response.
     * @param httpClient        The OkHttpClient for network calls
     * @return The Retrofit instance.
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
     * Given an azure-core {@link SerializerAdapter}, wrap that in a Retrofit {@link Converter} so that it can be
     * plugged into the Retrofit serialization-deserialization pipeline.
     *
     * @param serializer azure-core {@link SerializerAdapter}.
     * @param encoding   The encoding format.
     * @return A Retrofit {@link Converter}.
     */
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
