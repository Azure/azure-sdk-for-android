// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.implementation.util.serializer;

import java.io.IOException;
import java.lang.reflect.Type;

import okhttp3.Headers;

/**
 * Interface that defines the behaviors of a serializer.
 */
public interface SerializerAdapter {
    /**
     * Serializes an object into a string.
     *
     * @param object   The object to serialize.
     * @param encoding The encoding to use for serialization.
     * @return The serialized string. {@code null} if the object to serialize is {@code null}.
     * @throws IOException exception from serialization.
     */
    String serialize(Object object, SerializerEncoding encoding) throws IOException;

    /**
     * Deserializes a string into a {@code U} object.
     *
     * @param value    The string value to deserialize.
     * @param <U>      The type of the deserialized object.
     * @param type     The type to deserialize.
     * @param encoding The encoding used in the serialized value.
     * @return The deserialized object.
     * @throws IOException exception from deserialization.
     */
    <U> U deserialize(String value, Type type, SerializerEncoding encoding) throws IOException;

    /**
     * Deserializes the provided headers returned from a REST API to an entity instance declared as the model to hold
     * 'Matching' headers.
     *
     * @param headers The headers returned by the REST API.
     * @param <U>     The type of the deserialized object.
     * @param type    The type to deserialize.
     * @return Instance of a header entity type created based on the provided {@code headers}, if the header entity
     * model does not exists then return {@code null}.
     * @throws IOException If an I/O error occurs.
     */
    <U> U deserialize(Headers headers, Type type) throws IOException;

    /**
     * @return The default serializer
     */
    static SerializerAdapter createDefault() {
        return JacksonAdapter.createDefaultSerializerAdapter();
    }
}
