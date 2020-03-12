// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.http.interceptor;

import androidx.annotation.NonNull;

import com.azure.android.core.util.HttpUtil;
import com.azure.android.core.util.logging.ClientLogger;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
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

import static com.azure.android.core.http.interceptor.RequestIdInterceptor.REQUEST_ID_HEADER;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Pipeline interceptor that handles logging of HTTP requests and responses.
 */
public final class LoggingInterceptor implements Interceptor {
    private static final String CONTENT_TYPE_HEADER = "Content-Type";
    private static final String CONTENT_LENGTH_HEADER = "Content-Length";
    private final ClientLogger logger;
    private final Set<String> allowedHeaderNames;
    private final Set<String> allowedQueryParameterNames;

    /**
     * Creates an LoggingPolicy with the given log configurations and a default {@link ClientLogger}.
     *
     * @param logOptions The HTTP logging configurations.
     */
    public LoggingInterceptor(LogOptions logOptions) {
        this(logOptions, ClientLogger.getDefault(LoggingInterceptor.class));
    }

    /**
     * Creates an LoggingPolicy with the given log configurations and {@link ClientLogger}.
     *
     * @param logOptions   The HTTP logging configurations.
     * @param clientLogger The {@link ClientLogger} implementation to use for logging.
     */
    public LoggingInterceptor(LogOptions logOptions, ClientLogger clientLogger) {
        logger = clientLogger;

        if (logOptions == null) {
            allowedHeaderNames = Collections.emptySet();
            allowedQueryParameterNames = Collections.emptySet();
        } else {
            allowedHeaderNames = new HashSet<>();
            allowedQueryParameterNames = new HashSet<>();

            for (String headerName : logOptions.getAllowedHeaderNames()) {
                allowedHeaderNames.add(headerName.toLowerCase(Locale.ROOT));
            }

            for (String queryParamName : logOptions.getAllowedQueryParamNames()) {
                allowedQueryParameterNames.add(queryParamName.toLowerCase(Locale.ROOT));
            }
        }
    }

    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        Request request = chain.request();

        try {
            logRequest(request);

            long startNs = System.nanoTime();
            Response response = chain.proceed(request);
            long tookMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNs);

            logResponse(response, tookMs);

            return response;
        } catch (Exception e) {
            logger.warning("OPERATION FAILED: ", e);
            logger.info("<-- [END" + request.header(REQUEST_ID_HEADER) + "]");

            throw e;
        }
    }

    /**
     * Logs the HTTP request.
     *
     * @param request The HTTP request being sent to Azure.
     */
    private void logRequest(final Request request) throws IOException {
        HttpUrl url = request.url();

        logger.info("--> [" + request.header(REQUEST_ID_HEADER) + "]"); // Request ID
        logger.info(request.method() + " " + url.encodedPath() + LogUtils.getRedactedQueryString(url,
            allowedQueryParameterNames)); // URL path + query
        logger.info("Host: " + url.scheme() + "://" + url.host()); // URL host

        // TODO: Add log level guard for headers and body.
        RequestBody requestBody = request.body();
        String contentType = HttpUtil.getContentType(request);
        Long contentLength = HttpUtil.getContentLength(request);

        logHeaders(request.headers(), contentType, contentLength);

        String bodySummary = LogUtils.getBodySummary(request.headers(), contentType, contentLength);
        if (bodySummary != null) {
            logger.debug(bodySummary);
        } else if (requestBody != null) {
            MediaType bodyContentType = requestBody.contentType();
            Charset charset = (bodyContentType == null) ? UTF_8 : bodyContentType.charset(UTF_8);
            Buffer buffer = new Buffer();
            try {
                requestBody.writeTo(buffer);
                logger.debug(buffer.readString(charset == null ? UTF_8 : charset));
            } catch (IOException e) {
                logger.warning("Could not log the request body", e);
            }
        } else {
            logger.debug("(empty body)");
        }

        logger.info("--> [END " + request.header(REQUEST_ID_HEADER) + "]");
    }

    /**
     * Logs the HTTP response.
     *
     * @param response The HTTP response received form Azure.
     * @param tookMs   Nanosecond representation of when the request was sent.
     */
    private void logResponse(final Response response, long tookMs) {
        logger.info("<-- [" + response.header(REQUEST_ID_HEADER) + "] " + "(" + tookMs + ")"); // Request ID + duration

        if (response.code() < 400) {
            logger.info(response.code() + " " + response.message());
        } else {
            logger.warning(response.code() + " " + response.message());
        }

        // TODO: Add log level guard for headers and body.
        ResponseBody responseBody = response.body();
        String contentType = HttpUtil.getContentType(response);
        Long contentLength = HttpUtil.getContentLength(response);

        logHeaders(response.headers(), contentType, contentLength);

        String bodySummary = LogUtils.getBodySummary(response.headers(), contentType, contentLength);
        if (bodySummary != null) {
            logger.debug(bodySummary);
        } else if (responseBody != null) {
            MediaType bodyContentType = responseBody.contentType();
            Charset charset = (bodyContentType == null) ? UTF_8 : bodyContentType.charset(UTF_8);
            try {
                logger.debug(responseBody.source().peek().readString(charset == null ? UTF_8 : charset));
            } catch (IOException e) {
                logger.warning("Could not log the response body", e);
            }
        } else {
            logger.debug("(empty body)");
        }

        logger.info("<-- [END " + response.header(REQUEST_ID_HEADER) + "]");
    }

    /**
     * Adds HTTP headers into the StringBuilder that is generating the log message. If a header is not allowed it will
     * be redacted.
     *
     * @param headers HTTP headers on the request or response.
     * @param contentType Content type value previously extracted from headers or body.
     * @param contentLength Content length value previously extracted from headers or body.
     */
    private void logHeaders(Headers headers, String contentType, Long contentLength) {
        if (contentType != null) {
            String headerName = CONTENT_TYPE_HEADER;
            String headerValue = contentType;
            if (!allowedHeaderNames.contains(headerName.toLowerCase(Locale.ROOT))) {
                headerValue = LogUtils.REDACTED_PLACEHOLDER;
            }
            logger.debug(headerName + ": " + headerValue);
        }

        if (contentLength != null) {
            String headerName = CONTENT_LENGTH_HEADER;
            String headerValue = contentLength.toString();
            if (!allowedHeaderNames.contains(headerName.toLowerCase(Locale.ROOT))) {
                headerValue = LogUtils.REDACTED_PLACEHOLDER;
            }
            logger.debug(headerName + ": " + headerValue);
        }

        String contentTypeLower = CONTENT_TYPE_HEADER.toLowerCase(Locale.ROOT);
        String contentLengthLower = CONTENT_LENGTH_HEADER.toLowerCase(Locale.ROOT);

        for (int i = 0; i < headers.size(); i++) {
            String headerName = headers.name(i);
            String headerValue = headers.value(i);

            String headerNameLower = headerName.toLowerCase(Locale.ROOT);

            // Skip headers which have already been logged.
            if (contentTypeLower.equals(headerNameLower) || contentLengthLower.equals(headerNameLower)) {
                continue;
            }

            if (!allowedHeaderNames.contains(headerNameLower)) {
                headerValue = LogUtils.REDACTED_PLACEHOLDER;
            }

            logger.debug(headerName + ": " + headerValue);
        }
    }
}
