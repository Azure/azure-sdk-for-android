// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.communication.sample_chat_app;

import com.azure.android.communication.chat.*;
import com.azure.android.core.http.ServiceClient;
import com.azure.android.core.http.interceptor.UserAgentInterceptor;

import androidx.appcompat.app.AppCompatActivity;


import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    private ChatClient chatClient;
    private int eventHandlerCalled;
    private JSONObject eventPayload;

    // Replace <userToken> with your valid communication service token
    private final String userAccessToken = "<userToken>";
    private final String endpoint = "https://chat-sdktester-e2e.int.communication.azure.net";
    private final String listenerId = "testListener";
    private final String sdkVersion = "1.0.0-beta.6";
    private static final String SDK_NAME = "azure-communication-com.azure.android.communication.signaling.signaling";
    private static final String TAG = "--------------Chat Signaling Test Demo-----------";


    private void log(String msg) {
        Log.i(TAG, msg);
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startChatClient();
    }

    public void startChatClient() {
        log("Creating chat client");

        UserAgentInterceptor userAgentInterceptor = new UserAgentInterceptor(
            null,
            SDK_NAME,
            sdkVersion,
            null,
            null,
            null);

        // Initialize the chat client
        chatClient = new ChatClient.Builder()
            .serviceClientBuilder(new ServiceClient.Builder())
            .endpoint(endpoint)
            .userAgentInterceptor(userAgentInterceptor)
            .realtimeNotificationParams(this.getApplicationContext(), userAccessToken)
            .build();
    }

    public void actionStartRealtimeNotification(View view) {
        Log.i(TAG, "Starting real time notification");
        try {
            chatClient.startRealtimeNotifications();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }

    public void actionRegisterATestListener(View view) {
        // Act - subscribe
        log("Register a test listener");
        chatClient.on("chatMessageReceived", listenerId, (JSONObject payload) -> {
            eventHandlerCalled++;
            eventPayload = payload;

            Log.i(TAG, eventHandlerCalled + " messages handled.");
            System.out.printf("Message received! Content is %s.", eventPayload);
            Log.i(TAG, payload.toString());
        });
    }

    public void actionUnregisterATestListener(View view) {
        // Act - subscribe
        log("Unregister a test listener");
        chatClient.off("chatMessageReceived", listenerId);
    }
}
