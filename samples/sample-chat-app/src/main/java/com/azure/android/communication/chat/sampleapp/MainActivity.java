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
import com.azure.android.communication.chat.models.ChatMessageDeletedEvent;
import com.azure.android.communication.chat.models.ChatMessageEditedEvent;
import com.azure.android.communication.chat.models.ChatMessageReadReceipt;
import com.azure.android.communication.chat.models.ChatMessageReceivedEvent;
import com.azure.android.communication.chat.models.ChatMessageType;
import com.azure.android.communication.chat.models.ChatParticipant;
import com.azure.android.communication.chat.models.ChatThreadCreatedEvent;
import com.azure.android.communication.chat.models.ChatThreadDeletedEvent;
import com.azure.android.communication.chat.models.ChatThreadProperties;
import com.azure.android.communication.chat.models.ChatThreadPropertiesUpdatedEvent;
import com.azure.android.communication.chat.models.CreateChatThreadOptions;
import com.azure.android.communication.chat.models.CreateChatThreadResult;
import com.azure.android.communication.chat.models.ListParticipantsOptions;
import com.azure.android.communication.chat.models.ListReadReceiptOptions;
import com.azure.android.communication.chat.models.ParticipantsAddedEvent;
import com.azure.android.communication.chat.models.ParticipantsRemovedEvent;
import com.azure.android.communication.chat.models.ReadReceiptReceivedEvent;
import com.azure.android.communication.chat.models.RealTimeNotificationCallback;
import com.azure.android.communication.chat.models.SendChatMessageOptions;
import com.azure.android.communication.chat.models.ChatEvent;
import com.azure.android.communication.chat.models.TypingIndicatorReceivedEvent;
import com.azure.android.communication.chat.models.TypingNotificationOptions;
import com.azure.android.communication.common.CommunicationTokenCredential;
import com.azure.android.communication.common.CommunicationUserIdentifier;
import com.azure.android.core.http.policy.HttpLogDetailLevel;
import com.azure.android.core.http.policy.HttpLogOptions;
import com.azure.android.core.http.policy.UserAgentPolicy;
import com.azure.android.core.rest.util.paging.PagedAsyncStream;
import com.azure.android.core.serde.jackson.JacksonSerder;
import com.azure.android.core.util.AsyncStreamHandler;
import com.azure.android.core.util.RequestContext;
import com.jakewharton.threetenabp.AndroidThreeTen;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.StringJoiner;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static com.azure.android.communication.chat.models.ChatEventType.CHAT_MESSAGE_RECEIVED;
import static com.azure.android.communication.chat.models.ChatEventType.CHAT_MESSAGE_EDITED;
import static com.azure.android.communication.chat.models.ChatEventType.CHAT_MESSAGE_DELETED;
import static com.azure.android.communication.chat.models.ChatEventType.TYPING_INDICATOR_RECEIVED;
import static com.azure.android.communication.chat.models.ChatEventType.READ_RECEIPT_RECEIVED;
import static com.azure.android.communication.chat.models.ChatEventType.CHAT_THREAD_CREATED;
import static com.azure.android.communication.chat.models.ChatEventType.CHAT_THREAD_PROPERTIES_UPDATED;
import static com.azure.android.communication.chat.models.ChatEventType.CHAT_THREAD_DELETED;
import static com.azure.android.communication.chat.models.ChatEventType.PARTICIPANTS_ADDED;
import static com.azure.android.communication.chat.models.ChatEventType.PARTICIPANTS_REMOVED;

public class MainActivity extends AppCompatActivity {

    private ChatAsyncClient chatAsyncClient;
    private ChatThreadAsyncClient chatThreadAsyncClient;
    private int eventHandlerCalled;

    // Replace firstUserId and secondUserId with valid communication user identifiers from your ACS instance.
    private String firstUserId = "";
    private String secondUserId = "";
    // Replace userAccessToken with a valid communication service token for your ACS instance.
    private final String firstUserAccessToken = "";
    private String threadId = "<to-be-updated-below>";
    private String chatMessageId = "<to-be-updated-below>";
    private final String endpoint = "";
    private final String sdkVersion = "1.1.0-beta.3";
    private static final String SDK_NAME = "azure-communication-com.azure.android.communication.chat";
    private static final String APPLICATION_ID = "Chat_Test_App";
    private static final String TAG = "[Chat Test App]";
    private final Queue<String> unreadMessages = new ConcurrentLinkedQueue<>();
    private static RealTimeNotificationCallback messageReceivedHandler;

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
                .credential(new CommunicationTokenCredential(firstUserAccessToken))
                .addPolicy(new UserAgentPolicy(APPLICATION_ID, SDK_NAME, sdkVersion))
                .httpLogOptions(new HttpLogOptions()
                    .setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS)
                    .addAllowedHeaderName("MS-CV"))
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
            .setIdempotencyToken(repeatabilityRequestID);

        try {
            CreateChatThreadResult createChatThreadResult =
                chatAsyncClient.createChatThread(createChatThreadOptions).get();

            ChatThreadProperties chatThreadProperties = createChatThreadResult.getChatThreadProperties();
            threadId = chatThreadProperties.getId();

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
        logAndToast( "Starting real time notification");
        try {
            chatAsyncClient.startRealtimeNotifications(firstUserAccessToken, getApplicationContext());
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }

    public void registerRealTimeNotificationListener(View view) {
        logAndToast("Register a test listener");
        JacksonSerder jacksonSerder = JacksonSerder.createDefault();

        messageReceivedHandler = (ChatEvent payload) -> {
            eventHandlerCalled++;

            Log.i(TAG, eventHandlerCalled + " messages handled.");
            ChatMessageReceivedEvent event = (ChatMessageReceivedEvent) payload;
            Log.i(TAG, "Message created! ThreadId: " + event.getChatThreadId());
        };

        chatAsyncClient.addEventHandler(CHAT_MESSAGE_RECEIVED, messageReceivedHandler);

        chatAsyncClient.addEventHandler(CHAT_MESSAGE_EDITED, (ChatEvent payload) -> {
            eventHandlerCalled++;

            Log.i(TAG, eventHandlerCalled + " messages handled.");
            ChatMessageEditedEvent event = (ChatMessageEditedEvent) payload;
            Log.i(TAG, "Message edited! ThreadId: " + event.getChatThreadId());
        });

        chatAsyncClient.addEventHandler(CHAT_MESSAGE_DELETED, (ChatEvent payload) -> {
            eventHandlerCalled++;

            Log.i(TAG, eventHandlerCalled + " messages handled.");
            ChatMessageDeletedEvent event = (ChatMessageDeletedEvent) payload;
            Log.i(TAG, "Message deleted! ThreadId: " + event.getChatThreadId());
        });

        chatAsyncClient.addEventHandler(TYPING_INDICATOR_RECEIVED, (ChatEvent payload) -> {
            eventHandlerCalled++;

            Log.i(TAG, eventHandlerCalled + " messages handled.");
            TypingIndicatorReceivedEvent event = (TypingIndicatorReceivedEvent) payload;
            Log.i(TAG, "Typing indicator received! ThreadId: " + event.getChatThreadId());
        });

        chatAsyncClient.addEventHandler(READ_RECEIPT_RECEIVED, (ChatEvent payload) -> {
            eventHandlerCalled++;

            Log.i(TAG, eventHandlerCalled + " messages handled.");
            ReadReceiptReceivedEvent event = (ReadReceiptReceivedEvent) payload;
            Log.i(TAG, "Read receipt received! ThreadId: " + event.getChatThreadId());
        });

        chatAsyncClient.addEventHandler(CHAT_THREAD_CREATED, (ChatEvent payload) -> {
            eventHandlerCalled++;

            Log.i(TAG, eventHandlerCalled + " messages handled.");
            ChatThreadCreatedEvent event = (ChatThreadCreatedEvent) payload;
            Log.i(TAG, "Chat thread created! ThreadId: " + event.getChatThreadId());
        });

        chatAsyncClient.addEventHandler(CHAT_THREAD_DELETED, (ChatEvent payload) -> {
            eventHandlerCalled++;

            Log.i(TAG, eventHandlerCalled + " messages handled.");
            ChatThreadDeletedEvent event = (ChatThreadDeletedEvent) payload;
            Log.i(TAG, "Chat thread deleted! ThreadId: " + event.getChatThreadId());
        });

        chatAsyncClient.addEventHandler(CHAT_THREAD_PROPERTIES_UPDATED, (ChatEvent payload) -> {
            eventHandlerCalled++;

            Log.i(TAG, eventHandlerCalled + " messages handled.");
            ChatThreadPropertiesUpdatedEvent event = (ChatThreadPropertiesUpdatedEvent) payload;
            Log.i(TAG, "Chat thread properties updated! ThreadId: " + event.getChatThreadId());
        });

        chatAsyncClient.addEventHandler(PARTICIPANTS_ADDED, (ChatEvent payload) -> {
            eventHandlerCalled++;

            Log.i(TAG, eventHandlerCalled + " messages handled.");
            ParticipantsAddedEvent event = (ParticipantsAddedEvent) payload;
            Log.i(TAG, "Participants added! ThreadId: " + event.getChatThreadId());
        });

        chatAsyncClient.addEventHandler(PARTICIPANTS_REMOVED, (ChatEvent payload) -> {
            eventHandlerCalled++;

            Log.i(TAG, eventHandlerCalled + " messages handled.");
            ParticipantsRemovedEvent event = (ParticipantsRemovedEvent) payload;
            Log.i(TAG, "Participants removed! ThreadId: " + event.getChatThreadId());
        });
    }

    public void unregisterRealTimeNotificationListener(View view) {
        logAndToast("Unregister a test listener");
        chatAsyncClient.removeEventHandler(CHAT_MESSAGE_RECEIVED, messageReceivedHandler);
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
                .setSenderDisplayName(senderDisplayName)
                .setMetadata(new HashMap<String, String>() {
                    {
                        put("tags", "tag1");
                        put("deliveryMode", "deliveryMode value");
                    }
                });

            // A string is the response returned from sending a message, it is an id, which is the unique ID of the
            // message.
            try {
                chatMessageId = chatThreadAsyncClient.sendMessage(chatMessageOptions).get().getId();
                unreadMessages.add(chatMessageId);

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
        if (chatThreadAsyncClient == null) {
            createChatThreadAsyncClient();
        }

        if (chatThreadAsyncClient != null) {
            // The maximum number of participants to be returned per page, optional.
            int maxPageSize = 10;
            // Skips participants up to a specified position in response.
            int skip = 0;
            // Options to pass to the list method.
            ListParticipantsOptions listParticipantsOptions = new ListParticipantsOptions()
                .setMaxPageSize(maxPageSize)
                .setSkip(skip);

            try {
                PagedAsyncStream<ChatParticipant> participantPagedAsyncStream
                    = chatThreadAsyncClient.listParticipants(new ListParticipantsOptions(), null);

                StringJoiner participantsStringJoiner =
                    new StringJoiner(
                        "\nParticipant: ",
                        "",
                        ""
                    );

                CountDownLatch latch = new CountDownLatch(1);

                participantPagedAsyncStream.forEach(new AsyncStreamHandler<ChatParticipant>() {
                    @Override
                    public void onNext(ChatParticipant participant) {
                        participantsStringJoiner.add(participant.getDisplayName());
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        latch.countDown();
                    }

                    @Override
                    public void onComplete() {
                        latch.countDown();
                    }
                });

                awaitOnLatch(latch);
                logAndToast(participantsStringJoiner.toString());
            } catch (Exception e) {
                logAndToast("Listing participants failed: " + e.getMessage());
            }
        } else {
            logAndToast("ChatThreadAsyncClient creation failed");
        }
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
                TypingNotificationOptions options = new TypingNotificationOptions();
                options.setSenderDisplayName("Sender Display Name");

                chatThreadAsyncClient.sendTypingNotification(options).get();

                logAndToast("Sent a typing notification successfully");
            } catch (InterruptedException | ExecutionException e) {
                logAndToast("Send typing notification failed: " + e.getMessage());
            }
        } else {
            logAndToast("ChatThreadAsyncClient creation failed");
        }
    }

    public void sendReadReceipts(View view) {
        if (chatThreadAsyncClient == null) {
            createChatThreadAsyncClient();
        }

        if (chatThreadAsyncClient != null) {
            try {
                for (String unreadMessageId : unreadMessages) {
                    chatThreadAsyncClient.sendReadReceipt(unreadMessageId).get();
                    unreadMessages.poll();

                    logAndToast("Sent a read receipt for message with ID: " + unreadMessageId);
                }
            } catch (InterruptedException | ExecutionException e) {
                logAndToast("Send read receipt failed: " + e.getMessage());
            }
        } else {
            logAndToast("ChatThreadAsyncClient creation failed");
        }
    }

    public void listReadReceipts(View view) {
        if (chatThreadAsyncClient == null) {
            createChatThreadAsyncClient();
        }

        if (chatThreadAsyncClient != null) {
            // The maximum number of read receipts to be returned per page, optional.
            int maxPageSize = 10;
            // Skips participants up to a specified position in response.
            int skip = 0;
            // Options to pass to the list method.
            ListReadReceiptOptions listReadReceiptOptions = new ListReadReceiptOptions()
                .setMaxPageSize(maxPageSize)
                .setSkip(skip);

            try {
                PagedAsyncStream<ChatMessageReadReceipt> readReceipts =
                    chatThreadAsyncClient.listReadReceipts(listReadReceiptOptions, RequestContext.NONE);
                readReceipts.forEach(readReceipt -> {
                    Log.d(TAG, "Got receipt for participant "
                        + ((CommunicationUserIdentifier)readReceipt.getSenderCommunicationIdentifier()).getId()
                        + " for message with id: "
                        + readReceipt.getChatMessageId());
                });
            } catch (Exception e) {
                logAndToast("Listing read receipts failed: " + e.getMessage());
            }
        } else {
            logAndToast("ChatThreadAsyncClient creation failed");
        }
    }

    private static void awaitOnLatch(CountDownLatch latch) {
        long timeoutInSec = 2;
        try {
            latch.await(timeoutInSec, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            Log.e(TAG, "List operation didn't complete within " + timeoutInSec + " minutes");
        }
    }
}
