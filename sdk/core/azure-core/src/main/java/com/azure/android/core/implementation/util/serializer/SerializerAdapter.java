// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.implementation.util.serializer;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;

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
    String serialize(Object object, SerializerEncoding encoding) throws IOException;

    /**
     * Serializes a list into a string with the delimiter specified with the
     * Swagger collection format joining each individual serialized items in
     * the list.
     *
     * @param list the list to serialize
     * @param format the Swagger collection format
     * @return the serialized string
     */
    String serializeList(List<?> list, CollectionFormat format);

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
    <U> U deserialize(String value, Type type, SerializerEncoding encoding) throws IOException;

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

    enum CollectionFormat {
        /**
         * Comma separated values.
         * E.g. foo,bar
         */
        CSV(","),
        /**
         * Space separated values.
         * E.g. foo bar
         */
        SSV(" "),
        /**
         * Tab separated values.
         * E.g. foo\tbar
         */
        TSV("\t"),
        /**
         * Pipe(|) separated values.
         * E.g. foo|bar
         */
        PIPES("|"),
        /**
         * Corresponds to multiple parameter instances instead of multiple values
         * for a single instance.
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
         * @param delimiter the delimiter as a string.
         */
        CollectionFormat(String delimiter) {
            this.delimiter = delimiter;
        }

        /**
         * Gets the delimiter used to join a list of parameters.
         *
         * @return the delimiter of the current collection format.
         */
        public String getDelimiter() {
            return delimiter;
        }
    }
}