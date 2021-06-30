// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.communication.chat.implementation.pushnotification;

import com.azure.android.core.http.HttpCallback;
import com.azure.android.core.http.HttpClient;
import com.azure.android.core.http.HttpMethod;
import com.azure.android.core.http.HttpRequest;
import com.azure.android.core.http.HttpResponse;
import com.azure.android.core.http.okhttp.OkHttpAsyncHttpClientBuilder;
import com.azure.android.core.logging.ClientLogger;
import com.azure.android.core.serde.jackson.JacksonSerder;
import com.azure.android.core.serde.jackson.SerdeEncoding;
import com.azure.android.core.util.CancellationToken;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * The registrar client interface
 */
public class RegistrarClient {
    private static final String REGISTRAR_SERVICE_URL = "https://edge.skype.net/registrar/testenv/v2/registrations";
    private static final String SKYPE_TOKEN_HEADER = "X-Skypetoken";
    private static final String USER_AGENT_HEADER = "User-Agent";
    private static final String CONTENT_TYPE_HEADER = "Content-Type";

    private static final String CONTENT_TYPE_JSON = "application/json";
    private static final String PLATFORM = "Android";
    private static final String APP_ID = "AcsAndroid";
    private static final String TEMPLATE_KEY = "AcsAndroid.AcsNotify_3.0";
    private static final String PLATFORM_UI_VERSION = "0.0.0.0";
    private static final String NODE_ID = "";
    private static final String TTL = "90000";

    private final ClientLogger logger = new ClientLogger(RegistrarClient.class);
    private final HttpClient httpClient;
    private final JacksonSerder jacksonSerder;

    private String registrationId;
    private boolean requestResult;

    private class ClientDescription {
        @JsonProperty(value = "languageId")
        String languageId;

        @JsonProperty(value = "platform")
        String platform;

        @JsonProperty(value = "platformUIVersion")
        String platformUIVersion;

        @JsonProperty(value = "appId")
        String applicationId;

        @JsonProperty(value = "templateKey")
        String templateKey;
    }

    private class FcmTransport {
        @JsonProperty(value = "context")
        String context;

        @JsonProperty(value = "creationTime")
        String creationTime;

        @JsonProperty(value = "path")
        String path;

        @JsonProperty(value = "ttl")
        String ttl;
    }

    private class Transports {
        @JsonProperty(value = "FCM")
        List<FcmTransport> fcm;
    }

    private class RegistrarRequestBody {
        @JsonProperty(value = "clientDescription")
        ClientDescription clientDescription;

        @JsonProperty(value = "nodeId")
        String nodeId;

        @JsonProperty(value = "registrationId")
        String registrationId;

        @JsonProperty(value = "transports")
        Transports transports;
    }

    RegistrarClient() {
        this.httpClient = new OkHttpAsyncHttpClientBuilder().build();
        this.jacksonSerder = JacksonSerder.createDefault();
    }

    public boolean Register(String skypeUserToken, String deviceRegistrationToken) {
        HttpRequest request = new HttpRequest(HttpMethod.POST, REGISTRAR_SERVICE_URL);
        request
            .setHeader(USER_AGENT_HEADER, PLATFORM_UI_VERSION)
            .setHeader(CONTENT_TYPE_HEADER, CONTENT_TYPE_JSON)
            .setHeader(SKYPE_TOKEN_HEADER, skypeUserToken);

        AddRequestBody(request, deviceRegistrationToken);

        CountDownLatch latch = new CountDownLatch(1);

        httpClient.send(request, CancellationToken.NONE, new HttpCallback() {
            @Override
            public void onSuccess(HttpResponse response) {
                latch.countDown();
                requestResult = true;
            }

            @Override
            public void onError(Throwable error) {
                latch.countDown();
                requestResult = false;
            }
        });

        awaitOnLatch(latch);
        logger.info("Registrar request result:" + requestResult);
        logger.info("registrationId:" + registrationId);
        return requestResult;
    }

    public boolean Unregister(String skypeUserToken) {
        if (registrationId == null) {
            return false;
        }

        String unregisterUrl = REGISTRAR_SERVICE_URL + "/" + registrationId;
        HttpRequest request = new HttpRequest(HttpMethod.DELETE, unregisterUrl);
        request
            .setHeader(USER_AGENT_HEADER, PLATFORM_UI_VERSION)
            .setHeader(CONTENT_TYPE_HEADER, CONTENT_TYPE_JSON)
            .setHeader(SKYPE_TOKEN_HEADER, skypeUserToken);

        CountDownLatch latch = new CountDownLatch(1);

        httpClient.send(request, CancellationToken.NONE, new HttpCallback() {
            @Override
            public void onSuccess(HttpResponse response) {
                latch.countDown();
                requestResult = true;
            }

            @Override
            public void onError(Throwable error) {
                latch.countDown();
                requestResult = false;
            }
        });

        awaitOnLatch(latch);
        logger.info("Registrar unregistered request result:" + requestResult);
        logger.info("registrationId:" + registrationId);
        return requestResult;
    }

    private void AddRequestBody(HttpRequest request, String deviceRegistrationToken) {
        this.registrationId = "d9013b1b-28bc-4def-8ecd-38e234182e25"; // UUID.randomUUID().toString();

        ClientDescription clientDescription = new ClientDescription();
        clientDescription.languageId = "en-US";
        clientDescription.platform = PLATFORM;
        clientDescription.platformUIVersion = PLATFORM_UI_VERSION;
        clientDescription.applicationId = APP_ID;
        clientDescription.templateKey = TEMPLATE_KEY;

        Transports transports = new Transports();
        transports.fcm = new ArrayList<>();

        FcmTransport transport = new FcmTransport();
        transport.creationTime = "";
        transport.context = "";
        transport.path = deviceRegistrationToken;
        transport.ttl = TTL;
        transports.fcm.add(transport);

        RegistrarRequestBody registrarRequestBody = new RegistrarRequestBody();
        registrarRequestBody.clientDescription = clientDescription;
        registrarRequestBody.nodeId = NODE_ID;
        registrarRequestBody.registrationId = this.registrationId;
        registrarRequestBody.transports = transports;

        try {
            String body = this.jacksonSerder.serialize(registrarRequestBody, SerdeEncoding.JSON);
            request.setBody(body);
        } catch (IOException e) {
            throw logger.logExceptionAsError(new RuntimeException(e));
        }
    }

    private void awaitOnLatch(CountDownLatch latch) {
        long timeoutInSec = 2;
        try {
            latch.await(timeoutInSec, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            throw logger.logExceptionAsError(new RuntimeException("Operation didn't complete within " + timeoutInSec + " minutes"));
        }
    }
}
