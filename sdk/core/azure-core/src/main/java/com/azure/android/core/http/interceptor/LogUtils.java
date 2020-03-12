// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.http.interceptor;

import androidx.annotation.NonNull;

import java.util.Locale;
import java.util.Set;

import okhttp3.Headers;
import okhttp3.HttpUrl;

import static com.azure.android.core.util.CoreUtils.isNullOrEmpty;

final class LogUtils {
    static final int MAX_BODY_LOG_SIZE = 1024 * 16;
    static final String REDACTED_PLACEHOLDER = "REDACTED";

    private LogUtils() {
        // Empty constructor to prevent instantiation of this class.
    }

    /**
     * Produces a loggable representation of the a body based on its headers, content type, and content length. If the
     * body appears to contain text and is below MAX_BODY_LOG_SIZE then it will be logged verbatim, otherwise a summary
     * message describing the body is returned.
     *
     * @param headers The headers of the body.
     * @param contentType The content type of the body.
     * @param contentLength The content length of the body.
     * @return The text of the body if applicable, otherwise a summary message describing the body.
     */
    static String getBodySummary(@NonNull Headers headers, String contentType, Long contentLength) {
        String contentEncoding = headers.get("Content-Encoding");
        if (!isNullOrEmpty(contentEncoding) && !contentEncoding.equalsIgnoreCase("identity")) {
            return "(encoded body omitted)";
        }

        String contentDisposition = headers.get("Content-Disposition");
        if (!isNullOrEmpty(contentDisposition) && !contentDisposition.equalsIgnoreCase("inline")) {
            return "(non-inline body omitted)";
        }

        if (!isNullOrEmpty(contentType)
            && (contentType.contains("octet-stream") || contentType.startsWith("image"))) {
            return "(binary body omitted)";
        }

        if (contentLength == null || contentLength <= 0) {
            return "(empty body)";
        } else if (contentLength > MAX_BODY_LOG_SIZE) {
            return "(" + contentLength + "-byte body omitted)";
        }

        return null;
    }

    /**
     * Generates a redacted query parameters string based on a given URL.
     *
     * @param url The request URL.
     * @return A query parameter string redacted based on the {@link LogOptions} configuration.
     */
    static String getRedactedQueryString(@NonNull HttpUrl url, @NonNull Set<String> allowedQueryParameterNames) {
        Set<String> names = url.queryParameterNames();

        if (names.isEmpty()) {
            return "";
        }

        StringBuilder queryStringBuilder = new StringBuilder("?");

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
