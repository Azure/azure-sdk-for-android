// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.communication.chat.sampleapp;

import com.azure.android.communication.chat.*;
import com.azure.android.communication.chat.models.AddChatParticipantsRequest;
import com.azure.android.communication.chat.models.AddChatParticipantsResult;
import com.azure.android.communication.chat.models.ChatMessageType;
import com.azure.android.communication.chat.models.ChatParticipant;
import com.azure.android.communication.chat.models.ChatThread;
import com.azure.android.communication.chat.models.CommunicationIdentifierModel;
import com.azure.android.communication.chat.models.CommunicationUserIdentifierModel;
import com.azure.android.communication.chat.models.CreateChatThreadRequest;
import com.azure.android.communication.chat.models.CreateChatThreadResult;
import com.azure.android.communication.chat.models.SendChatMessageRequest;
import com.azure.android.communication.sampleapp.R;
import com.azure.android.core.http.Callback;
import com.azure.android.core.http.HttpHeader;
import com.azure.android.core.http.ServiceClient;
import com.azure.android.core.http.interceptor.UserAgentInterceptor;
import com.azure.android.core.http.responsepaging.AsyncPagedDataCollection;
import com.azure.android.core.util.paging.Page;

import androidx.appcompat.app.AppCompatActivity;


import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private ChatClient chatClient;
    private int eventHandlerCalled;
    private JSONObject eventPayload;

    // Replace <user_token> with your valid communication service token
    private final String userAccessToken = "<user_token>";
    private String id = "8:acs:46849534-eb08-4ab7-bde7-c36928cd1547_00000008-7b5a-8b78-1655-373a0d009ba1";
    private String second_user_id = "8:acs:46849534-eb08-4ab7-bde7-c36928cd1547_00000008-7b73-cf66-dbb7-3a3a0d009c9f";
    private String threadId = "<to_be_updated_below>";
    private final String endpoint = "https://<your_acs_instance>.communication.azure.net";
    private final String listenerId = "testListener";
    private final String sdkVersion = "1.0.0-beta.6";
    private static final String SDK_NAME = "azure-communication-com.azure.android.communication.chat";
    private static final String TAG = "[Chat Test App]";


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
            this.getApplicationContext(),
            SDK_NAME,
            sdkVersion
        );

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
