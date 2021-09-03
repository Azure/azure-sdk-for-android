// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.communication.chat.implementation.notifications.fcm;

import com.azure.android.core.http.HttpCallback;
import com.azure.android.core.http.HttpClient;
import com.azure.android.core.http.HttpMethod;
import com.azure.android.core.http.HttpRequest;
import com.azure.android.core.http.HttpResponse;
import com.azure.android.core.logging.ClientLogger;
import com.azure.android.core.serde.jackson.JacksonSerder;
import com.azure.android.core.serde.jackson.SerdeEncoding;
import com.azure.android.core.util.CancellationToken;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static com.azure.android.communication.chat.BuildConfig.PLATFORM;
import static com.azure.android.communication.chat.BuildConfig.PLATFORM_UI_VERSION;
import static com.azure.android.communication.chat.BuildConfig.PUSHNOTIFICATION_REGISTRAR_SERVICE_URL;
import static com.azure.android.communication.chat.BuildConfig.PUSHNOTIFICATION_REGISTRAR_SERVICE_TTL;
import static com.azure.android.communication.chat.BuildConfig.PUSHNOTIFICATION_APPLICATION_ID;
import static com.azure.android.communication.chat.BuildConfig.PUSHNOTIFICATION_TEMPLATE_KEY;

/**
 * The registrar client interface
 */
public class RegistrarClient {
    private static final String SKYPE_TOKEN_HEADER = "X-Skypetoken";
    private static final String USER_AGENT_HEADER = "User-Agent";
    private static final String CONTENT_TYPE_HEADER = "Content-Type";

    private static final String CONTENT_TYPE_JSON = "application/json";
    private static final String NODE_ID = "";

    private final ClientLogger logger = new ClientLogger(RegistrarClient.class);
    private final HttpClient httpClient;
    private final JacksonSerder jacksonSerder;

    private String registrationId;
    private boolean requestResult;

    private static class ClientDescription {
        @JsonProperty(value = "languageId")
        private String languageId;

        @JsonProperty(value = "platform")
        private String platform;

        @JsonProperty(value = "platformUIVersion")
        private String platformUIVersion;

        @JsonProperty(value = "appId")
        private String applicationId;

        @JsonProperty(value = "templateKey")
        private String templateKey;
    }

    private static class FcmTransport {
        @JsonProperty(value = "context")
        private String context;

        @JsonProperty(value = "creationTime")
        private String creationTime;

        @JsonProperty(value = "path")
        private String path;

        @JsonProperty(value = "ttl")
        private String ttl;
    }

    private static class Transports {
        @JsonProperty(value = "FCM")
        private List<FcmTransport> fcm;
    }

    private static class RegistrarRequestBody {
        @JsonProperty(value = "clientDescription")
        private ClientDescription clientDescription;

        @JsonProperty(value = "nodeId")
        private String nodeId;

        @JsonProperty(value = "registrationId")
        private String registrationId;

        @JsonProperty(value = "transports")
        private Transports transports;
    }

    RegistrarClient() {
        this.httpClient = HttpClient.createDefault();
        this.jacksonSerder = JacksonSerder.createDefault();
    }

    public boolean register(String skypeUserToken, String deviceRegistrationToken) {
        HttpRequest request = new HttpRequest(HttpMethod.POST, PUSHNOTIFICATION_REGISTRAR_SERVICE_URL);
        request
            .setHeader(USER_AGENT_HEADER, PLATFORM_UI_VERSION)
            .setHeader(CONTENT_TYPE_HEADER, CONTENT_TYPE_JSON)
            .setHeader(SKYPE_TOKEN_HEADER, skypeUserToken);

        addRequestBody(request, deviceRegistrationToken);

        CountDownLatch latch = new CountDownLatch(1);

        this.httpClient.send(request, CancellationToken.NONE, new HttpCallback() {
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
        this.logger.info("Registrar request result:" + requestResult);
        this.logger.info("registrationId:" + registrationId);
        return requestResult;
    }

    public boolean unregister(String skypeUserToken) {
        if (registrationId == null) {
            return false;
        }

        String unregisterUrl = PUSHNOTIFICATION_REGISTRAR_SERVICE_URL + "/" + registrationId;
        HttpRequest request = new HttpRequest(HttpMethod.DELETE, unregisterUrl);
        request
            .setHeader(USER_AGENT_HEADER, PLATFORM_UI_VERSION)
            .setHeader(CONTENT_TYPE_HEADER, CONTENT_TYPE_JSON)
            .setHeader(SKYPE_TOKEN_HEADER, skypeUserToken);

        CountDownLatch latch = new CountDownLatch(1);

        this.httpClient.send(request, CancellationToken.NONE, new HttpCallback() {
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

    private void addRequestBody(HttpRequest request, String deviceRegistrationToken) {
        this.registrationId = UUID.randomUUID().toString();

        ClientDescription clientDescription = new ClientDescription();
        clientDescription.languageId = "";
        clientDescription.platform = PLATFORM;
        clientDescription.platformUIVersion = PLATFORM_UI_VERSION;
        clientDescription.applicationId = PUSHNOTIFICATION_APPLICATION_ID;
        clientDescription.templateKey = PUSHNOTIFICATION_TEMPLATE_KEY;

        Transports transports = new Transports();
        transports.fcm = new ArrayList<>();

        FcmTransport transport = new FcmTransport();
        transport.creationTime = "";
        transport.context = "";
        transport.path = deviceRegistrationToken;
        transport.ttl = PUSHNOTIFICATION_REGISTRAR_SERVICE_TTL;
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

    private boolean awaitOnLatch(CountDownLatch latch) {
        long timeoutInMin = 1;
        try {
            return latch.await(timeoutInMin, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            throw logger.logExceptionAsError(new RuntimeException("Operation didn't complete within " + timeoutInMin + " minutes"));
        }
    }
}
