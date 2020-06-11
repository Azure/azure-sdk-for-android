// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.http.interceptor;

import androidx.annotation.NonNull;

import com.azure.android.core.http.HttpHeader;
import com.azure.android.core.util.HttpUtil;
import com.azure.android.core.util.logging.ClientLogger;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import okhttp3.Headers;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

import static com.azure.android.core.util.CoreUtil.isNullOrEmpty;

/**
 * Pipeline interceptor that handles logging of HTTP requests and responses.
 */
public final class LoggingInterceptor implements Interceptor {
    static final int MAX_BODY_LOG_SIZE = 1024 * 16;
    static final String REDACTED_PLACEHOLDER = "REDACTED";

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

    /**
     * Intercept and log a request response pair in the pipeline.
     *
     * @param chain provide access to the request and response to log.
     *
     * @return Response from the next interceptor in the pipeline.
     * @throws IOException If an IO error occurs while processing the request and response.
     */
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
            logger.info("<-- [END" + request.header(HttpHeader.CLIENT_REQUEST_ID) + "]");

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

        logger.info("--> [" + request.header(HttpHeader.CLIENT_REQUEST_ID) + "]"); // Request ID
        logger.info(request.method() + " " + url.encodedPath() + getRedactedQueryString(url)); // URL path + query
        logger.info("Host: " + url.scheme() + "://" + url.host()); // URL host

        // TODO: Add log level guard for headers and body.
        RequestBody requestBody = request.body();
        String contentType = HttpUtil.getContentType(request);
        Long contentLength = HttpUtil.getContentLength(request);

        logHeaders(request.headers(), contentType, contentLength);

        String bodySummary = getBodySummary(request.headers(), contentType, contentLength);
        if (bodySummary != null) {
            logger.debug(bodySummary);
        } else if (requestBody != null) {
            try {
                logger.debug(HttpUtil.getBodyAsString(requestBody));
            } catch (IOException e) {
                logger.warning("Could not log the request body", e);
            }
        } else {
            logger.debug("(empty body)");
        }

        logger.info("--> [END " + request.header(HttpHeader.CLIENT_REQUEST_ID) + "]");
    }

    /**
     * Logs the HTTP response.
     *
     * @param response The HTTP response received form Azure.
     * @param tookMs   Nanosecond representation of when the request was sent.
     */
    private void logResponse(final Response response, long tookMs) {
        logger.info("<-- [" + response.header(HttpHeader.CLIENT_REQUEST_ID) + "] " + "(" + tookMs + ")");

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

        String bodySummary = getBodySummary(response.headers(), contentType, contentLength);
        if (bodySummary != null) {
            logger.debug(bodySummary);
        } else if (responseBody != null) {
            try {
                logger.debug(HttpUtil.getBodyAsString(responseBody));
            } catch (IOException e) {
                logger.warning("Could not log the response body", e);
            }
        } else {
            logger.debug("(empty body)");
        }

        logger.info("<-- [END " + response.header(HttpHeader.CLIENT_REQUEST_ID) + "]");
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
            String headerName = HttpHeader.CONTENT_TYPE;
            String headerValue = contentType;
            if (!allowedHeaderNames.contains(headerName.toLowerCase(Locale.ROOT))) {
                headerValue = REDACTED_PLACEHOLDER;
            }
            logger.debug(headerName + ": " + headerValue);
        }

        if (contentLength != null) {
            String headerName = HttpHeader.CONTENT_LENGTH;
            String headerValue = contentLength.toString();
            if (!allowedHeaderNames.contains(headerName.toLowerCase(Locale.ROOT))) {
                headerValue = REDACTED_PLACEHOLDER;
            }
            logger.debug(headerName + ": " + headerValue);
        }

        String contentTypeLower = HttpHeader.CONTENT_TYPE.toLowerCase(Locale.ROOT);
        String contentLengthLower = HttpHeader.CONTENT_LENGTH.toLowerCase(Locale.ROOT);

        for (int i = 0; i < headers.size(); i++) {
            String headerName = headers.name(i);
            String headerValue = headers.value(i);

            String headerNameLower = headerName.toLowerCase(Locale.ROOT);

            // Skip headers which have already been logged.
            if (contentTypeLower.equals(headerNameLower) || contentLengthLower.equals(headerNameLower)) {
                continue;
            }

            if (!allowedHeaderNames.contains(headerNameLower)) {
                headerValue = REDACTED_PLACEHOLDER;
            }

            logger.debug(headerName + ": " + headerValue);
        }
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
    private String getBodySummary(@NonNull Headers headers, String contentType, Long contentLength) {
        String contentEncoding = headers.get(HttpHeader.CONTENT_ENCODING);
        if (!isNullOrEmpty(contentEncoding) && !contentEncoding.equalsIgnoreCase("identity")) {
            return "(encoded body omitted)";
        }

        String contentDisposition = headers.get(HttpHeader.CONTENT_DISPOSITION);
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
    private String getRedactedQueryString(@NonNull HttpUrl url) {
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
