// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.serde;

import java.util.Map;
import java.util.TreeMap;

/**
 * Supported serialization encoding formats.
 */
public enum SerdeEncoding {
    /**
     * JavaScript Object Notation.
     */
    JSON,

    /**
     * Extensible Markup Language.
     */
    XML;

    private static final String CONTENT_TYPE = "Content-Type";
    private static final Map<String, SerdeEncoding> SUPPORTED_MIME_TYPES;
    private static final TreeMap<String, SerdeEncoding> SUPPORTED_SUFFIXES;
    private static final SerdeEncoding DEFAULT_ENCODING = JSON;

    static {
        // Encodings and suffixes from: https://tools.ietf.org/html/rfc6838
        SUPPORTED_MIME_TYPES = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        SUPPORTED_MIME_TYPES.put("text/xml", XML);
        SUPPORTED_MIME_TYPES.put("application/xml", XML);
        SUPPORTED_MIME_TYPES.put("application/json", JSON);

        SUPPORTED_SUFFIXES = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        SUPPORTED_SUFFIXES.put("xml", XML);
        SUPPORTED_SUFFIXES.put("json", JSON);
    }

    /**
     * Determines the serializer encoding to use based on the Content-Type header.
     *
     * @param headers the headers to check the encoding for.
     * @return the serializer encoding to use for the body. {@link #JSON} if there is no Content-Type header or an
     * unrecognized Content-Type encoding is returned.
     */
    public static SerdeEncoding fromHeaders(Map<String, String> headers) {
        final String mimeContentType = headers.get(CONTENT_TYPE);
        if (mimeContentType == null || mimeContentType.isEmpty()) {
            // 'CONTENT_TYPE' not found. Returning default encoding: DEFAULT_ENCODING
            return DEFAULT_ENCODING;
        }

        final String[] parts = mimeContentType.split(";");
        final SerdeEncoding encoding = SUPPORTED_MIME_TYPES.get(parts[0]);
        if (encoding != null) {
            return encoding;
        }

        final String[] mimeTypeParts = parts[0].split("/");
        if (mimeTypeParts.length != 2) {
            // Content-Type 'parts[0]' does not match mime-type formatting 'type'/'subtype'.
            // Returning default: DEFAULT_ENCODING"
            return DEFAULT_ENCODING;
        }

        // Check the suffix if it does not match the full types.
        final String subtype = mimeTypeParts[1];
        final int lastIndex = subtype.lastIndexOf("+");
        if (lastIndex == -1) {
            return DEFAULT_ENCODING;
        }

        final String mimeTypeSuffix = subtype.substring(lastIndex + 1);
        final SerdeEncoding serdeEncoding = SUPPORTED_SUFFIXES.get(mimeTypeSuffix);
        if (serdeEncoding != null) {
            return serdeEncoding;
        }

        // Content-Type 'mimeContentType' does not match any supported one. Returning default: DEFAULT_ENCODING
        return DEFAULT_ENCODING;
    }
}
