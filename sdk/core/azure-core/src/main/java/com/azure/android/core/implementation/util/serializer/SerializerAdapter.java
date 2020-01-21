// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.implementation.util.serializer;

import java.io.IOException;
import java.lang.reflect.Type;

import okhttp3.Headers;

/**
 * An interface defining the behaviors of a serializer.
 */
public interface SerializerAdapter {
    /**
     * Serializes an object into a string.
     *
     * @param object the object to serialize
     * @param encoding the encoding to use for serialization
     * @return the serialized string. Null if the object to serialize is null
     * @throws IOException exception from serialization
     */
    String serialize(Object object, SerializerFormat encoding) throws IOException;

    /**
     * Deserializes a string into a {@code U} object.
     *
     * @param value the string value to deserialize
     * @param <U> the type of the deserialized object
     * @param type the type to deserialize
     * @param encoding the encoding used in the serialized value
     * @return the deserialized object
     * @throws IOException exception from deserialization
     */
    <U> U deserialize(String value, Type type, SerializerFormat encoding) throws IOException;

    /**
     * Deserialize the provided headers returned from a REST API to an entity instance declared as
     * the model to hold 'Matching' headers.
     *
     * @param headers the REST API returned headers
     * @param <U> the type of the deserialized object
     * @param type the type to deserialize
     * @return instance of header entity type created based on provided {@code headers}, if header
     * entity model does not exists then return null
     * @throws IOException If an I/O error occurs
     */
    <U> U deserialize(Headers headers, Type type) throws IOException;

    /**
     * @return the default serializer
     */
    static SerializerAdapter createDefault() {
        return JacksonAdapter.createDefaultSerializerAdapter();
    }
}