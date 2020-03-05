// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.http.interceptor;

import java.io.IOException;
import java.util.Locale;
import java.util.Set;

import okhttp3.Headers;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;

import static com.azure.android.core.util.CoreUtils.isNullOrEmpty;

final class LogUtils {
    static final int MAX_BODY_LOG_SIZE = 1024 * 16;
    static final String REDACTED_PLACEHOLDER = "REDACTED";

    private LogUtils() {
        // Empty constructor to prevent instantiation of this class.
    }

    /**
     * Determines if the request or response body should be logged. If not, if returns an appropriate message to log
     * in lieu of said body.
     *
     * @param headers HTTP headers of the request or response.
     * @return "Log body" if the body should be logged in its entirety, otherwise a message indicating why the body
     * was not logged is returned.
     */
    static String getBodySummary(Headers headers, RequestBody requestBody, ResponseBody responseBody) throws IOException {
        String contentEncoding = headers.get("Content-Encoding");
        String contentDisposition = headers.get("Content-Disposition");
        String contentType;
        long contentLength;

        if (requestBody == null) {
            if (responseBody == null) {
                String contentLengthString = headers.get("Content-Length");

                contentType = headers.get("Content-Type");
                contentLength = isNullOrEmpty(contentLengthString) ? 0 : Long.parseLong(contentLengthString);
            } else {
                MediaType responseBodyContentType = responseBody.contentType();

                contentType = (responseBodyContentType == null) ? null : responseBodyContentType.toString();
                contentLength = responseBody.contentLength();
            }
        } else {
            MediaType requestBodyContentType = requestBody.contentType();

            contentType = (requestBodyContentType == null) ? null : requestBodyContentType.toString();
            contentLength = requestBody.contentLength();
        }

        if (!isNullOrEmpty(contentEncoding) && !contentEncoding.equalsIgnoreCase("identity")) {
            return "(encoded body omitted)";
        }

        if (!isNullOrEmpty(contentDisposition) && !contentDisposition.equalsIgnoreCase("inline")) {
            return "(non-inline body omitted)";
        }

        if (!isNullOrEmpty(contentType)
            && (contentType.contains("octet-stream") || contentType.startsWith("image"))) {
            return "(binary body omitted)";
        }

        if (contentLength <= 0) { // A ResponseBody's default Content-Length is -1
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
    static String getRedactedQueryString(HttpUrl url, Set<String> allowedQueryParameterNames) {
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
