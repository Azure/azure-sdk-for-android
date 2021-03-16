// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.communication.chat.sampleapp;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.azure.android.communication.chat.ChatAsyncClient;
import com.azure.android.communication.chat.ChatClient;
import com.azure.android.communication.chat.ChatClientBuilder;
import com.azure.android.communication.chat.ChatThreadAsyncClient;
import com.azure.android.communication.chat.models.ChatMessageType;
import com.azure.android.communication.chat.models.ChatParticipant;
import com.azure.android.communication.chat.models.ChatThread;
import com.azure.android.communication.chat.models.CreateChatThreadOptions;
import com.azure.android.communication.chat.models.CreateChatThreadResult;
import com.azure.android.communication.chat.models.SendChatMessageOptions;
import com.azure.android.communication.common.CommunicationUserIdentifier;
import com.azure.android.core.credential.AccessToken;
import com.azure.android.core.http.okhttp.OkHttpAsyncClientProvider;
import com.azure.android.core.http.policy.BearerTokenAuthenticationPolicy;
import com.azure.android.core.http.policy.UserAgentPolicy;
import com.jakewharton.threetenabp.AndroidThreeTen;

import org.json.JSONObject;
import org.threeten.bp.OffsetDateTime;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    private ChatClient chatClient;
    private int eventHandlerCalled;
    private JSONObject eventPayload;

    // Replace userAccessToken with a valid communication service token for your ACS instance.
    private final String userAccessToken = "<first-user-access-token>";
    private String firstUserId = "<first-user-id>";
    private String secondUserId = "<second-user-id>";
    private String threadId = "<to-be-updated-below>";
    private String chatMessageId = "<to-be-updated-below>";
    private final String endpoint = "https://<your-acs-instance>.communication.azure.com";
    private final String listenerId = "testListener";
    private final String sdkVersion = "1.0.0-beta.8";
    private static final String SDK_NAME = "azure-communication-com.azure.android.communication.chat";
    private static final String APPLICATION_ID = "Chat Test App";
    private static final String TAG = "[Chat Test App]";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        AndroidThreeTen.init(this);

        //startChatClient();
    }

    /*public void startChatClient() {
        log("Creating chat client");

        UserAgentPolicy userAgentPolicy = new UserAgentPolicy(
            APPLICATION_ID,
            SDK_NAME,
            sdkVersion
        );

        // Initialize the chat client
        chatClient = new ChatClientBuilder()
            .endpoint(endpoint)
            .addPolicy(userAgentPolicy)
            //.realtimeNotificationParams(this.getApplicationContext(), userAccessToken)
            .buildClient();
    }*/

    public void actionTestBasicOperations(View view) {
        try {
            ChatAsyncClient chatAsyncClient = new ChatClientBuilder()
                .endpoint(endpoint)
                .credentialPolicy(new BearerTokenAuthenticationPolicy((request, callback) ->
                    callback.onSuccess(new AccessToken(userAccessToken, OffsetDateTime.now().plusDays(1)))))
                .addPolicy(new UserAgentPolicy(APPLICATION_ID, SDK_NAME, sdkVersion))
                .httpClient(new OkHttpAsyncClientProvider().createInstance())
                .buildAsyncClient();

            // <CREATE A CHAT THREAD>
            // The list of ChatParticipant to be added to the thread.
            List<ChatParticipant> participants = new ArrayList<>();
            // The display name for the thread participant.
            String displayName = "First participant";
            participants.add(new ChatParticipant()
                .setCommunicationIdentifier(new CommunicationUserIdentifier(firstUserId))
                .setDisplayName(displayName));


            // The topic for the thread.
            final String topic = "General";
            // Optional, set a repeat request ID.
            final String repeatabilityRequestID = "";
            // The model to pass to the create method.
            CreateChatThreadOptions createChatThreadOptions = new CreateChatThreadOptions()
                .setTopic(topic)
                .setParticipants(participants)
                .setRepeatabilityRequestId(repeatabilityRequestID);

            CreateChatThreadResult createChatThreadResult =
                chatAsyncClient.createChatThread(createChatThreadOptions).get();

            ChatThread chatThread = createChatThreadResult.getChatThread();
            threadId = chatThread.getId();

            Log.i(TAG, "Created thread with ID: " + threadId);

            // <CREATE A CHAT THREAD CLIENT>
            ChatThreadAsyncClient threadClient =
                chatAsyncClient.getChatThreadClient(threadId);

            // <SEND A MESSAGE>
            // The chat message content, required.
            final String content = "Test message 1";
            // The display name of the sender, if null (i.e. not specified), an empty name will be set.
            final String senderDisplayName = "First participant";
            SendChatMessageOptions chatMessageOptions = new SendChatMessageOptions()
                .setType(ChatMessageType.TEXT)
                .setContent(content)
                .setSenderDisplayName(senderDisplayName);

            // A string is the response returned from sending a message, it is an id, which is the unique ID of the
            // message.
            chatMessageId = threadClient.sendMessage(chatMessageOptions).get();

            Log.i(TAG, "Message sent with ID: " + chatMessageId);

            // <ADD A USER>
            // The display name for the thread participant.
            String secondUserDisplayName = "Second participant";

            threadClient.addParticipant(new ChatParticipant().setCommunicationIdentifier(
                new CommunicationUserIdentifier(secondUserId)).setDisplayName(secondUserDisplayName)).get();

            Log.i(TAG, "Added chat participant.");

            // <LIST USERS>
            // The maximum number of participants to be returned per page, optional.
            int maxPageSize = 10;
            // Skips participants up to a specified position in response.
            int skip = 0;

            /*threadClient.listlistChatParticipantsPages(threadId,
                maxPageSize,
                skip,
                new Callback<AsyncPagedDataCollection<ChatParticipant, Page<ChatParticipant>>>() {
                    @Override
                    public void onSuccess(AsyncPagedDataCollection<ChatParticipant, Page<ChatParticipant>> pageCollection,
                                          okhttp3.Response response) {
                        // pageCollection enables enumerating list of chat participants.
                        pageCollection.getFirstPage(new Callback<Page<ChatParticipant>>() {
                            @Override
                            public void onSuccess(Page<ChatParticipant> firstPage, okhttp3.Response response) {
                                for (ChatParticipant participant : firstPage.getItems()) {
                                    // Take further action.
                                    Log.i(TAG, "participant: " + participant.getDisplayName());
                                }
                                listChatParticipantsNext(firstPage.getNextPageId(), pageCollection);
                            }

                            @Override
                            public void onFailure(Throwable throwable, okhttp3.Response response) {
                                // Handle error.
                                Log.e(TAG, throwable.getMessage());
                            }
                        });
                    }

                    @Override
                    public void onFailure(Throwable throwable, okhttp3.Response response) {
                        // Handle error.
                        Log.e(TAG, throwable.getMessage());
                    }
                });*/

            // <REMOVE A USER>
            // Using the unique ID of the participant.
            threadClient.removeParticipant(new CommunicationUserIdentifier(secondUserId)).get();
            Log.i(TAG, "Removed participant successfully.");

            // <<SEND A TYPING NOTIFICATION>>
            threadClient.sendTypingNotification().get();
            Log.i(TAG, "Sent a typing notification successfully.");

            // <<SEND A READ RECEIPT>>
            threadClient.sendReadReceipt(chatMessageId);

            Log.i(TAG, "Sent a read receipt for message with ID: " + chatMessageId);

            /*// <<LIST READ RECEIPTS>>
            // The maximum number of participants to be returned per page, optional.
            maxPageSize = 10;
            // Skips participants up to a specified position in response.
            skip = 0;
            threadClient.listChatReadReceiptsPages(threadId,
                maxPageSize,
                skip,
                new Callback<AsyncPagedDataCollection<ChatMessageReadReceipt, Page<ChatMessageReadReceipt>>>() {
                    @Override
                    public void onSuccess(AsyncPagedDataCollection<ChatMessageReadReceipt, Page<ChatMessageReadReceipt>> pageCollection,
                                          Response response) {
                        // pageCollection enables enumerating list of chat participants.
                        pageCollection.getFirstPage(new Callback<Page<ChatMessageReadReceipt>>() {
                            @Override
                            public void onSuccess(Page<ChatMessageReadReceipt> firstPage, Response response) {
                                for (ChatMessageReadReceipt receipt : firstPage.getItems()) {
                                    Log.i(TAG, "receipt: " + receipt.getChatMessageId());
                                }
                                listChatReadReceiptsNext(firstPage.getNextPageId(), pageCollection);
                            }

                            @Override
                            public void onFailure(Throwable throwable, Response response) {
                                Log.e(TAG, throwable.getMessage());
                            }
                        });
                    }

                    @Override
                    public void onFailure(Throwable throwable, Response response) {
                        Log.e(TAG, throwable.getMessage());
                    }
                });*/
        } catch (Exception e) {
            System.out.println("Quickstart failed: " + e.getMessage());
        }
    }

    private void logAndToast(String msg) {
        Log.i(TAG, msg);
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }

    public void actionStartRealtimeNotification(View view) {
        logAndToast("Implementation pending");

        /*Log.i(TAG, "Starting real time notification");
        try {
            chatClient.startRealtimeNotifications();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }*/
    }

    public void actionRegisterATestListener(View view) {
        Log.i(TAG, "Starting real time notification");

        /*// Act - subscribe
        log("Register a test listener");
        chatClient.on("chatMessageReceived", listenerId, (JSONObject payload) -> {
            eventHandlerCalled++;
            eventPayload = payload;

            Log.i(TAG, eventHandlerCalled + " messages handled.");
            System.out.printf("Message received! Content is %s.", eventPayload);
            Log.i(TAG, payload.toString());
        });*/
    }

    public void actionUnregisterATestListener(View view) {
        Log.i(TAG, "Starting real time notification");

        /*// Act - subscribe
        log("Unregister a test listener");
        chatClient.off("chatMessageReceived", listenerId);*/
    }

    /*private void listChatParticipantsNext(String nextLink, AsyncPagedDataCollection<ChatParticipant,
    Page<ChatParticipant>> pageCollection) {
        if (nextLink != null) {
            pageCollection.getPage(nextLink, new Callback<Page<ChatParticipant>>() {
                @Override
                public void onSuccess(Page<ChatParticipant> nextPage, Response response) {
                    for (ChatParticipant participant : nextPage.getItems()) {
                        // Take further action.
                        Log.i(TAG, "participant: " + participant.getDisplayName());
                    }
                    if (nextPage.getPageId() != null) {
                        listChatParticipantsNext(nextPage.getPageId(), pageCollection);
                    }
                }

                @Override
                public void onFailure(Throwable throwable, Response response) {
                    Log.e(TAG, throwable.getMessage());
                }
            });
        }
    }

    private void listChatReadReceiptsNext(String nextLink, AsyncPagedDataCollection<ChatMessageReadReceipt, Page<ChatMessageReadReceipt>> pageCollection) {
        if (nextLink != null) {
            pageCollection.getPage(nextLink, new Callback<Page<ChatMessageReadReceipt>>() {
                @Override
                public void onSuccess(Page<ChatMessageReadReceipt> nextPage, Response response) {
                    for (ChatMessageReadReceipt receipt : nextPage.getItems()) {
                        Log.i(TAG, "receipt: " + receipt.getChatMessageId());
                    }
                    if (nextPage.getPageId() != null) {
                        listChatReadReceiptsNext(nextPage.getPageId(), pageCollection);
                    }
                }

                @Override
                public void onFailure(Throwable throwable, Response response) {
                    Log.e(TAG, throwable.getMessage());
                }
            });
        }
    }*/
}
