// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.http.interceptor;

import androidx.annotation.NonNull;

import com.azure.android.core.http.HttpHeader;
import com.azure.android.core.util.HttpUtil;
import com.azure.android.core.util.logging.ClientLogger;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Headers;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.azure.android.core.util.CoreUtil.replace;

/**
 * Pipeline interceptor that logs HTTP requests as cURL commands.
 */
public class CurlLoggingInterceptor implements Interceptor {
    private final ClientLogger logger;
    private boolean compressed;
    private StringBuilder curlCommand;

    public CurlLoggingInterceptor() {
        this(ClientLogger.getDefault(CurlLoggingInterceptor.class));
    }

    public CurlLoggingInterceptor(ClientLogger clientLogger) {
        logger = clientLogger;
        compressed = false;
        curlCommand = new StringBuilder("curl");
    }

    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        Request request = chain.request();
        Headers headers = request.headers();

        curlCommand.append(" -X ")
            .append(request.method());

        appendHeadersToCurlCommand(headers, curlCommand);

        if (request.body() != null) {
            appendBodyToCurlCommand(request.body(), curlCommand);
        }

        curlCommand.append(" ")
            .append(request.url());

        // TODO: Add log level guard for headers and body.
        logger.debug("╭--- cURL " + request.url());
        logger.debug(curlCommand.toString());
        logger.debug("╰--- (copy and paste the above line to a terminal)");

        return chain.proceed(chain.request());
    }

    /**
     * Adds HTTP headers to the StringBuilder that is generating the cURL command.
     *
     * @param headers     HTTP headers on the request or response.
     * @param curlCommand The StringBuilder that is generating the cURL command.
     */
    private void appendHeadersToCurlCommand(Headers headers, StringBuilder curlCommand) {
        int size = headers.size();
        for (int i = 0; i < size; i++) {
            String headerName = headers.name(i);
            String headerValue = headers.value(i);

            if (headerValue.startsWith("\"") || headerValue.endsWith("\"")) {
                // Remove quotation marks at the beginning and end of the value.
                String innerHeaderValue = headerValue.substring(1, headerValue.length() - 1);
                innerHeaderValue = innerHeaderValue.replace("\\", "\\\\");
                headerValue = "\\\"" + innerHeaderValue + "\\\"";
            } else {
                headerValue = headerValue.replace("\\", "\\\\");
            }

            curlCommand.append(" -H \"")
                .append(headerName)
                .append(": ")
                .append(headerValue)
                .append("\"");

            if (headerName.equalsIgnoreCase(HttpHeader.ACCEPT_ENCODING) &&
                !headerValue.equalsIgnoreCase("identity")) {
                compressed = true;
            }
        }
    }

    /**
     * Adds HTTP headers into the StringBuilder that is generating the cURL command.
     *
     * @param requestBody Body of the request.
     * @param curlCommand The StringBuilder that is generating the cURL command.
     */
    private void appendBodyToCurlCommand(RequestBody requestBody, StringBuilder curlCommand) {
        try {
            String bodyContent = HttpUtil.getBodyAsString(requestBody);
            Map<Character, CharSequence> toReplace = new HashMap<>();

            toReplace.put('\n', "\\n");
            toReplace.put('\'', "\\'");

            curlCommand.append(" --data $'")
                .append(replace(bodyContent, toReplace))
                .append("'");

            if (compressed) {
                curlCommand.append(" --compressed");
            }
        } catch (IOException e) {
            logger.warning("Could not log the request body", e);
        }
    }
}
