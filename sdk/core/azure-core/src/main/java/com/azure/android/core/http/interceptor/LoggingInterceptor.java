// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.http.interceptor;

import androidx.annotation.NonNull;

import com.azure.android.core.util.logging.ClientLogger;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.HashSet;
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

import static com.azure.android.core.http.interceptor.RequestIdInterceptor.REQUEST_ID_HEADER;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Pipeline interceptor that handles logging of HTTP requests and responses.
 */
public final class LoggingInterceptor implements Interceptor {
    private static final String CONTENT_TYPE_HEADER = "Content-Type".toLowerCase(Locale.ROOT);
    private static final String CONTENT_LENGTH_HEADER = "Content-Length".toLowerCase(Locale.ROOT);
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
        boolean contentTypeLogged = false;
        boolean contentLengthLogged = false;

        // According to the OkHttp documentation, the request body headers are only present when this is installed as a
        // network interceptor.
        // https://github.com/square/okhttp/blob/b189a382bccc1b9a01d4672210b69680d73b4306/okhttp-logging-interceptor/src/main/java/okhttp3/logging/HttpLoggingInterceptor.java#L173
        if (requestBody != null) {
            if (requestBody.contentType() != null  && allowedHeaderNames.contains(CONTENT_TYPE_HEADER)) {
                logger.debug("Content-Type: " + requestBody.contentType());
                contentTypeLogged = true;
            }

            if (requestBody.contentLength() != -1  && allowedHeaderNames.contains(CONTENT_LENGTH_HEADER)) {
                logger.debug("Content-Length: " + requestBody.contentLength());
                contentLengthLogged = true;
            }
        }

        logHeaders(request.headers(), contentTypeLogged, contentLengthLogged);

        try {
            String bodySummary = LogUtils.getBodySummary(request.headers(), requestBody, null);

            if (bodySummary == null) {
                Buffer buffer = new Buffer();
                MediaType contentType = Objects.requireNonNull(requestBody).contentType();
                Charset charset = (contentType == null) ? UTF_8 : contentType.charset(UTF_8);

                requestBody.writeTo(buffer);

                if (charset != null) {
                    logger.debug(buffer.readString(charset));
                } else {
                    logger.warning("Could not log the request body. No charset found for decoding.");
                }
            } else {
                logger.debug(bodySummary);
            }
        } catch (IOException | NullPointerException e) {
            logger.warning("Could not log the request body", e);
        }

        logger.info("--> [END " + request.header(REQUEST_ID_HEADER) + "]");
    }

    /**
     * Logs the HTTP response.
     *
     * @param response The HTTP response received form Azure.
     * @param tookMs   Nanosecond representation of when the request was sent.
     */
    @SuppressWarnings("ConstantConditions")
    private void logResponse(final Response response, long tookMs) {
        logger.info("<-- [" + response.header(REQUEST_ID_HEADER) + "] " + "(" + tookMs + ")"); // Request ID + duration

        if (response.code() < 400) {
            logger.info(response.code() + " " + response.message());
        } else {
            logger.warning(response.code() + " " + response.message());
        }

        // TODO: Add log level guard for headers and body.
        ResponseBody responseBody = response.body();
        boolean contentTypeLogged = false;
        boolean contentLengthLogged = false;

        if (responseBody != null) {
            if (responseBody.contentType() != null && allowedHeaderNames.contains(CONTENT_TYPE_HEADER)) {
                logger.debug("Content-Type: " + responseBody.contentType());
                contentTypeLogged = true;
            }

            if (responseBody.contentLength() != -1 && allowedHeaderNames.contains(CONTENT_LENGTH_HEADER)) {
                logger.debug("Content-Length: " + responseBody.contentLength());
                contentLengthLogged = true;
            }
        }

        logHeaders(response.headers(), contentTypeLogged, contentLengthLogged);

        try {
            String bodySummary = LogUtils.getBodySummary(response.headers(), null, responseBody);

            if (bodySummary == null) {
                // TODO: Figure out if it's possible to log the body without cloning it in its entirety.
                BufferedSource bufferedSource = responseBody.source();

                bufferedSource.request(Long.MAX_VALUE); // Buffer the entire body

                Buffer buffer = bufferedSource.getBuffer();
                MediaType contentType = responseBody.contentType();
                Charset charset = (contentType == null) ? UTF_8 : contentType.charset(UTF_8);

                if (charset != null) {
                    logger.debug(buffer.clone().readString(charset));
                } else {
                    logger.warning("Could not log the response body. No charset found for decoding.");
                }
            } else {
                logger.debug(bodySummary);
            }
        } catch (IOException e) {
            logger.warning("Could not log the response body", e);
        }

        logger.info("<-- [END " + response.header(REQUEST_ID_HEADER) + "]");
    }

    /**
     * Adds HTTP headers into the StringBuilder that is generating the log message. If a header is not allowed it will
     * be redacted.
     *
     * @param headers HTTP headers on the request or response.
     */
    private void logHeaders(Headers headers, boolean contentTypeLogged,  boolean contentLengthLogged) {
        int size = headers.size();

        for (int i = 0; i < size; i++) {
            String headerName = headers.name(i);
            String headerValue = headers.value(i);

            // Skip these headers if they have already been logged.
            if ((headerName.toLowerCase(Locale.ROOT).equals(CONTENT_TYPE_HEADER) && contentTypeLogged)
                || (headerName.toLowerCase(Locale.ROOT).equals(CONTENT_LENGTH_HEADER) && contentLengthLogged)) {
                continue;
            }

            if (!allowedHeaderNames.contains(headerName.toLowerCase(Locale.ROOT))) {
                headerValue = LogUtils.REDACTED_PLACEHOLDER;
            }

            logger.debug(headerName + ": " + headerValue);
        }
    }
}
