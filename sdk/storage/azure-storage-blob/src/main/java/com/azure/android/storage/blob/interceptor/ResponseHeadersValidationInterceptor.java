package com.azure.android.storage.blob.interceptor;

import androidx.annotation.NonNull;

import com.azure.android.core.http.exception.HttpResponseException;
import com.azure.android.core.util.logging.ClientLogger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

import static com.azure.android.core.util.CoreUtil.isNullOrEmpty;

/**
 * Interceptor that validates that a collection of headers have consistent values between a request and a response.
 */
public class ResponseHeadersValidationInterceptor implements Interceptor {
    private static final String CLIENT_ID_HEADER = "x-ms-client-id";
    private static final String ENCRYPTION_KEY_SHA256_HEADER = "x-ms-encryption-key-sha256";

    private final ClientLogger logger;
    private final Collection<String> headerNames = new ArrayList<>();

    /**
     * Constructor that adds two mandatory headers used by Storage and uses a default {@link ClientLogger}.
     */
    public ResponseHeadersValidationInterceptor() {
        this(ClientLogger.getDefault(ResponseHeadersValidationInterceptor.class));
    }

    /**
     * Constructor that adds two mandatory headers used by Storage and uses a provided {@link ClientLogger}.
     *
     * @param clientLogger the logger
     */
    public ResponseHeadersValidationInterceptor(ClientLogger clientLogger) {
        headerNames.add(CLIENT_ID_HEADER);
        headerNames.add(ENCRYPTION_KEY_SHA256_HEADER);
        logger = clientLogger;
    }

    /**
     * Constructor that accepts a list of header names to validate. Adds two mandatory Storage header names as well.
     * and uses a default {@link ClientLogger}.
     *
     * @param headerNames the header names
     */
    public ResponseHeadersValidationInterceptor(Collection<String> headerNames) {
        this(headerNames, ClientLogger.getDefault(ResponseHeadersValidationInterceptor.class));
    }

    /**
     * Constructor that accepts a list of header names to validate. Adds two mandatory Storage header names as well
     * and uses a provided {@link ClientLogger}.
     *
     * @param headerNames  the header names
     * @param clientLogger the logger
     */
    public ResponseHeadersValidationInterceptor(Collection<String> headerNames, ClientLogger clientLogger) {
        headerNames.add(CLIENT_ID_HEADER);
        headerNames.add(ENCRYPTION_KEY_SHA256_HEADER);
        this.headerNames.addAll(headerNames);
        logger = clientLogger;
    }

    /**
     * Intercept and validate that a collection of headers have consistent values between a request and a response.
     *
     * @param chain provide access to the response to be validated.
     *
     * @return response from the next interceptor in the pipeline
     * @throws IOException if an IO error occurs while processing the request and response
     */
    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        Request request = chain.request();
        Response response = chain.proceed(request);

        for (String headerName : headerNames) {
            String responseHeaderValue = response.header(headerName);
            String requestHeaderValue = request.header(headerName);

            if (isNullOrEmpty(responseHeaderValue) || !responseHeaderValue.equals(requestHeaderValue)) {
                String errorMessage = String.format(
                    "Unexpected header value. Expected response to echo '%s: %s'. Got value '%s'.",
                    headerName, requestHeaderValue, responseHeaderValue);

                logger.error(errorMessage);

                throw new HttpResponseException(errorMessage, response);
            }
        }

        return response;
    }
}
