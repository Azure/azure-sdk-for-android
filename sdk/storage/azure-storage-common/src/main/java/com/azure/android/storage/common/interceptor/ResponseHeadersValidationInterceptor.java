package com.azure.android.storage.common.interceptor;

import com.azure.android.core.util.logging.ClientLogger;
import com.azure.android.core.util.CoreUtils;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Interceptor that validates that a collection of headers have consistent values between a request and a response.
 */
public class ResponseHeadersValidationInterceptor implements Interceptor {
    private static final String CLIENT_ID_HEADER = "x-ms-client-id";
    private static final String ENCRYPTION_KEY_SHA256_HEADER = "x-ms-encryption-key-sha256";

    private final ClientLogger logger = new ClientLogger(ResponseHeadersValidationInterceptor.class);
    private final Collection<String> headerNames = new ArrayList<>();

    /**
     * Constructor that adds two mandatory headers used by Azure Storage.
     */
    public ResponseHeadersValidationInterceptor() {
        headerNames.add(CLIENT_ID_HEADER);
        headerNames.add(ENCRYPTION_KEY_SHA256_HEADER);
    }

    /**
     * Constructor that accepts a list of header names to validate. Adds two mandatory Azure Storage header names as
     * well.
     */
    public ResponseHeadersValidationInterceptor(Collection<String> headerNames) {
        this();
        this.headerNames.addAll(headerNames);
    }

    @NotNull
    @Override
    public Response intercept(@NotNull Chain chain) throws IOException {
        Request request = chain.request();
        Response response = chain.proceed(request);

        for (String headerName : headerNames) {
            String responseHeaderValue = response.header(headerName);
            String requestHeaderValue = request.header(headerName);

            if (CoreUtils.isNullOrEmpty(responseHeaderValue) || !responseHeaderValue.equals(requestHeaderValue)) {
                throw logger.logExceptionAsError(new RuntimeException(String.format(
                    "Unexpected header value. Expected response to echo '%s: %s'. Got value '%s'.",
                    headerName, requestHeaderValue, responseHeaderValue
                )));
            }
        }

        return response;
    }
}
