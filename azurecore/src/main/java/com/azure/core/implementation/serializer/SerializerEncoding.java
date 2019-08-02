// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.serializer;

import java.util.List;

import okhttp3.Headers;

/**
 * Supported serialization encoding formats.
 */
public enum SerializerEncoding {
    /**
     * JavaScript Object Notation.
     */
    JSON,

    /**
     * Extensible Markup Language.
     */
    XML;

    /**
     * Determines the serializer encoding to use based on the Content-Type header.
     *
     * @param headers the headers to check the encoding for
     * @return the serializer encoding to use for the body
     */
    public static SerializerEncoding fromHeaders(Headers headers) { // TODO: anuchan don't leak OkHttp headers
        List<String> mimeContentTypes = headers.values("Content-Type");
        if (mimeContentTypes.size() > 0) {
            String mimeContentType = mimeContentTypes.get(0);
            if (mimeContentType != null) {
                String[] parts = mimeContentType.split(";");
                if (parts[0].equalsIgnoreCase("application/xml") || parts[0].equalsIgnoreCase("text/xml")) {
                    return XML;
                }
            }
        }
        //
        return JSON;
    }
}
