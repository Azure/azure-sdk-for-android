// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.internal.util.serializer;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;

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
    String serialize(Object object, SerializerFormat encoding) throws IOException;

    /**
     * Serializes a list into a string with the delimiter specified with the
     * Swagger collection format joining each individual serialized items in
     * the list.
     *
     * @param list   The list to serialize.
     * @param format The Swagger collection format.
     * @return The serialized string.
     */
    String serializeList(List<?> list, CollectionFormat format);

    /**
     * Deserializes a string into an {@code U} object.
     *
     * @param value    The string value to deserialize.
     * @param <U>      The type of the deserialized object.
     * @param type     The type to deserialize.
     * @param encoding The encoding used in the serialized value.
     * @return The deserialized object.
     * @throws IOException exception from deserialization.
     */
    <U> U deserialize(String value, Type type, SerializerFormat encoding) throws IOException;

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
     * @return The default serializer.
     */
    static SerializerAdapter createDefault() {
        return JacksonAdapter.createDefaultSerializerAdapter();
    }

    enum CollectionFormat {
        /**
         * Comma separated values.
         *
         * E.g. foo,bar
         */
        CSV(","),
        /**
         * Space separated values.
         *
         * E.g. foo bar
         */
        SSV(" "),
        /**
         * Tab separated values.
         *
         * E.g. foo\tbar
         */
        TSV("\t"),
        /**
         * Pipe(|) separated values.
         *
         * E.g. foo|bar
         */
        PIPES("|"),
        /**
         * Corresponds to multiple parameter instances instead of multiple values
         * for a single instance.
         *
         * E.g. foo=bar&amp;foo=baz
         */
        MULTI("&");

        /**
         * The delimiter separating the values.
         */
        private String delimiter;

        /**
         * Creates CollectionFormat enum.
         *
         * @param delimiter The delimiter.
         */
        CollectionFormat(String delimiter) {
            this.delimiter = delimiter;
        }

        /**
         * Gets the delimiter used to join a list of parameters.
         *
         * @return The delimiter.
         */
        public String getDelimiter() {
            return delimiter;
        }
    }
}
