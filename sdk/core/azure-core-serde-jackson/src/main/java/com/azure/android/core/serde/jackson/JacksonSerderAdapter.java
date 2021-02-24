// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.serde.jackson;

import android.text.TextUtils;

import com.azure.android.core.serde.jackson.implementation.threeten.ThreeTenModule;
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Implementation of serde adapter for Jackson.
 */
public class JacksonSerderAdapter {
    private static final Pattern PATTERN = Pattern.compile("^\"*|\"*$");

//    private final ClientLogger logger = new ClientLogger(JacksonAdapter.class);

    /**
     * An instance of {@link ObjectMapper} to serialize/deserialize objects.
     */
    private final ObjectMapper mapper;

    /**
     * An instance of {@link ObjectMapper} that does not do flattening.
     */
    private final ObjectMapper simpleMapper;

    private final ObjectMapper xmlMapper;

    private final ObjectMapper headerMapper;

    /*
     * The lazily-created serializer for this ServiceClient.
     */
    private static JacksonSerderAdapter serdeAdapter;

    /**
     * Creates a new JacksonAdapter instance with default mapper settings.
     */
    public JacksonSerderAdapter() {
        simpleMapper = initializeObjectMapper(new ObjectMapper());

        xmlMapper = initializeObjectMapper(new XmlMapper())
            .setDefaultUseWrapper(false)
            .configure(ToXmlGenerator.Feature.WRITE_XML_DECLARATION, true);

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
    private ObjectMapper simpleMapper() {
        return simpleMapper;
    }

    /**
     * maintain singleton instance of the default serializer adapter.
     *
     * @return the default serializer
     */
    public static synchronized JacksonSerderAdapter createDefaultSerdeAdapter() {
        if (serdeAdapter == null) {
            serdeAdapter = new JacksonSerderAdapter();
        }
        return serdeAdapter;
    }

    /**
     * @return the original serializer type
     */
    public ObjectMapper serializer() {
        return mapper;
    }

    /**
     * Serializes an object into a string.
     *
     * @param object the object to serialize
     * @param encoding the encoding to use for serialization
     * @return the serialized string. Null if the object to serialize is null
     * @throws IOException exception from serialization
     */
    public String serialize(Object object, SerdeEncoding encoding) throws IOException {
        if (object == null) {
            return null;
        }

        ByteArrayOutputStream outStream = new AccessibleByteArrayOutputStream();
        serialize(object, encoding, outStream);

        return new String(outStream.toByteArray(), 0, outStream.size(), Charset.forName("UTF-8"));
    }

    /**
     * Serializes an object and writes its output into an {@link OutputStream}.
     *
     * @param object The object to serialize.
     * @param encoding The encoding to use for serialization.
     * @param outputStream The {@link OutputStream} where the serialized object will be written.
     * @throws IOException exception from serialization
     */
    public void serialize(Object object, SerdeEncoding encoding, OutputStream outputStream) throws IOException {
        if (object == null) {
            return;
        }

        if ((encoding == SerdeEncoding.XML)) {
            xmlMapper.writeValue(outputStream, object);
        } else {
            serializer().writeValue(outputStream, object);
        }
    }

    /**
     * Serializes an object into a raw string. The leading and trailing quotes will be trimmed.
     *
     * @param object the object to serialize
     * @return the serialized string. Null if the object to serialize is null
     */
    public String serializeRaw(Object object) {
        if (object == null) {
            return null;
        }
        try {
            return PATTERN.matcher(serialize(object, SerdeEncoding.JSON)).replaceAll("");
        } catch (IOException ex) {
//            logger.warning("Failed to serialize {} to JSON.", object.getClass(), ex);
            return null;
        }
    }

    /**
     * Serializes a list into a string with the delimiter specified with the Swagger collection format joining each
     * individual serialized items in the list.
     *
     * @param list the list to serialize
     * @param format the Swagger collection format
     * @return the serialized string
     */
    public String serializeList(List<?> list, SerdeCollectionFormat format) {
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

    /**
     * Deserializes a string into a {@code T} object.
     *
     * @param value the string value to deserialize
     * @param <T> the type of the deserialized object
     * @param type the type to deserialize
     * @param encoding the encoding used in the serialized value
     * @return the deserialized object
     * @throws IOException exception from reading value to deserialize
     * @throws SerdeParseException exception from deserialization
     */
    @SuppressWarnings("unchecked")
    public <T> T deserialize(String value, Type type, SerdeEncoding encoding) throws IOException {
        if (value == null || value.length() == 0) {
            return null;
        }

        final JavaType javaType = createJavaType(type);
        try {
            if (encoding == SerdeEncoding.XML) {
                return (T) xmlMapper.readValue(value, javaType);
            } else {
                return (T) serializer().readValue(value, javaType);
            }
        } catch (JsonParseException jpe) {
            throw new SerdeParseException(jpe.getMessage(), jpe);
//            throw logger.logExceptionAsError(new MalformedValueException(jpe.getMessage(), jpe));
        }
    }

    /**
     * Deserializes a byte[] into a {@code T} object.
     *
     * @param inputStream The {@link InputStream} containing the serialized object data to deserialize.
     * @param type The type to deserialize.
     * @param encoding The encoding used to serialize value.
     * @param <T> The type of the deserialized object.
     * @return The deserialized object, or null if it cannot be deserialized.
     * @throws IOException exception from deserialization
     * @throws SerdeParseException exception from deserialization
     */
    @SuppressWarnings("unchecked")
    public <T> T deserialize(InputStream inputStream, final Type type, SerdeEncoding encoding)
        throws IOException {
        if (inputStream == null) {
            return null;
        }

        final JavaType javaType = createJavaType(type);
        try {
            if (encoding == SerdeEncoding.XML) {
                return (T) xmlMapper.readValue(inputStream, javaType);
            } else {
                return (T) serializer().readValue(inputStream, javaType);
            }
        } catch (JsonParseException jpe) {
            throw new SerdeParseException(jpe.getMessage(), jpe);
//            throw logger.logExceptionAsError(new MalformedValueException(jpe.getMessage(), jpe));
        }
    }

    /**
     * Deserialize the provided headers returned from a REST API to an entity instance declared as the model to hold
     * 'Matching' headers.
     *
     * 'Matching' headers are the REST API returned headers those with:
     *
     * <ol>
     *   <li>header names same as name of a properties in the entity.</li>
     *   <li>header names start with value of {@link HeaderCollection} annotation applied to
     *   the properties in the entity.</li>
     * </ol>
     *
     * @param <T> the type of the deserialized object
     * @param headers the REST API returned headers
     * @param deserializedHeadersType the type to deserialize
     * @return instance of header entity type created based on provided {@code headers}, if header entity model does not
     * not exists then return null
     * @throws IOException If an I/O error occurs
     */
    public <T> T deserialize(Map<String, String> headers, Type deserializedHeadersType) throws IOException {
        if (deserializedHeadersType == null) {
            return null;
        }

        final String headersJsonString = headerMapper.writeValueAsString(headers);
        T deserializedHeaders =
            headerMapper.readValue(headersJsonString, createJavaType(deserializedHeadersType));

        final Class<?> deserializedHeadersClass = TypeUtil.getRawClass(deserializedHeadersType);
        final Field[] declaredFields = deserializedHeadersClass.getDeclaredFields();
        for (final Field declaredField : declaredFields) {
            if (!declaredField.isAnnotationPresent(HeaderCollection.class)) {
                continue;
            }

            final Type declaredFieldType = declaredField.getGenericType();
            if (!TypeUtil.isTypeOrSubTypeOf(declaredField.getType(), Map.class)) {
                continue;
            }

            final Type[] mapTypeArguments = TypeUtil.getTypeArguments(declaredFieldType);
            if (mapTypeArguments.length == 2
                && mapTypeArguments[0] == String.class
                && mapTypeArguments[1] == String.class) {
                final HeaderCollection headerCollectionAnnotation = declaredField.getAnnotation(HeaderCollection.class);
                final String headerCollectionPrefix = headerCollectionAnnotation.value().toLowerCase(Locale.ROOT);
                final int headerCollectionPrefixLength = headerCollectionPrefix.length();
                if (headerCollectionPrefixLength > 0) {
                    final Map<String, String> headerCollection = new HashMap<>();
                    for (final Map.Entry<String, String> header : headers.entrySet()) {
                        final String headerName = header.getKey();
                        if (headerName.toLowerCase(Locale.ROOT).startsWith(headerCollectionPrefix)) {
                            headerCollection.put(headerName.substring(headerCollectionPrefixLength),
                                header.getValue());
                        }
                    }

                    final boolean declaredFieldAccessibleBackup = declaredField.isAccessible();
                    try {
                        if (!declaredFieldAccessibleBackup) {
                            AccessController.doPrivileged((PrivilegedAction<Object>) () -> {
                                declaredField.setAccessible(true);
                                return null;
                            });
                        }
                        declaredField.set(deserializedHeaders, headerCollection);
                    } catch (IllegalAccessException ignored) {
                    } finally {
                        if (!declaredFieldAccessibleBackup) {
                            AccessController.doPrivileged((PrivilegedAction<Object>) () -> {
                                declaredField.setAccessible(false);
                                return null;
                            });
                        }
                    }
                }
            }
        }
        return deserializedHeaders;
    }

    /**
     * Initializes an instance of JacksonMapperAdapter with default configurations applied to the object mapper.
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
            .registerModule(DateTimeDeserializer.getModule())
            .registerModule(DateTimeRfc1123Serializer.getModule())
            .registerModule(DurationSerializer.getModule())
            .registerModule(UnixTimeSerializer.getModule());
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

}
