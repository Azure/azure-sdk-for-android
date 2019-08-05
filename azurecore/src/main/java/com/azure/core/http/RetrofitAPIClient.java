package com.azure.core.http;

import com.azure.core.implementation.serializer.SerializerAdapter;
import com.azure.core.implementation.serializer.SerializerEncoding;
import com.azure.core.implementation.serializer.jackson.JacksonAdapter;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Converter;
import retrofit2.Retrofit;

public class RetrofitAPIClient {
    private static Retrofit retrofit = null;

    public static <T> T createAPIService(String baseUri, SerializerAdapter serializerAdapter, HttpPipeline httpPipeline, Class<T> service) {
        return getClient(baseUri, serializerAdapter, httpPipeline).create(service);
    }

    public static <T> T createAPIService(String baseUri, HttpPipeline httpPipeline, Class<T> service) {
        return getClient(baseUri, JacksonAdapter.createDefaultSerializerAdapter(), httpPipeline).create(service);
    }

    private static Retrofit getClient(String baseUri, SerializerAdapter serializerAdapter, HttpPipeline httpPipeline) {
        HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor();
        httpLoggingInterceptor.level(HttpLoggingInterceptor.Level.BODY);
        //
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .addInterceptor(httpLoggingInterceptor);
        //
        if (httpPipeline != null) {
            builder.addInterceptor(wrapHttpPipeline(httpPipeline));
        }
        OkHttpClient client = builder.build();
        retrofit = new Retrofit.Builder()
                .baseUrl(baseUri)
                .addConverterFactory(wrapSerializer(serializerAdapter, SerializerEncoding.JSON))
                .callFactory(client)
                .build();
        return retrofit;
    }

    private static Interceptor wrapHttpPipeline(HttpPipeline httpPipeline) {
        return chain -> {
            HttpPipelineCallContext context = chain.request().tag(HttpPipelineCallContext.class);
            if (context == null) {
                context = new HttpPipelineCallContext();
            }
            final HttpRequest httpRequest = new WrappedOkHttpRequest(chain.request());
            final HttpResponse httpResponse = httpPipeline.send(context.httpRequest(httpRequest), r -> {
                // TODO: anuchan re-evaluate unwrap design for request and response
                Response response = chain.proceed(((UnwrapOkHttp.InnerRequest)r).unwrap());
                return new WrappedOkHttpResponse(response, httpRequest);
            });
            return ((UnwrapOkHttp.InnerResponse)httpResponse).unwrap();
        };
    }

    private static Converter.Factory wrapSerializer(SerializerAdapter serializer, final SerializerEncoding encoding) {
        final MediaType mediaType = encoding == SerializerEncoding.XML
                ? MediaType.parse("application/xml; charset=UTF-8")
                : MediaType.parse("application/json; charset=UTF-8");
        //
        return new Converter.Factory() {
            @Override
            public Converter<?, RequestBody> requestBodyConverter(Type type, Annotation[] parameterAnnotations, Annotation[] methodAnnotations, Retrofit retrofit) {
                return value -> RequestBody.create(serializer.serialize(value, encoding), mediaType);
            }

            @Override
            public Converter<ResponseBody, ?> responseBodyConverter(Type type, Annotation[] annotations, Retrofit retrofit) {
                return (Converter<ResponseBody, Object>) body -> serializer.deserialize(body.charStream(), type, encoding);
            }
        };
    }
}
