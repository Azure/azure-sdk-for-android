package com.azure.android.core.implementation.util.serializer;

import android.text.TextUtils;
import android.util.Log;

import com.azure.android.core.annotation.HeaderCollection;
import com.azure.android.core.implementation.util.serializer.threeten.ThreeTenModule;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;

import java.io.IOException;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import okhttp3.Headers;

/**
 * Implementation of {@link SerializerAdapter} for Jackson.
 */
public class JacksonAdapter implements SerializerAdapter {
    /**
     * An instance of {@link ObjectMapper} to serialize/deserialize objects.
     */
    private final ObjectMapper mapper;

    /**
     * An instance of {@link ObjectMapper} that does not do flattening.
     */
    private final ObjectMapper simpleMapper;

    private final XmlMapper xmlMapper;

    private final ObjectMapper headerMapper;

    private static SerializerAdapter serializerAdapter;

    /*
     * BOM header from some response bodies. To be removed in deserialization.
     */
    private static final String BOM = "\uFEFF";

    /**
     * Creates a new JacksonAdapter instance with default mapper settings.
     */
    public JacksonAdapter() {
        simpleMapper = initializeObjectMapper(new ObjectMapper());
        xmlMapper = initializeObjectMapper(new XmlMapper());
        xmlMapper.configure(ToXmlGenerator.Feature.WRITE_XML_DECLARATION, true);
        xmlMapper.setDefaultUseWrapper(false);
        ObjectMapper flatteningMapper = initializeObjectMapper(new ObjectMapper())
                .registerModule(FlatteningSerializer.getModule(simpleMapper()))
                .registerModule(FlatteningDeserializer.getModule(simpleMapper()));
        mapper = initializeObjectMapper(new ObjectMapper())
                // Order matters: must register in reverse order of hierarchy
                .registerModule(AdditionalPropertiesSerializer.getModule(flatteningMapper))
                .registerModule(AdditionalPropertiesDeserializer.getModule(flatteningMapper))
                .registerModule(FlatteningSerializer.getModule(simpleMapper()))
                .registerModule(FlatteningDeserializer.getModule(simpleMapper()));
        headerMapper = simpleMapper
                .copy()
                .configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
    }

    /**
     * Gets a static instance of {@link ObjectMapper} that doesn't handle flattening.
     *
     * @return an instance of {@link ObjectMapper}.
     */
    protected ObjectMapper simpleMapper() {
        return simpleMapper;
    }

    /**
     * maintain singleton instance of the default serializer adapter.
     *
     * @return the default serializer
     */
    public static synchronized SerializerAdapter createDefaultSerializerAdapter() {
        if (serializerAdapter == null) {
            serializerAdapter = new JacksonAdapter();
        }
        return serializerAdapter;
    }

    /**
     * @return the original serializer type
     */
    public ObjectMapper serializer() {
        return mapper;
    }

    @Override
    public String serialize(Object object, SerializerFormat encoding) throws IOException {
        if (object == null) {
            return null;
        }
        StringWriter writer = new StringWriter();
        if (encoding == SerializerFormat.XML) {
            xmlMapper.writeValue(writer, object);
        } else {
            serializer().writeValue(writer, object);
        }

        return writer.toString();
    }

    @Override
    public String serializeList(List<?> list, CollectionFormat format) {
        if (list == null) {
            return null;
        }
        List<String> serialized = new ArrayList<>();
        for (Object element : list) {
            String raw = serializeRaw(element);
            serialized.add(raw != null ? raw : "");
        }
        return TextUtils.join(format.getDelimiter(), serialized);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T deserialize(String value, final Type type, SerializerFormat encoding) throws IOException {
        if (value == null || value.isEmpty() || value.equals(BOM)) {
            return null;
        }
        // Remove BOM
        if (value.startsWith(BOM)) {
            value = value.replaceFirst(BOM, "");
        }

        final JavaType javaType = createJavaType(type);
        try {
            if (encoding == SerializerFormat.XML) {
                return (T) xmlMapper.readValue(value, javaType);
            } else {
                return (T) serializer().readValue(value, javaType);
            }
        } catch (JsonParseException jpe) {
            // TODO: anuchan log this error once we've logger abstraction
            throw new MalformedValueException(jpe.getMessage(), jpe);
        }
    }

    @Override
    public <T> T deserialize(Headers headers, Type deserializedHeadersType) throws IOException {
        if (deserializedHeadersType == null) {
            return null;
        }
        Map<String, String> headersMap = new HashMap<>();
        for (String headerName : headers.names()) {
            headersMap.put(headerName, headers.get(headerName));
        }
        final String headersJsonString = headerMapper.writeValueAsString(headersMap);
        T deserializedHeaders =
                headerMapper.readValue(headersJsonString, createJavaType(deserializedHeadersType));

        final Class<?> deserializedHeadersClass = getRawClass(deserializedHeadersType);
        final Field[] declaredFields = deserializedHeadersClass.getDeclaredFields();
        for (final Field declaredField : declaredFields) {
            if (declaredField.isAnnotationPresent(HeaderCollection.class)) {
                final Type declaredFieldType = declaredField.getGenericType();
                if (isTypeOrSubTypeOf(declaredField.getType(), Map.class)) {
                    final Type[] mapTypeArguments = getTypeArguments(declaredFieldType);
                    if (mapTypeArguments.length == 2
                            && mapTypeArguments[0] == String.class
                            && mapTypeArguments[1] == String.class) {
                        final HeaderCollection headerCollectionAnnotation =
                                declaredField.getAnnotation(HeaderCollection.class);
                        final String headerCollectionPrefix =
                                headerCollectionAnnotation.value().toLowerCase(Locale.ROOT);
                        final int headerCollectionPrefixLength = headerCollectionPrefix.length();
                        if (headerCollectionPrefixLength > 0) {
                            final Map<String, String> headerCollection = new HashMap<>();
                            for (String headerName : headers.names()) {
                                if (headerName.toLowerCase(Locale.ROOT).startsWith(headerCollectionPrefix)) {
                                    headerCollection.put(headerName.substring(headerCollectionPrefixLength),
                                            headers.get(headerName));
                                }
                            }

                            final boolean declaredFieldAccessibleBackup = declaredField.isAccessible();
                            try {
                                if (!declaredFieldAccessibleBackup) {
                                    declaredField.setAccessible(true);
                                }
                                declaredField.set(deserializedHeaders, headerCollection);
                            } catch (IllegalAccessException ignored) {
                            } finally {
                                if (!declaredFieldAccessibleBackup) {
                                    declaredField.setAccessible(declaredFieldAccessibleBackup);
                                }
                            }
                        }
                    }
                }
            }
        }
        return deserializedHeaders;
    }

    /**
     * Initializes an instance of JacksonMapperAdapter with default configurations
     * applied to the object mapper.
     *
     * @param mapper the object mapper to use.
     */
    private static <T extends ObjectMapper> T initializeObjectMapper(T mapper) {
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
                .configure(SerializationFeature.WRITE_EMPTY_JSON_ARRAYS, true)
                .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
                .configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true)
                .setSerializationInclusion(JsonInclude.Include.NON_NULL)
                .registerModule(new ThreeTenModule())
                .registerModule(ByteArraySerializer.getModule())
                .registerModule(Base64UrlSerializer.getModule())
                .registerModule(DateTimeSerializer.getModule())
                .registerModule(DateTimeRfc1123Serializer.getModule())
                .registerModule(DurationSerializer.getModule());
        mapper.setVisibility(mapper.getSerializationConfig().getDefaultVisibilityChecker()
                .withFieldVisibility(JsonAutoDetect.Visibility.ANY)
                .withSetterVisibility(JsonAutoDetect.Visibility.NONE)
                .withGetterVisibility(JsonAutoDetect.Visibility.NONE)
                .withIsGetterVisibility(JsonAutoDetect.Visibility.NONE));
        return mapper;
    }

    private JavaType createJavaType(Type type) {
        JavaType result;
        if (type == null) {
            result = null;
        } else if (type instanceof JavaType) {
            result = (JavaType) type;
        } else if (type instanceof ParameterizedType) {
            final ParameterizedType parameterizedType = (ParameterizedType) type;
            final Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
            JavaType[] javaTypeArguments = new JavaType[actualTypeArguments.length];
            for (int i = 0; i != actualTypeArguments.length; i++) {
                javaTypeArguments[i] = createJavaType(actualTypeArguments[i]);
            }
            result = mapper
                    .getTypeFactory().constructParametricType((Class<?>) parameterizedType.getRawType(), javaTypeArguments);
        } else {
            result = mapper
                    .getTypeFactory().constructType(type);
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private static Class<?> getRawClass(Type type) {
        if (type instanceof ParameterizedType) {
            return (Class<?>) ((ParameterizedType) type).getRawType();
        } else {
            return (Class<?>) type;
        }
    }

    private static boolean isTypeOrSubTypeOf(Type subType, Type superType) {
        Class<?> sub = getRawClass(subType);
        Class<?> sup = getRawClass(superType);

        return sup.isAssignableFrom(sub);
    }

    private static Type[] getTypeArguments(Type type) {
        if (!(type instanceof ParameterizedType)) {
            return new Type[0];
        }
        return ((ParameterizedType) type).getActualTypeArguments();
    }

    private String serializeRaw(Object object) {
        if (object == null) {
            return null;
        }
        try {
            return serialize(object, SerializerEncoding.JSON)
                    .replaceAll("^\"*", "")
                    .replaceAll("\"*$", "");
        } catch (IOException ex) {
            Log.w("", "Failed to serialize to JSON.", ex);
            return null;
        }
    }
}
