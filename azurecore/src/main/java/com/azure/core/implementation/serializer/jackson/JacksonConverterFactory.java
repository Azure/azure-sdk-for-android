package com.azure.core.implementation.serializer.jackson;


import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;

import java.io.IOException;
import java.io.Reader;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Converter;
import retrofit2.Retrofit;

/**
 * A retrofit2 converter to convert request and response content using customized Jackson adapter.
 */
final class JacksonConverterFactory extends Converter.Factory {
    /**
     * Create an instance using {@code mapper} for conversion.
     *
     * @param mapper a user-provided {@link ObjectMapper} to use
     * @return an instance of JacksonConverterFactory
     */
    public static JacksonConverterFactory create(ObjectMapper mapper) {
        return new JacksonConverterFactory(mapper);
    }

    /**
     * The Jackson object mapper.
     */
    private final ObjectMapper mapper;

    private JacksonConverterFactory(ObjectMapper mapper) {
        if (mapper == null) {
            throw new NullPointerException("mapper == null");
        }
        this.mapper = mapper;
    }

    @Override
    public Converter<ResponseBody, ?> responseBodyConverter(Type type, Annotation[] annotations, Retrofit retrofit) {
        JavaType javaType = mapper.getTypeFactory().constructType(type);
        ObjectReader reader = mapper.readerFor(javaType);
        return new JacksonResponseBodyConverter<>(reader);
    }

    @Override
    public Converter<?, RequestBody> requestBodyConverter(Type type,
                                                          Annotation[] parameterAnnotations, Annotation[] methodAnnotations, Retrofit retrofit) {
        ObjectWriter writer = mapper.writer();
        return new JacksonRequestBodyConverter<>(writer);
    }

    /**
     * An instance of this class converts an object into JSON or XML.
     *
     * @param <T> type of request object
     */
    final class JacksonRequestBodyConverter<T> implements Converter<T, RequestBody> {
        /** Jackson object writer. */
        private final ObjectWriter adapter;

        JacksonRequestBodyConverter(ObjectWriter adapter) {
            this.adapter = adapter;
        }

        @Override public RequestBody convert(T value) throws IOException {
            byte[] bytes = adapter.writeValueAsBytes(value);
            return RequestBody.create(bytes, MediaType.parse("application/json; charset=UTF-8"));
        }
    }

    /**
     * An instance of this class converts a JSON or XML payload into an object.
     *
     * @param <T> the expected object type to convert to
     */
    final class JacksonResponseBodyConverter<T> implements Converter<ResponseBody, T> {
        /** Jackson object reader. */
        private final ObjectReader adapter;

        JacksonResponseBodyConverter(ObjectReader adapter) {
            this.adapter = adapter;
        }

        @Override public T convert(ResponseBody value) throws IOException {
            Reader reader = value.charStream();
            try {
                return adapter.readValue(reader);
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException ignored) {
                    }
                }
            }
        }
    }
}


