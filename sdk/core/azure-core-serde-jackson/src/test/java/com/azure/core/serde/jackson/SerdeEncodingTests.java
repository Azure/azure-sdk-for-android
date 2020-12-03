// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.core.serde.jackson;

import com.azure.core.http.HttpHeaders;
import com.azure.core.serde.SerdeEncoding;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Collections;

/**
 * Tests for {@link SerdeEncoding}.
 */
class SerdeEncodingTests {
    private static final String CONTENT_TYPE = "Content-Type";

    @ParameterizedTest
    @ValueSource(strings = {"application/xml", "application/atom+xml", "text/xml", "application/foo+XML", "TEXT/XML",
        "application/xml;charset=utf-8", "application/atom+xml; charset=utf-32"})
    void recognizeXml(String mimeType) {
        // Arrange
        HttpHeaders headers = new HttpHeaders(Collections.singletonMap(CONTENT_TYPE, mimeType));

        // Act & Assert
        Assertions.assertEquals(SerdeEncoding.XML, SerdeEncoding.fromHeaders(headers.toMap()));
    }

    @ParameterizedTest
    @ValueSource(strings = {"application/json", "application/kv+json", "APPLICATION/JSON", "application/FOO+JSON",
        "application/json;charset=utf-8", "application/config+json; charset=utf-32"})
    void recognizeJson(String mimeType) {
        // Arrange
        HttpHeaders headers = new HttpHeaders(Collections.singletonMap(CONTENT_TYPE, mimeType));

        // Act & Assert
        Assertions.assertEquals(SerdeEncoding.JSON, SerdeEncoding.fromHeaders(headers.toMap()));
    }

    @Test
    void defaultNoContentType() {
        // Arrange
        HttpHeaders headers = new HttpHeaders(Collections.singletonMap("Http-Method", "GET"));

        // Act & Assert
        Assertions.assertEquals(SerdeEncoding.JSON, SerdeEncoding.fromHeaders(headers.toMap()));
    }

    @ParameterizedTest
    @ValueSource(strings = {"application/binary", "invalid-mime-type"})
    void defaultUnsupportedType(String mimeType) {
        // Arrange
        HttpHeaders headers = new HttpHeaders(Collections.singletonMap(CONTENT_TYPE, mimeType));

        // Act & Assert
        Assertions.assertEquals(SerdeEncoding.JSON, SerdeEncoding.fromHeaders(headers.toMap()));
    }
}
