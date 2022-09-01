// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.communication.chat.sampleapp;

import android.util.Log;

import com.azure.android.communication.chat.implementation.notifications.fcm.RegistrarClient;
import com.azure.android.core.http.HttpCallback;
import com.azure.android.core.http.HttpClient;
import com.azure.android.core.http.HttpMethod;
import com.azure.android.core.http.HttpRequest;
import com.azure.android.core.http.HttpResponse;
import com.azure.android.core.logging.ClientLogger;
import com.azure.android.core.util.CancellationToken;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Client that calls azure function to obtain user id and token
 */
public class UserTokenClient {
    private final ClientLogger logger = new ClientLogger(RegistrarClient.class);

    private final HttpClient httpClient;

    private String endPoint;

    private String userID;

    private String userToken;

    private String ACSEndpoint;

    public UserTokenClient(String endPoint) {
        httpClient = HttpClient.createDefault();
        this.endPoint = endPoint;
    }

    public String getUserID() {
        return userID;
    }

    public String getUserToken() {
        return userToken;
    }

    public String getACSEndpoint() {
        return ACSEndpoint;
    }

    public void getNewUserContext() throws Throwable {
        HttpRequest request = new HttpRequest(HttpMethod.GET, endPoint);

        CountDownLatch latch = new CountDownLatch(1);
        final Throwable[] requestError = { null };

        this.httpClient.send(request, CancellationToken.NONE, new HttpCallback() {
            @Override
            public void onSuccess(HttpResponse response) {
                int statusCode = response.getStatusCode();
                UserTokenClient.this.logger.info("Get token http response code:" + statusCode);
                if (statusCode != 200) {
                    requestError[0] = new RuntimeException("Get token request failed with http status code "
                        + statusCode
                        + ". Error message: "
                        + response.getBodyAsString()
                    );
                }

                UserData userData = parseResponse(response.getBodyAsString());
                userID = userData.getUserID();
                userToken = userData.getUserToken();
                ACSEndpoint = userData.getACSEndpoint();
                latch.countDown();
            }

            @Override
            public void onError(Throwable error) {
                requestError[0] = error;
                latch.countDown();
            }
        });

        awaitOnLatch(latch);
        if (requestError[0] != null) {
            throw logger.logThrowableAsError(requestError[0]);
        }
    }

    private boolean awaitOnLatch(CountDownLatch latch) {
        long timeoutInMin = 1;
        try {
            return latch.await(timeoutInMin, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            throw logger.logExceptionAsError(new RuntimeException("Operation didn't complete within " + timeoutInMin + " minutes"));
        }
    }

    private UserData parseResponse(final String response) {
        try {
            Log.i("Response", response);
            UserData userData = new ObjectMapper().readValue(response, UserData.class);
            return userData;
        } catch (JsonMappingException e) {
            throw logger.logExceptionAsError(new RuntimeException(e));
        } catch (JsonProcessingException e) {
            throw logger.logExceptionAsError(new RuntimeException(e));
        }
    }

    public static class UserData {
        @JsonProperty("ACSEndpoint")
        private String ACSEndpoint;

        @JsonProperty("userID")
        private String userID;

        @JsonProperty("userToken")
        private String userToken;

        public UserData() {
        }

        public String getACSEndpoint() {
            return ACSEndpoint;
        }

        public String getUserID() {
            return userID;
        }

        public String getUserToken() {
            return userToken;
        }
    }
}
