// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.http.interceptor;

import com.azure.android.core.util.CoreUtils;

import java.util.Locale;
import java.util.Set;

import okhttp3.Headers;
import okhttp3.HttpUrl;

interface LogUtils {
    int MAX_BODY_LOG_SIZE = 1024 * 16;
    String REDACTED_PLACEHOLDER = "REDACTED";

    /**
     * Attempts to retrieve and parse the Content-Length header into a numeric representation.
     *
     * @param headers HTTP headers that are checked for containing Content-Length.
     * @return The content length of the request or response.
     */
    static long getContentLength(Headers headers) {
        long contentLength = 0;
        String contentLengthString = headers.get("Content-Length");

        if (!CoreUtils.isNullOrEmpty(contentLengthString)) {
            contentLength = Long.parseLong(contentLengthString);
        }

        return contentLength;
    }

    /**
     * Determines if the request or response body should be logged. If not, if returns an appropriate message to log
     * in lieu of said body.
     *
     * @param headers HTTP headers of the request or response.
     * @return "Log body" if the body should be logged in its entirety, otherwise a message indicating why the body
     * was not logged is returned.
     */
    static String evaluateBody(Headers headers) {
        String contentEncoding = headers.get("Content-Encoding");
        String contentDisposition = headers.get("Content-Disposition");
        String contentType = headers.get("Content-Type");
        String contentLength = headers.get("Content-Length");

        if (!CoreUtils.isNullOrEmpty(contentEncoding) && contentEncoding.equalsIgnoreCase("identity")) {
            return "(encoded body omitted)";
        }

        if (!CoreUtils.isNullOrEmpty(contentDisposition) && contentDisposition.equalsIgnoreCase("inline")) {
            return "(non-inline body omitted)";
        }

        if (!CoreUtils.isNullOrEmpty(contentType) && (contentType.endsWith("octet-stream") || contentType.startsWith(
            "image"))) {
            return "(binary body omitted)";
        }

        if (!CoreUtils.isNullOrEmpty(contentLength)) {
            long contentLengthValue = getContentLength(headers);

            if (contentLengthValue == 0) {
                return "(empty body)";
            } else if (contentLengthValue < MAX_BODY_LOG_SIZE) {
                return "(" + contentLengthValue + "-byte body omitted)";
            }
        }

        return "Log body";
    }

    /**
     * Generates a redacted query parameters string based on a given URL.
     *
     * @param url The request URL.
     * @return A query parameter string redacted based on the {@link LogOptions} configuration.
     */
    static String getRedactedQueryString(HttpUrl url, Set<String> allowedQueryParameterNames) {
        Set<String> names = url.queryParameterNames();

        if (names.isEmpty()) {
            return "";
        }

        StringBuilder queryStringBuilder = new StringBuilder();

        for (String name : names) {
            if (allowedQueryParameterNames.contains(name.toLowerCase(Locale.ROOT))) {
                for (String value : url.queryParameterValues(name)) {
                    queryStringBuilder.append(name).append("=").append(value).append("&");
                }
            } else {
                queryStringBuilder.append(name).append("=").append(REDACTED_PLACEHOLDER).append("&");
            }
        }

        queryStringBuilder.setLength(queryStringBuilder.length() - 1);

        return queryStringBuilder.toString();
    }
}
