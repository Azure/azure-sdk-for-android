package com.azure.android.core.http.interceptor;

import com.azure.android.core.util.CoreUtils;

import java.util.Locale;
import java.util.Set;

import okhttp3.Headers;

interface LogUtils {
    int MAX_BODY_LOG_SIZE = 1024 * 16;
    String REDACTED_PLACEHOLDER = "REDACTED";

    /**
     * Attempts to retrieve and parse the Content-Length header into a numeric representation.
     *
     * @param headers HTTP headers that are checked for containing Content-Length.
     * @return long value indicating the content length of the Request or Response
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
     * @return null in case the body should be logged in its entirety, otherwise a message indicating why the body was
     * not logged is returned.
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

        if (!CoreUtils.isNullOrEmpty(contentType) &&
                (contentType.endsWith("octet-stream") || contentType.startsWith("image"))) {
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
     * Generates the logging safe query parameters string.
     *
     * @param queryString Query parameter string from the request URL.
     * @return A query parameter string redacted based on the configurations in this policy.
     */
    static String getAllowedQueryString(String queryString, Set<String> allowedQueryParameterNames) {
        if (CoreUtils.isNullOrEmpty(queryString)) {
            return "";
        }

        StringBuilder queryStringBuilder = new StringBuilder();
        String[] queryParams = queryString.split("&");

        for (String queryParam : queryParams) {
            if (queryStringBuilder.length() > 0) {
                queryStringBuilder.append("&");
            }

            String[] queryPair = queryParam.split("=", 2);

            if (queryPair.length == 2) {
                String queryName = queryPair[0];

                if (allowedQueryParameterNames.contains(queryName.toLowerCase(Locale.ROOT))) {
                    queryStringBuilder.append(queryParam);
                } else {
                    queryStringBuilder.append(queryPair[0]).append("=").append(REDACTED_PLACEHOLDER);
                }
            } else {
                queryStringBuilder.append(queryParam);
            }
        }

        return queryStringBuilder.toString();
    }
}
