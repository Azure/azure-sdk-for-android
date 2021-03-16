// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.communication.chat.sampleapp;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.azure.android.communication.chat.ChatAsyncClient;
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

import org.threeten.bp.OffsetDateTime;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;


public class MainActivity extends AppCompatActivity {

    private ChatAsyncClient chatAsyncClient;
    private ChatThreadAsyncClient chatThreadAsyncClient;
    //private int eventHandlerCalled;
    //private JSONObject eventPayload;

    // Replace firstUserId and secondUserId with valid communication user identifiers from your ACS instance.
    private String firstUserId = "<first-user-id>";
    private String secondUserId = "<second-useirer-id>";
    // Replace userAccessToken with a valid communication service token for your ACS instance.
    private final String firstUserAccessToken = "<first-user-access-token>";
    private String threadId = "<to-be-updated-below>";
    private String chatMessageId = "<to-be-updated-below>";
    private final String endpoint = "https://vicolina-acs.communication.azure.com";
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

        createChatAsyncClient();
    }

    public void createChatAsyncClient() {
        try {
            chatAsyncClient = new ChatClientBuilder()
                .endpoint(endpoint)
                .credentialPolicy(new BearerTokenAuthenticationPolicy((request, callback) ->
                    callback.onSuccess(new AccessToken(firstUserAccessToken, OffsetDateTime.now().plusDays(1)))))
                .addPolicy(new UserAgentPolicy(APPLICATION_ID, SDK_NAME, sdkVersion))
                .httpClient(new OkHttpAsyncClientProvider().createInstance())
                .buildAsyncClient();

            Log.d(TAG, "Created ChatAsyncClient");
        } catch (Exception e) {
            Log.e("ChatAsyncClient creation failed", Objects.requireNonNull(e.getMessage()));
        }
    }

    public void createChatThreadAsyncClient() {
        if (chatAsyncClient == null) {
            createChatAsyncClient();
        }

        // A list of participants to start the thread with.
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
        // Options to pass to the create method.
        CreateChatThreadOptions createChatThreadOptions = new CreateChatThreadOptions()
            .setTopic(topic)
            .setParticipants(participants)
            .setRepeatabilityRequestId(repeatabilityRequestID);

        try {
            CreateChatThreadResult createChatThreadResult =
                chatAsyncClient.createChatThread(createChatThreadOptions).get();

            ChatThread chatThread = createChatThreadResult.getChatThread();
            threadId = chatThread.getId();

            logAndToast("Created thread with ID: " + threadId);

            chatThreadAsyncClient = chatAsyncClient.getChatThreadClient(threadId);

            Log.d(TAG, "Created ChatThreadAsyncClient");
        } catch (InterruptedException | ExecutionException e) {
            Log.e("ChatThreadAsyncClient creation failed", Objects.requireNonNull(e.getMessage()));
        }
    }

    private void logAndToast(String msg) {
        Log.d(TAG, msg);
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }

    public void startRealTimeNotification(View view) {
        logAndToast("Implementation pending");

        /*Log.i(TAG, "Starting real time notification");
        try {
            chatClient.startRealtimeNotifications();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }*/
    }

    public void registerRealTimeNotificationListener(View view) {
        logAndToast("Implementation pending");

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

    public void unregisterRealTimeNotificationListener(View view) {
        logAndToast("Implementation pending");

        /*// Act - subscribe
        log("Unregister a test listener");
        chatClient.off("chatMessageReceived", listenerId);*/
    }

    public void sendChatMessage(View view) {
        if (chatThreadAsyncClient == null) {
            createChatThreadAsyncClient();
        }

        if (chatThreadAsyncClient != null) {
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
            try {
                chatMessageId = chatThreadAsyncClient.sendMessage(chatMessageOptions).get();

                logAndToast("Message sent with ID: " + chatMessageId);
            } catch (InterruptedException | ExecutionException e) {
                logAndToast("Send message failed: " + e.getMessage());
            }
        } else {
            logAndToast("ChatThreadAsyncClient creation failed");
        }
    }

    public void addParticipant(View view) {
        if (chatThreadAsyncClient == null) {
            createChatThreadAsyncClient();
        }

        if (chatThreadAsyncClient != null) {
            // The display name for the thread participant.
            String secondUserDisplayName = "Second participant";

            try {
                chatThreadAsyncClient.addParticipant(new ChatParticipant().setCommunicationIdentifier(
                    new CommunicationUserIdentifier(secondUserId)).setDisplayName(secondUserDisplayName)).get();

                logAndToast("Added chat participant");
            } catch (InterruptedException | ExecutionException e) {
                logAndToast("Add user failed: " + e.getMessage());
            }
        } else {
            logAndToast("ChatThreadAsyncClient creation failed");
        }
    }

    public void listParticipants(View view) {
        logAndToast("Implementation pending");

        /*
        if (chatThreadAsyncClient == null) {
            createChatThreadAsyncClient();
        }

        // The maximum number of participants to be returned per page, optional.
        int maxPageSize = 10;
        // Skips participants up to a specified position in response.
        int skip = 0;

        threadClient.listlistChatParticipantsPages(threadId,
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
    }

    public void removeParticipant(View view) {
        if (chatThreadAsyncClient == null) {
            createChatThreadAsyncClient();
        }

        if (chatThreadAsyncClient != null) {
            try {
                // Using the unique ID of the participant.
                chatThreadAsyncClient.removeParticipant(new CommunicationUserIdentifier(secondUserId)).get();

                logAndToast("Removed second participant");
            } catch (InterruptedException | ExecutionException e) {
                logAndToast("Remove user failed: " + e.getMessage());
            }
        } else {
            logAndToast("ChatThreadAsyncClient creation failed");
        }
    }

    public void sendTypingNotification(View view) {
        if (chatThreadAsyncClient == null) {
            createChatThreadAsyncClient();
        }

        if (chatThreadAsyncClient != null) {
            try {
                chatThreadAsyncClient.sendTypingNotification().get();

                logAndToast("Sent a typing notification successfully");
            } catch (InterruptedException | ExecutionException e) {
                logAndToast("Send typing notification failed: " + e.getMessage());
            }
        } else {
            logAndToast("ChatThreadAsyncClient creation failed");
        }
    }

    public void sendReadReceipt(View view) {
        if (chatThreadAsyncClient == null) {
            createChatThreadAsyncClient();
        }

        if (chatThreadAsyncClient != null) {
            try {
                chatThreadAsyncClient.sendReadReceipt(chatMessageId).get();

                logAndToast("Sent a read receipt for message with ID: " + chatMessageId);
            } catch (InterruptedException | ExecutionException e) {
                logAndToast("Send read receipt failed: " + e.getMessage());
            }
        } else {
            logAndToast("ChatThreadAsyncClient creation failed");
        }
    }

    public void listReadReceipts(View view) {
        logAndToast("Implementation pending");

        /*
        if (chatThreadAsyncClient == null) {
            createChatThreadAsyncClient();
        }

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
