package com.azure.android.core.http.interceptor;

import com.azure.android.core.util.CoreUtils;
import com.azure.android.core.util.logging.ClientLogger;

import java.util.Locale;
import java.util.Set;

import okhttp3.Headers;

interface LogUtils {
    int MAX_BODY_LOG_SIZE = 1024 * 16;
    String REDACTED_PLACEHOLDER = "REDACTED";
    String APPLICATION_OCTET_STREAM = "application/octet-stream";

    /**
     * Attempts to retrieve and parse the Content-Length header into a numeric representation.
     *
     * @param logger  Logger used to log a warning if the Content-Length header is an invalid number.
     * @param headers HTTP headers that are checked for containing Content-Length.
     * @return long value indicating the content length of the Request or Response
     */
    static long getContentLength(ClientLogger logger, Headers headers) {
        long contentLength = 0;
        String contentLengthString = headers.get("Content-Length");

        if (CoreUtils.isNullOrEmpty(contentLengthString)) {
            return contentLength;
        }

        try {
            contentLength = Long.parseLong(contentLengthString);
        } catch (NumberFormatException | NullPointerException e) {
            logger.warning("Could not parse the HTTP header content-length: '{}'.", headers.get("content-length"), e);
        }

        return contentLength;
    }

    /**
     * Determines if the request or response body should be logged.
     *
     * <p>The request or response body is logged if the Content-Type is not "application/octet-stream" and the body
     * isn't empty and is less than 16KB in size.</p>
     *
     * @param contentTypeHeader Content-Type header value.
     * @param contentLength     Content-Length header represented as a numeric.
     * @return A flag indicating if the request or response body should be logged.
     */
    static boolean shouldLogBody(String contentTypeHeader, long contentLength) {
        return !APPLICATION_OCTET_STREAM.equalsIgnoreCase(contentTypeHeader)
            && contentLength != 0
            && contentLength < MAX_BODY_LOG_SIZE;
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
