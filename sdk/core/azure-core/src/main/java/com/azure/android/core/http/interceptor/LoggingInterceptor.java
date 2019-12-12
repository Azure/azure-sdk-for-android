// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.http.interceptor;

import androidx.annotation.NonNull;

import com.azure.android.core.util.CoreUtils;
import com.azure.android.core.util.logging.ClientLogger;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import kotlin.Pair;
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
import okio.GzipSource;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Pipeline interceptor that handles logging of HTTP requests and responses.
 */
public final class LoggingInterceptor implements Interceptor {
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

    /**
     * Generates the redacted URL for logging.
     *
     * @param url URL where the request is being sent.
     * @return A URL with query parameters redacted based on configurations in this policy.
     */
    private String getRedactedUrl(HttpUrl url) {
        return url.newBuilder()
            .query(LogUtils.getAllowedQueryString(url.query(), allowedQueryParameterNames))
            .build()
            .toString();
    }

    /**
     * Logs the HTTP request.
     *
     * @param logger  Logger used to log the request.
     * @param request OkHTTP request being sent to Azure.
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
        long contentLength = LogUtils.getContentLength(logger, request.headers());

        requestLogMessage.append(contentLength)
            .append("-byte body:")
            .append(System.lineSeparator());

        String responseBodyString = "(body content not logged)";

        if (LogUtils.shouldLogBody(contentTypeString, contentLength)) {
            try {
                Buffer buffer = new Buffer();
                MediaType contentType = requestBody.contentType();
                Charset charset = (contentType == null) ? UTF_8 : contentType.charset(UTF_8);
                requestBody.writeTo(buffer);

                if (charset != null) {
                    responseBodyString = buffer.readString(charset);
                } else {
                    logger.warning("Could not log the response body. No charset found for decoding.");
                }
            } catch (IOException e) {
                logger.warning("Could not log the request body", e);
            }
        }

        requestLogMessage.append(responseBodyString)
            .append(System.lineSeparator())
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
     * @param tookMs   Nanosecond representation of when the request was sent.
     */
    private void logResponse(final ClientLogger logger, final Response response, long tookMs) {
        StringBuilder responseLogMessage = new StringBuilder();
        String contentLengthString = response.header("Content-Length");
        String bodySize = (CoreUtils.isNullOrEmpty(contentLengthString))
            ? "unknown-length body"
            : contentLengthString + "-byte body";

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
        long contentLength = LogUtils.getContentLength(logger, response.headers());

        responseLogMessage.append("Response body:")
            .append(System.lineSeparator());

        String requestBodyString = "(body content not logged)";

        if (LogUtils.shouldLogBody(contentTypeString, contentLength)) {
            try {
                // TODO: Figure if it's possible to log the body without cloning it in its entirety
                BufferedSource bufferedSource = responseBody.source();
                bufferedSource.request(Long.MAX_VALUE); // Buffer the entire body
                Buffer buffer = bufferedSource.getBuffer();

                if ("gzip".equalsIgnoreCase(response.header("Content-Encoding"))) {
                    Long gzippedLength = buffer.size();
                    GzipSource gzipSource = new GzipSource(buffer.clone());
                    buffer = new Buffer();
                    buffer.writeAll(gzipSource);

                    responseLogMessage.append("(decompressed ")
                        .append(gzippedLength)
                        .append("-byte body)")
                        .append(System.lineSeparator());
                }

                MediaType contentType = responseBody.contentType();
                Charset charset = (contentType == null) ? UTF_8 : contentType.charset(UTF_8);

                if (charset != null) {
                    requestBodyString = buffer.clone().readString(charset);
                } else {
                    logger.warning("Could not log the response body. No charset found for decoding.");
                }
            } catch (IOException e) {
                logger.warning("Could not log the response body", e);
            }
        }

        responseLogMessage.append(requestBodyString)
            .append(System.lineSeparator())
            .append("<-- END HTTP");

        logger.info(responseLogMessage.toString());
    }

    /**
     * Adds HTTP headers into the StringBuilder that is generating the log message. If a header is not allowed it will
     * be redacted.
     *
     * @param headers    HTTP headers on the request or response.
     * @param logMessage StringBuilder that is generating the log message.
     */
    private void addHeadersToLogMessage(Headers headers, StringBuilder logMessage) {
        for (Pair<? extends String, ? extends String> header : headers) {
            String headerName = header.getFirst();
            String headerValue = header.getSecond();

            logMessage.append(headerName)
                .append(": ");

            if (allowedHeaderNames.contains(headerName.toLowerCase(Locale.ROOT))) {
                logMessage.append(headerValue);
            } else {
                logMessage.append(LogUtils.REDACTED_PLACEHOLDER);
            }

            logMessage.append(System.lineSeparator());
        }
    }
}
