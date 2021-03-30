// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.test.policy;

import com.azure.android.core.http.HttpHeader;
import com.azure.android.core.http.HttpPipelinePolicy;
import com.azure.android.core.http.HttpPipelinePolicyChain;
import com.azure.android.core.http.HttpResponse;
import com.azure.android.core.http.NextPolicyCallback;
import com.azure.android.core.http.PolicyCompleter;
import com.azure.android.core.logging.ClientLogger;
import com.azure.android.core.test.models.RecordedData;
import com.azure.android.core.http.HttpHeaders;
import com.azure.android.core.http.util.UrlBuilder;
import com.azure.android.core.test.models.NetworkCallError;
import com.azure.android.core.test.models.NetworkCallRecord;
import com.azure.android.core.test.models.RecordingRedactor;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.zip.GZIPInputStream;

/**
 * HTTP Pipeline policy that keeps track of each HTTP request and response that flows through the pipeline. Data is
 * recorded into {@link RecordedData}.
 */
public class RecordNetworkCallPolicy implements HttpPipelinePolicy {
    private static final int DEFAULT_BUFFER_LENGTH = 1024;
    private static final String CONTENT_TYPE = "Content-Type";
    private static final String CONTENT_ENCODING = "Content-Encoding";
    private static final String CONTENT_LENGTH = "Content-Length";
    private static final String X_MS_CLIENT_REQUEST_ID = "x-ms-client-request-id";
    private static final String X_MS_ENCRYPTION_KEY_SHA256 = "x-ms-encryption-key-sha256";
    private static final String X_MS_VERSION = "x-ms-version";
    private static final String USER_AGENT = "User-Agent";
    private static final String STATUS_CODE = "StatusCode";
    private static final String BODY = "Body";
    private static final String SIG = "sig";

    private final ClientLogger logger = new ClientLogger(RecordNetworkCallPolicy.class);
    private final RecordedData recordedData;

    /**
     * Creates a policy that records network calls into {@code recordedData}.
     *
     * @param recordedData The record to persist network calls into.
     */
    public RecordNetworkCallPolicy(RecordedData recordedData) {
        Objects.requireNonNull(recordedData, "'recordedData' cannot be null.");
        this.recordedData = recordedData;
    }

    @Override
    public void process(HttpPipelinePolicyChain chain) {
        final NetworkCallRecord networkCallRecord = new NetworkCallRecord();
        Map<String, String> headers = new HashMap<>();

        captureRequestHeaders(chain.getRequest().getHeaders(), headers,
            X_MS_CLIENT_REQUEST_ID,
            CONTENT_TYPE,
            X_MS_VERSION,
            USER_AGENT);

        networkCallRecord.setHeaders(headers);
        networkCallRecord.setMethod(chain.getRequest().getHttpMethod().toString());

        // Remove sensitive information such as SAS token signatures from the recording.
        UrlBuilder urlBuilder = UrlBuilder.parse(chain.getRequest().getUrl());
        redactedAccountName(urlBuilder);
        if (urlBuilder.getQuery().containsKey(SIG)) {
            urlBuilder.setQueryParameter(SIG, "REDACTED");
        }
        networkCallRecord.setUri(urlBuilder.toString().replaceAll("\\?$", ""));

        chain.processNextPolicy(chain.getRequest(), new NextPolicyCallback() {
            @Override
            public PolicyCompleter.CompletionState onSuccess(HttpResponse response, PolicyCompleter completer) {
                final HttpResponse bufferedResponse = response.buffer();
                Map<String, String> responseData = extractResponseData(bufferedResponse);
                networkCallRecord.setResponse(responseData);
                String body = responseData.get(BODY);

                // Remove pre-added header if this is a waiting or redirection
                if (body != null && body.contains("<Status>InProgress</Status>")
                    || Integer.parseInt(responseData.get(STATUS_CODE)) == HttpURLConnection.HTTP_MOVED_TEMP) {
                    logger.info("Waiting for a response or redirection.");
                } else {
                    recordedData.addNetworkCall(networkCallRecord);
                }

                return completer.completed(bufferedResponse);
            }

            @Override
            public PolicyCompleter.CompletionState onError(Throwable error, PolicyCompleter completer) {
                networkCallRecord.setException(new NetworkCallError(error));
                recordedData.addNetworkCall(networkCallRecord);
                return completer.completedError(error);
            }
        });
    }

    private void captureRequestHeaders(HttpHeaders requestHeaders, Map<String, String> captureHeaders,
                                       String... headerNames) {
        for (String headerName : headerNames) {
            if (requestHeaders.getValue(headerName) != null) {
                captureHeaders.put(headerName, requestHeaders.getValue(headerName));
            }
        }
    }

    private void redactedAccountName(UrlBuilder urlBuilder) {
        String[] hostParts = urlBuilder.getHost().split("\\.");
        hostParts[0] = "REDACTED";

        urlBuilder.setHost(String.join(".", hostParts));
    }

    private Map<String, String> extractResponseData(final HttpResponse response) {
        final Map<String, String> responseData = new HashMap<>();
        responseData.put(STATUS_CODE, Integer.toString(response.getStatusCode()));

        boolean addedRetryAfter = false;
        for (HttpHeader header : response.getHeaders()) {
            String headerValueToStore = header.getValue();

            if (header.getName().equalsIgnoreCase("retry-after")) {
                headerValueToStore = "0";
                addedRetryAfter = true;
            } else if (header.getName().equalsIgnoreCase(X_MS_ENCRYPTION_KEY_SHA256)) {
                // The encryption key is sensitive information so capture it with a hidden value.
                headerValueToStore = "REDACTED";
            }

            responseData.put(header.getName(), headerValueToStore);
        }

        if (!addedRetryAfter) {
            responseData.put("retry-after", "0");
        }

        String contentType = response.getHeaderValue(CONTENT_TYPE);
        if (contentType == null) {
            final byte[] bytes = response.getBodyAsByteArray();
            if (bytes != null && bytes.length != 0) {
                String content = new String(bytes, StandardCharsets.UTF_8);
                responseData.put(CONTENT_LENGTH, Integer.toString(content.length()));
                responseData.put(BODY, content);
            }
            return responseData;
        } else if (contentType.equalsIgnoreCase("application/octet-stream")
            || contentType.equalsIgnoreCase("avro/binary")) {
            final byte[] bytes = response.getBodyAsByteArray();
            if (bytes != null && bytes.length != 0) {
                responseData.put(BODY, Base64.getEncoder().encodeToString(bytes));
            }
            return responseData;
        } else if (contentType.contains("json") || response.getHeaderValue(CONTENT_ENCODING) == null) {
            String content = response.getBodyAsString(StandardCharsets.UTF_8);
            if (content == null || content.length() == 0) {
                content = "";
            }
            responseData.put(BODY, new RecordingRedactor().redact(content));
            return responseData;
        } else {
            final byte[] bytes = response.getBodyAsByteArray();
            if (bytes == null || bytes.length == 0) {
                return responseData;
            }

            String content;
            if ("gzip".equalsIgnoreCase(response.getHeaderValue(CONTENT_ENCODING))) {
                try (GZIPInputStream gis = new GZIPInputStream(new ByteArrayInputStream(bytes));
                     ByteArrayOutputStream output = new ByteArrayOutputStream()) {
                    byte[] buffer = new byte[DEFAULT_BUFFER_LENGTH];
                    int position = 0;
                    int bytesRead = gis.read(buffer, position, buffer.length);

                    while (bytesRead != -1) {
                        output.write(buffer, 0, bytesRead);
                        position += bytesRead;
                        bytesRead = gis.read(buffer, position, buffer.length);
                    }

                    content = output.toString("UTF-8");
                } catch (IOException e) {
                    throw logger.logExceptionAsWarning(new RuntimeException(e));
                }
            } else {
                content = new String(bytes, StandardCharsets.UTF_8);
            }

            responseData.remove(CONTENT_ENCODING);
            responseData.put(CONTENT_LENGTH, Integer.toString(content.length()));

            responseData.put(BODY, content);
            return responseData;
        }
    }
}
