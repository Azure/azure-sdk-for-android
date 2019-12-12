// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.http.interceptor;

import com.azure.android.core.util.CoreUtils;
import com.azure.android.core.util.logging.ClientLogger;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import kotlin.Pair;
import okhttp3.Headers;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.Buffer;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Pipeline interceptor that handles logging of HTTP requests as cURL commands.
 */
public class CurlLoggingInterceptor implements Interceptor {
    private final ClientLogger logger;
    private StringBuilder curlCommand;
    private boolean compressed;

    public CurlLoggingInterceptor() {
        logger = new ClientLogger(CurlLoggingInterceptor.class);
        curlCommand = new StringBuilder("curl");
        compressed = false;
    }

    @NotNull
    @Override
    public Response intercept(@NotNull Chain chain) throws IOException {
        Request request = chain.request();
        Headers headers = request.headers();

        curlCommand.append(" -X ")
            .append(request.method());

        addHeadersToCurlCommand(headers, curlCommand);

        String contentTypeString = request.header("Content-Type");
        long contentLength = LogUtils.getContentLength(logger, request.headers());
        RequestBody requestBody = request.body();

        if (LogUtils.shouldLogBody(contentTypeString, contentLength) && requestBody != null) {
            addBodyToCurlCommand(requestBody, curlCommand);
        }

        curlCommand.append(" \"")
            .append(request.url())
            .append("\"");

        logger.info("╭--- cURL " + request.url());
        logger.info(curlCommand.toString());
        logger.info("╰--- (copy and paste the above line to a terminal)");

        return chain.proceed(chain.request());
    }

    /**
     * Adds HTTP headers into the StringBuilder that is generating the cURL command.
     *
     * @param headers     HTTP headers on the request or response.
     * @param curlCommand StringBuilder that is generating the cURL command.
     */
    private void addHeadersToCurlCommand(Headers headers, StringBuilder curlCommand) {
        for (Pair<? extends String, ? extends String> header : headers) {
            String headerName = header.getFirst();
            String headerValue = header.getSecond();

            if (headerValue.startsWith("\"") || headerValue.endsWith("\"")) {
                headerValue = "\\\"" + headerValue.replaceAll("\"", "") + "\\\"";
            }

            curlCommand.append(" -H \"")
                .append(headerName)
                .append(": ")
                .append(headerValue)
                .append("\"");

            if (headerValue.equalsIgnoreCase("gzip")) {
                compressed = true;
            }
        }
    }

    /**
     * Adds HTTP headers into the StringBuilder that is generating the cURL command.
     *
     * @param requestBody Body on the request.
     * @param curlCommand StringBuilder that is generating the cURL command.
     */
    private void addBodyToCurlCommand(RequestBody requestBody, StringBuilder curlCommand) {
        String requestBodyString = "(body content not logged)";

        try {
            Buffer buffer = new Buffer();
            MediaType contentType = requestBody.contentType();
            Charset charset = (contentType == null) ? UTF_8 : contentType.charset(UTF_8);
            requestBody.writeTo(buffer);

            if (charset != null) {
                requestBodyString = buffer.readString(charset);
            } else {
                logger.warning("Could not log the response body. No encoding charset found.");
            }
        } catch (IOException e) {
            logger.warning("Could not log the request body", e);
        }

        Map<Character, CharSequence> toReplace = new HashMap<>();
        toReplace.put('\n', "\\n");
        toReplace.put('\"', "\\\"");

        curlCommand.append(" --data '")
            .append(CoreUtils.replace(requestBodyString, toReplace))
            .append("'");

        if (compressed) {
            curlCommand.append(" --compressed");
        }
    }
}
