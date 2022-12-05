// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.http.policy;

import com.azure.android.core.http.HttpHeader;
import com.azure.android.core.http.HttpHeaders;
import com.azure.android.core.http.HttpPipelinePolicy;
import com.azure.android.core.http.HttpPipelinePolicyChain;
import com.azure.android.core.http.HttpRequest;
import com.azure.android.core.http.HttpResponse;
import com.azure.android.core.http.NextPolicyCallback;
import com.azure.android.core.http.PolicyCompleter;
import com.azure.android.core.http.util.UrlBuilder;
import com.azure.android.core.util.RequestContext;
import com.azure.android.core.logging.ClientLogger;
import com.azure.android.core.logging.LogLevel;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * The pipeline policy that handles logging of HTTP requests and responses.
 */
public class HttpLoggingPolicy implements HttpPipelinePolicy {
    private static final int MAX_BODY_LOG_SIZE = 1024 * 16;
    private static final String REDACTED_PLACEHOLDER = "REDACTED";
    private static final String LINE_SEPARATOR;

    private final HttpLogDetailLevel httpLogDetailLevel;
    private final Set<String> allowedHeaderNames;
    private final Set<String> allowedQueryParameterNames;

    static {
        final String lineSeparator = System.getProperty("line.separator");
        if (lineSeparator ==  null || lineSeparator.length() == 0) {
            LINE_SEPARATOR = lineSeparator;
        } else {
            LINE_SEPARATOR = "\n";
        }
    }

    /**
     * Key for {@link RequestContext} to pass request retry count metadata for logging.
     */
    public static final String RETRY_COUNT_CONTEXT = "requestRetryCount";

    /**
     * Creates an HttpLoggingPolicy with the given log configurations.
     *
     * @param httpLogOptions The HTTP logging configuration options.
     */
    public HttpLoggingPolicy(HttpLogOptions httpLogOptions) {
        if (httpLogOptions == null) {
            this.httpLogDetailLevel = HttpLogDetailLevel.NONE;
            this.allowedHeaderNames = Collections.emptySet();
            this.allowedQueryParameterNames = Collections.emptySet();
        } else {
            this.httpLogDetailLevel = httpLogOptions.getLogLevel();
            this.allowedHeaderNames = new HashSet<>(httpLogOptions.getAllowedHeaderNames().size());
            final Iterator<String> headerItr = httpLogOptions.getAllowedHeaderNames().iterator();
            while (headerItr.hasNext()) {
                this.allowedHeaderNames.add(headerItr.next().toLowerCase(Locale.ROOT));
            }
            this.allowedQueryParameterNames = new HashSet<>(httpLogOptions.getAllowedQueryParamNames().size());
            final Iterator<String> queryItr = httpLogOptions.getAllowedQueryParamNames().iterator();
            while (queryItr.hasNext()) {
                this.allowedQueryParameterNames.add(queryItr.next().toLowerCase(Locale.ROOT));
            }
            // this.prettyPrintBody = httpLogOptions.isPrettyPrintBody();
        }
    }

    @Override
    public void process(HttpPipelinePolicyChain chain) {
        // No logging will be performed, trigger a no-op.
        if (httpLogDetailLevel == HttpLogDetailLevel.NONE) {
            chain.processNextPolicy(chain.getRequest());
            return;
        }

        // final ClientLogger logger = new ClientLogger((String) context.getData("caller-method").orElse(""));
        final ClientLogger logger = new ClientLogger((String) "caller-method"); // TODO: bring context ^
        if (!logger.canLogAtLevel(LogLevel.INFORMATIONAL)) {
            chain.processNextPolicy(chain.getRequest());
            return;
        }

        final long startNs = System.nanoTime();
        final HttpRequest httpRequest = chain.getRequest();
        // final Integer retryCount = context.getData(RETRY_COUNT_CONTEXT);
        // final Integer retryCount = null; // TODO: bring context ^

        StringBuilder requestLogMessage = new StringBuilder();
        if (httpLogDetailLevel.shouldLogUrl()) {
            requestLogMessage.append("--> ")
                .append(httpRequest.getHttpMethod())
                .append(" ")
                .append(this.getRedactedUrl(httpRequest.getUrl()))
                .append(LINE_SEPARATOR);

            // if (retryCount != null) {
            //     requestLogMessage.append(retryCount).append(LINE_SEPARATOR);
            // }
        }

        this.appendHeaders(logger, httpRequest.getHeaders(), requestLogMessage);

        if (httpLogDetailLevel.shouldLogBody()) {
            if (httpRequest.getBody() == null) {
                requestLogMessage.append("(empty body)")
                    .append(LINE_SEPARATOR)
                    .append("--> END ")
                    .append(httpRequest.getHttpMethod())
                    .append(LINE_SEPARATOR);
            } else {
                final String requestContentType = httpRequest.getHeaders().getValue("Content-Type");
                final long requestContentLength = this.getContentLength(logger, httpRequest.getHeaders());
                if (this.isContentLoggable(requestContentType, requestContentLength)) {
                    final String content = this.convertBytesToString(httpRequest.getBody(), logger);
                    requestLogMessage.append(requestContentLength)
                        .append("-byte body:")
                        .append(LINE_SEPARATOR)
                        .append(content)
                        .append(LINE_SEPARATOR)
                        .append("--> END ")
                        .append(httpRequest.getHttpMethod())
                        .append(LINE_SEPARATOR);
                } else {
                    requestLogMessage.append(requestContentLength)
                        .append("-byte body: (content not logged)")
                        .append(LINE_SEPARATOR)
                        .append("--> END ")
                        .append(httpRequest.getHttpMethod())
                        .append(LINE_SEPARATOR);
                }
            }
        }

        logger.info(requestLogMessage.toString());

        chain.processNextPolicy(httpRequest, new NextPolicyCallback() {
            @Override
            public PolicyCompleter.CompletionState onSuccess(HttpResponse response, PolicyCompleter completer) {
                long tookMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNs);

                final String contentLengthHeaderValue = response.getHeaderValue("Content-Length");
                final String contentLengthMessage
                    = (contentLengthHeaderValue == null || contentLengthHeaderValue.length() == 0)
                    ? "unknown-length body"
                    : contentLengthHeaderValue + "-byte body";

                StringBuilder responseLogMessage = new StringBuilder();
                if (httpLogDetailLevel.shouldLogUrl()) {
                    responseLogMessage.append("<-- ")
                        .append(response.getStatusCode())
                        .append(" ")
                        .append(getRedactedUrl(response.getRequest().getUrl()))
                        .append(" (")
                        .append(tookMs)
                        .append(" ms, ")
                        .append(contentLengthMessage)
                        .append(")")
                        .append(LINE_SEPARATOR);
                }

                appendHeaders(logger, response.getHeaders(), responseLogMessage);

                HttpResponse httpResponse = response;
                if (httpLogDetailLevel.shouldLogBody()) {
                    final String responseContentType = response.getHeaderValue("Content-Type");
                    final long responseContentLength = getContentLength(logger, response.getHeaders());
                    if (isContentLoggable(responseContentType, responseContentLength)) {
                        httpResponse = response.buffer();
                        final String content = convertBytesToString(httpResponse.getBodyAsByteArray(), logger);
                        responseLogMessage.append("Response body:")
                            .append(LINE_SEPARATOR)
                            .append(content)
                            .append(LINE_SEPARATOR)
                            .append("<-- END HTTP");
                    } else {
                        responseLogMessage.append("(body content not logged)")
                            .append(LINE_SEPARATOR)
                            .append("<-- END HTTP");
                    }
                } else {
                    responseLogMessage.append("<-- END HTTP");
                }
                logger.info(responseLogMessage.toString());
                return completer.completed(httpResponse);
            }

            @Override
            public PolicyCompleter.CompletionState onError(Throwable error, PolicyCompleter completer) {
                logger.warning("<-- HTTP FAILED: ", error);
                return completer.completedError(error);
            }
        });
    }

    /*
     * Generates the redacted URL for logging.
     *
     * @param url URL where the request is being sent.
     * @return A URL with query parameters redacted based on configurations in this policy.
     */
    private String getRedactedUrl(URL url) {
        return UrlBuilder.parse(url)
            .setQuery(this.getAllowedQueryString(url.getQuery()))
            .toString();
    }

    /*
     * Generates the logging safe query parameters string.
     *
     * @param queryString Query parameter string from the request URL.
     * @return A query parameter string redacted based on the configurations in this policy.
     */
    private String getAllowedQueryString(String queryString) {
        if (queryString == null || queryString.length() == 0) {
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

    /*
     * Adds HTTP headers into the StringBuilder that is generating the log message.
     *
     * @param headers HTTP headers on the request or response.
     * @param sb StringBuilder that is generating the log message.
     * @param logLevel Log level the environment is configured to use.
     */
    private void appendHeaders(ClientLogger logger, HttpHeaders headers, StringBuilder logMessage) {
        // Either headers shouldn't be logged or the logging level isn't set to VERBOSE, don't add headers.
        if (!httpLogDetailLevel.shouldLogHeaders() || !logger.canLogAtLevel(LogLevel.VERBOSE)) {
            return;
        }

        for (HttpHeader header : headers) {
            String headerName = header.getName();
            logMessage.append(headerName).append(":");
            if (allowedHeaderNames.contains(headerName.toLowerCase(Locale.ROOT))) {
                logMessage.append(header.getValue());
            } else {
                logMessage.append(REDACTED_PLACEHOLDER);
            }
            logMessage.append(LINE_SEPARATOR);
        }
    }

    /*
     * Attempts to retrieve and parse the Content-Length header into a numeric representation.
     *
     * @param logger Logger used to log a warning if the Content-Length header is an invalid number.
     * @param headers HTTP headers that are checked for containing Content-Length.
     * @return The retrieved content length.
     */
    private long getContentLength(ClientLogger logger, HttpHeaders headers) {
        long contentLength = 0;

        String contentLengthString = headers.getValue("Content-Length");
        if (contentLengthString == null || contentLengthString.length() == 0) {
            return contentLength;
        }

        try {
            contentLength = Long.parseLong(contentLengthString);
        } catch (NumberFormatException | NullPointerException e) {
            logger.warning("Could not parse the HTTP header content-length: '{}'.",
                headers.getValue("content-length"), e);
        }

        return contentLength;
    }

    /*
     * Determines if the request or response body should be logged.
     *
     * <p>The request or response body is logged if the Content-Type is not "application/octet-stream" and the body
     * isn't empty and is less than 16KB in size.</p>
     *
     * @param contentTypeHeader Content-Type header value.
     * @param contentLength Content-Length header represented as a numeric.
     * @return A flag indicating if the request or response body should be logged.
     */
    private boolean isContentLoggable(String contentTypeHeader, long contentLength) {
        return !"application/octet-stream".equalsIgnoreCase(contentTypeHeader)
            && contentLength != 0
            && contentLength < MAX_BODY_LOG_SIZE;
    }

    /*
     * Converts a byte array to a String.
     *
     * @param bytes The bytes to convert.
     * @param logger The logger to log if conversion fails.
     * @return The byte array as string.
     *
     */
    private String convertBytesToString(byte[] bytes, ClientLogger logger) {
        try {
            return new String(bytes, "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            throw logger.logExceptionAsError(new RuntimeException(ex));
        }
    }
}