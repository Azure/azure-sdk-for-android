package com.azure.android.communication.chat.implementation.notifications.signaling;

import com.azure.android.core.http.HttpCallback;
import com.azure.android.core.http.HttpClient;
import com.azure.android.core.http.HttpMethod;
import com.azure.android.core.http.HttpRequest;
import com.azure.android.core.http.HttpResponse;
import com.azure.android.core.logging.ClientLogger;
import com.azure.android.core.serde.jackson.JacksonSerder;
import com.azure.android.core.util.CancellationToken;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class RealtimeNotificationConfigClient {
    private static final String TAG = "RealtimeNotificationConfigClient";

    private final HttpClient httpClient;

    private final ClientLogger logger = new ClientLogger(RealtimeNotificationConfigClient.class);

    private static final String AUTHORIZATION_HEADER = "Authorization";

    RealtimeNotificationConfigClient() {
        this.httpClient = HttpClient.createDefault();
    }

    /**
     * Fetches the RealTimeNotificationConfiguration from the server.
     *
     * @param token             The Bearer token for authorization.
     * @param endpoint          The base endpoint URL.
     * @param configApiVersion  The API version parameter.
     */
    public RealTimeNotificationConfig getTrouterSettings(String token, String endpoint, String configApiVersion) {
        /// Construct the URL
        String urlString = endpoint + "/chat/config/realTimeNotifications?api-version=" + configApiVersion;

        // Build the HttpRequest
        HttpRequest request = new HttpRequest(HttpMethod.GET, urlString);
        request
            .setHeader(AUTHORIZATION_HEADER, "Bearer " + token)
            .setHeader("Accept", "application/json");

        // Initialize CountDownLatch and error holder
        CountDownLatch latch = new CountDownLatch(1);
        final Throwable[] requestError = { null };
        final RealTimeNotificationConfig[] configResult = {null};

        // Send the request asynchronously
        this.httpClient.send(request, CancellationToken.NONE, new HttpCallback() {
            @Override
            public void onSuccess(HttpResponse response) {
                int statusCode = response.getStatusCode();
                logger.info("Retrieve realtime notification config http response code:" + statusCode);
                if (statusCode != 200) {
                    requestError[0] = new RuntimeException("Registrar register request failed with http status code "
                        + statusCode
                        + ". Error message: "
                        + response.getBodyAsString()
                    );
                }

                // Convert the response content to RealTimeNotificationConfig
                try {
                    ObjectMapper objectMapper = new ObjectMapper();
                    configResult[0] = objectMapper.readValue(response.getBodyAsString(), RealTimeNotificationConfig.class);
                    logger.info("Successfully converted response to RealTimeNotificationConfig.");
                } catch (Exception e) {
                    logger.error("Failed to parse response body to RealTimeNotificationConfig: " + e.getMessage(), e);
                    requestError[0] = new RuntimeException("Failed to parse response body", e);
                }

                latch.countDown();
            }

            @Override
            public void onError(Throwable error) {
                requestError[0] = error;
                latch.countDown();
            }
        });

        awaitOnLatch(latch);

        // Check for errors and throw an exception if necessary
        if (requestError[0] != null) {
            throw new RuntimeException(requestError[0]);
        }

        // Return the result
        return configResult[0];
    }

    private boolean awaitOnLatch(CountDownLatch latch) {
        long timeoutInMin = 1;
        try {
            return latch.await(timeoutInMin, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            throw logger.logExceptionAsError(new RuntimeException("Operation didn't complete within " + timeoutInMin + " minutes"));
        }
    }

    /**
     * Converts an InputStream to a String.
     *
     * @param is InputStream to convert.
     * @return String representation of the InputStream.
     * @throws IOException If an I/O error occurs.
     */
    private static String convertStreamToString(InputStream is) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line;
        while ( (line = reader.readLine()) != null ) {
            sb.append(line);
        }
        reader.close();
        return sb.toString();
    }
}
