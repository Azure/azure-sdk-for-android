// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.communication.chat.sampleapp;

import com.azure.android.communication.chat.*;
import com.azure.android.communication.chat.models.AddChatParticipantsRequest;
import com.azure.android.communication.chat.models.AddChatParticipantsResult;
import com.azure.android.communication.chat.models.ChatMessageReadReceipt;
import com.azure.android.communication.chat.models.ChatMessageType;
import com.azure.android.communication.chat.models.ChatParticipant;
import com.azure.android.communication.chat.models.ChatThread;
import com.azure.android.communication.chat.models.CommunicationIdentifierModel;
import com.azure.android.communication.chat.models.CommunicationUserIdentifierModel;
import com.azure.android.communication.chat.models.CreateChatThreadRequest;
import com.azure.android.communication.chat.models.CreateChatThreadResult;
import com.azure.android.communication.chat.models.SendChatMessageRequest;
import com.azure.android.communication.chat.models.SendReadReceiptRequest;
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
    private String chatMessageId = "<to_be_updated_below>";
    private final String endpoint = "https://<your_acs_instance>.communication.azure.net";
    private final String listenerId = "testListener";
    private final String sdkVersion = "1.0.0-beta.8";
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


    public void actionTestBasicOperations(View view) {
        try {
            ChatAsyncClient client = new ChatAsyncClient.Builder()
                .endpoint(endpoint)
                .credentialInterceptor(chain -> chain.proceed(chain.request()
                    .newBuilder()
                    .header(HttpHeader.AUTHORIZATION, "Bearer " + userAccessToken)
                    .build()))
                .build();

            // <CREATE A CHAT THREAD>
            //  The list of ChatParticipant to be added to the thread.
            List<ChatParticipant> participants = new ArrayList<>();
            // The display name for the thread participant.
            String displayName = "initial participant";
            participants.add(new ChatParticipant()
                .setCommunicationIdentifier(new CommunicationIdentifierModel().setCommunicationUser(new CommunicationUserIdentifierModel().setId(id)))
                .setDisplayName(displayName));


            // The topic for the thread.
            final String topic = "General";
            // The model to pass to the create method.
            CreateChatThreadRequest thread = new CreateChatThreadRequest()
                .setTopic(topic)
                .setParticipants(participants);

            // optional, set a repeat request ID
            final String repeatabilityRequestID = "";

            client.createChatThread(thread, repeatabilityRequestID, new Callback<CreateChatThreadResult>() {
                public void onSuccess(CreateChatThreadResult result, okhttp3.Response response) {
                    ChatThread chatThread = result.getChatThread();
                    threadId = chatThread.getId();
                    // take further action
                    log("threadId: " + threadId);
                }

                public void onFailure(Throwable throwable, okhttp3.Response response) {
                    // Handle error.
                    Log.e(TAG, throwable.getMessage());
                }
            });

            // <CREATE A CHAT THREAD CLIENT>
            ChatThreadAsyncClient threadClient =
                new ChatThreadAsyncClient.Builder()
                    .endpoint(endpoint)
                    .credentialInterceptor(chain -> chain.proceed(chain.request()
                        .newBuilder()
                        .header(HttpHeader.AUTHORIZATION, "Bearer " + userAccessToken)
                        .build()))
                    .build();

            // <SEND A MESSAGE>
            // The chat message content, required.
            final String content = "Test message 1";
            // The display name of the sender, if null (i.e. not specified), an empty name will be set.
            final String senderDisplayName = "An important person";
            SendChatMessageRequest message = new SendChatMessageRequest()
                .setType(ChatMessageType.TEXT)
                .setContent(content)
                .setSenderDisplayName(senderDisplayName);

            threadClient.sendChatMessage(threadId, message, new Callback<String>() {
                @Override
                public void onSuccess(String messageId, okhttp3.Response response) {
                    // A string is the response returned from sending a message, it is an id,
                    // which is the unique ID of the message.
                    chatMessageId = messageId;
                    // Take further action.
                    log("chatMessageId: " + chatMessageId);
                }

                @Override
                public void onFailure(Throwable throwable, okhttp3.Response response) {
                    // Handle error.
                    Log.e(TAG, throwable.getMessage());
                }
            });

            // <ADD A USER>
            //  The list of ChatParticipant to be added to the thread.
            participants = new ArrayList<>();

            // The display name for the thread participant.
            String secondUserDisplayName = "second participant";
            participants.add(new ChatParticipant().setCommunicationIdentifier(
                new CommunicationIdentifierModel().setCommunicationUser(
                    new CommunicationUserIdentifierModel().setId(second_user_id)
                )).setDisplayName(secondUserDisplayName));
            // The model to pass to the add method.
            AddChatParticipantsRequest addParticipantsRequest = new AddChatParticipantsRequest()
                .setParticipants(participants);

            threadClient.addChatParticipants(threadId, addParticipantsRequest, new Callback<AddChatParticipantsResult>() {
                @Override
                public void onSuccess(AddChatParticipantsResult result, okhttp3.Response response) {
                    // Take further action.
                    log("add chat participants success");
                }

                @Override
                public void onFailure(Throwable throwable, okhttp3.Response response) {
                    // Handle error.
                    Log.e(TAG, throwable.getMessage());
                }
            });

            // <LIST USERS>

            // The maximum number of participants to be returned per page, optional.
            int maxPageSize = 10;

            // Skips participants up to a specified position in response.
            int skip = 0;

            threadClient.listChatParticipantsPages(threadId,
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
                });

            // <REMOVE A USER>
            // The unique ID of the participant.
            CommunicationIdentifierModel communicationIdentifierModel = new CommunicationIdentifierModel().setCommunicationUser(new CommunicationUserIdentifierModel().setId(second_user_id));
            threadClient.removeChatParticipant(threadId, communicationIdentifierModel, new Callback<Void>() {
                @Override
                public void onSuccess(Void result, okhttp3.Response response) {
                    // Take further action.
                    log("remove a user successfully");
                }

                @Override
                public void onFailure(Throwable throwable, okhttp3.Response response) {
                    // Handle error.
                    Log.e(TAG, throwable.getMessage());
                }
            });


            // <<SEND A TYPING NOTIFICATION>>
            threadClient.sendTypingNotification(threadId, new Callback<Void>() {
                @Override
                public void onSuccess(Void result, Response response) {
                    Log.i(TAG, "send a typing notification successfully");
                }

                @Override
                public void onFailure(Throwable throwable, Response response) {
                    Log.e(TAG, throwable.getMessage());
                }
            });

            // <<SEND A READ RECEIPT>>
            SendReadReceiptRequest readReceipt = new SendReadReceiptRequest()
                .setChatMessageId(chatMessageId);
            threadClient.sendChatReadReceipt(threadId, readReceipt, new Callback<Void>() {
                @Override
                public void onSuccess(Void result, Response response) {
                    Log.i(TAG, "send a read receipt successfully");
                }

                @Override
                public void onFailure(Throwable throwable, Response response) {
                    Log.e(TAG, throwable.getMessage());
                }
            });

            // <<LIST READ RECEIPTS>>
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
                });

        } catch (Exception e){
            System.out.println("Quickstart failed: " + e.getMessage());
        }
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

    void listChatParticipantsNext(String nextLink, AsyncPagedDataCollection<ChatParticipant, Page<ChatParticipant>> pageCollection) {
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

    void listChatReadReceiptsNext(String nextLink, AsyncPagedDataCollection<ChatMessageReadReceipt, Page<ChatMessageReadReceipt>> pageCollection) {
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
    }
}
