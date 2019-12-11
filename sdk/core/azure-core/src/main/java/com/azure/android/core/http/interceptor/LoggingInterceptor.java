// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.http.interceptor;

import androidx.annotation.NonNull;

import com.azure.android.core.util.CoreUtils;
import com.azure.android.core.util.logging.ClientLogger;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import okhttp3.Headers;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Pipeline interceptor that handles logging of HTTP requests and responses.
 */
public final class LoggingInterceptor implements Interceptor {
    private static final int MAX_BODY_LOG_SIZE = 1024 * 16;
    private static final String REDACTED_PLACEHOLDER = "REDACTED";
    private static final String APPLICATION_OCTET_STREAM = "application/octet-stream";

    private final LogDetailLevel logDetailLevel;
    private final Set<String> allowedHeaderNames;
    private final Set<String> allowedQueryParameterNames;

    /**
     * Creates an LoggingPolicy with the given log configurations.
     *
     * @param LogOptions The HTTP logging configurations.
     */
    private LoggingInterceptor(LogOptions LogOptions) {
        allowedHeaderNames = Collections.emptySet();
        allowedQueryParameterNames = Collections.emptySet();

        if (LogOptions == null) {
            logDetailLevel = LogDetailLevel.NONE;
        } else {
            logDetailLevel = LogOptions.getLogLevel();

            for (String headerName : LogOptions.getAllowedHeaderNames()) {
                allowedHeaderNames.add(headerName.toLowerCase(Locale.ROOT));
            }

            for (String queryParamName : LogOptions.getAllowedQueryParamNames()) {
                allowedQueryParameterNames.add(queryParamName.toLowerCase(Locale.ROOT));
            }
        }
    }

    /**
     * Logs the HTTP request.
     *
     * @param logger     Logger used to log the request.
     * @param request    OkHTTP request being sent to Azure.
     */
    private void logRequest(final ClientLogger logger, final Request request) {
        StringBuilder requestLogMessage = new StringBuilder();
        HttpUrl url = request.url();

        if (logDetailLevel.shouldLogUrl()) {
            requestLogMessage.append("--> ")
                .append(request.method())
                .append(" ")
                .append(url.encodedPath())
                .append(System.lineSeparator())
                .append("Host: ")
                .append(url.scheme())
                .append("://")
                .append(url.host())
                .append(System.lineSeparator());
        }

        if (logDetailLevel.shouldLogHeaders()) {
            addHeadersToLogMessage(request.headers(), requestLogMessage);
        }

        if (!logDetailLevel.shouldLogBody()) {
            logger.info(requestLogMessage.toString());

            return;
        }

        RequestBody requestBody = request.body();

        if (requestBody == null) {
            requestLogMessage.append("(empty body)")
                .append(System.lineSeparator())
                .append("--> END ")
                .append(request.method())
                .append(System.lineSeparator());

            logger.info(requestLogMessage.toString());

            return;
        }

        String contentTypeString = request.header("Content-Type");
        long contentLength = getContentLength(logger, request.headers());

        requestLogMessage.append(contentLength)
            .append("-byte body:")
            .append(System.lineSeparator());

        if (shouldLogBody(contentTypeString, contentLength)) {
            try {
                Buffer buffer = new Buffer();
                MediaType contentType = requestBody.contentType();
                Charset charset = (contentType == null) ? UTF_8 : contentType.charset(UTF_8);
                requestBody.writeTo(buffer);

                if (charset != null) {
                    requestLogMessage.append(buffer.readString(charset));
                } else {
                    logger.warning("Could not log the response body. No encoding charset found.");

                    requestLogMessage.append(" (body content not logged)");
                }
            } catch (IOException e) {
                logger.warning("Could not log the request body", e);

                requestLogMessage.append(" (body content not logged)");
            }
        } else {
            requestLogMessage.append(" (body content not logged)");
        }

        requestLogMessage.append(System.lineSeparator())
            .append("--> END ")
            .append(request.method())
            .append(System.lineSeparator());

        logger.info(requestLogMessage.toString());
    }

    /**
     * Logs thr HTTP response.
     *
     * @param logger   Logger used to log the response.
     * @param response HTTP response returned from Azure.
     * @param tookMs  Nanosecond representation of when the request was sent.
     */
    private void logResponse(final ClientLogger logger, final Response response, long tookMs) {
        String contentLengthString = response.header("Content-Length");
        String bodySize = (CoreUtils.isNullOrEmpty(contentLengthString))
            ? "unknown-length body"
            : contentLengthString + "-byte body";
        StringBuilder responseLogMessage = new StringBuilder();

        if (logDetailLevel.shouldLogUrl()) {
            responseLogMessage.append("<-- ")
                .append(response.code())
                .append(" (")
                .append(tookMs)
                .append(" ms, ")
                .append(bodySize)
                .append(")")
                .append(System.lineSeparator());
        }

        if (logDetailLevel.shouldLogHeaders()) {
            addHeadersToLogMessage(response.headers(), responseLogMessage);
        }

        if (!logDetailLevel.shouldLogBody()) {
            responseLogMessage.append("<-- END HTTP");
            logger.info(responseLogMessage.toString());

            return;
        }

        ResponseBody responseBody = Objects.requireNonNull(response.body());
        String contentTypeString = response.header("Content-Type");
        long contentLength = getContentLength(logger, response.headers());

        responseLogMessage.append("Response body:")
            .append(System.lineSeparator());

        if (shouldLogBody(contentTypeString, contentLength)) {
            try {
                // TODO: Figure if it's possible to log the body without cloning it in its entirety
                BufferedSource bufferedSource = responseBody.source();
                bufferedSource.request(Long.MAX_VALUE); // Buffer the entire body
                Buffer buffer = bufferedSource.getBuffer();
                MediaType contentType = responseBody.contentType();
                Charset charset = (contentType == null) ? UTF_8 : contentType.charset(UTF_8);

                if (charset != null) {
                    responseLogMessage.append(buffer.clone().readString(charset));
                } else {
                    logger.warning("Could not log the response body. No encoding charset found.");

                    responseLogMessage.append(" (body content not logged)");
                }
            } catch (IOException e) {
                logger.warning("Could not log the response body", e);

                responseLogMessage.append(" (body content not logged)");
            }
        } else {
            responseLogMessage.append("(body content not logged)");
        }

        responseLogMessage.append(System.lineSeparator())
            .append("<-- END HTTP");

        logger.info(responseLogMessage.toString());
    }

    /**
     * Generates the redacted URL for logging.
     *
     * @param url URL where the request is being sent.
     * @return A URL with query parameters redacted based on configurations in this policy.
     */
    private String getRedactedUrl(HttpUrl url) {
        return url.newBuilder()
            .query(getAllowedQueryString(url.query()))
            .build()
            .toString();
    }

    /**
     * Generates the logging safe query parameters string.
     *
     * @param queryString Query parameter string from the request URL.
     * @return A query parameter string redacted based on the configurations in this policy.
     */
    private String getAllowedQueryString(String queryString) {
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

    /**
     * Adds HTTP headers into the StringBuilder that is generating the log message. If a header is not allowed it will
     * be redacted.
     *
     * @param headers       HTTP headers on the request or response.
     * @param logMessage StringBuilder that is generating the log message.
     */
    private void addHeadersToLogMessage(Headers headers, StringBuilder logMessage) {
        Set<String> headerNames = headers.names();

        for (String headerName : headerNames) {
            logMessage.append(headerName).append(": ");

            if (allowedHeaderNames.contains(headerName.toLowerCase(Locale.ROOT))) {
                List<String> headerValues = headers.values(headerName);

                for (String headerValue : headerValues) {
                    logMessage.append(headerValue);
                    logMessage.append(System.lineSeparator());
                }
            } else {
                logMessage.append(REDACTED_PLACEHOLDER);
                logMessage.append(System.lineSeparator());
            }
        }
    }

    /**
     * Attempts to retrieve and parse the Content-Length header into a numeric representation.
     *
     * @param logger  Logger used to log a warning if the Content-Length header is an invalid number.
     * @param headers HTTP headers that are checked for containing Content-Length.
     * @return long value indicating the content length of the Request or Response
     */
    private long getContentLength(ClientLogger logger, Headers headers) {
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
    private boolean shouldLogBody(String contentTypeHeader, long contentLength) {
        return !APPLICATION_OCTET_STREAM.equalsIgnoreCase(contentTypeHeader)
            && contentLength != 0
            && contentLength < MAX_BODY_LOG_SIZE;
    }

    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        Request request = chain.request();

        // No logging will be performed, trigger a no-op.
        if (logDetailLevel == LogDetailLevel.NONE) {
            return chain.proceed(request);
        }

        final ClientLogger logger = new ClientLogger(request.method() + " " + getRedactedUrl(request.url()));
        logRequest(logger, request);

        try {
            long startNs = System.nanoTime();
            Response response = chain.proceed(request);
            long tookMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNs);
            logResponse(logger, response, tookMs);

            return response;
        } catch (Exception e) {
            logger.warning("<-- HTTP FAILED: ", e);

            throw e;
        }
    }
}
