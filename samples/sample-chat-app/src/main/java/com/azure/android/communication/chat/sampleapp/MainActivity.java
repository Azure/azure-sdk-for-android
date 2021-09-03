// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.communication.chat.sampleapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.azure.android.communication.chat.ChatAsyncClient;
import com.azure.android.communication.chat.ChatClientBuilder;
import com.azure.android.communication.chat.ChatThreadAsyncClient;
import com.azure.android.communication.chat.models.ChatEventType;
import com.azure.android.communication.chat.models.ChatMessageDeletedEvent;
import com.azure.android.communication.chat.models.ChatMessageEditedEvent;
import com.azure.android.communication.chat.models.ChatMessageReadReceipt;
import com.azure.android.communication.chat.models.ChatMessageReceivedEvent;
import com.azure.android.communication.chat.models.ChatMessageType;
import com.azure.android.communication.chat.models.ChatParticipant;
import com.azure.android.communication.chat.models.ChatPushNotification;
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
import com.azure.android.core.util.AsyncStreamHandler;
import com.azure.android.core.util.RequestContext;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;
import com.jakewharton.threetenabp.AndroidThreeTen;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    private final String sdkVersion = "1.1.0-beta.2";
    private static final String SDK_NAME = "azure-communication-com.azure.android.communication.chat";
    private static final String APPLICATION_ID = "Chat_Test_App";
    private static final String TAG = "[Chat Test App]";
    private final Queue<String> unreadMessages = new ConcurrentLinkedQueue<>();
    private static Map<RealTimeNotificationCallback, ChatEventType> realTimeNotificationCallbacks = new HashMap<>();

    private BroadcastReceiver firebaseMessagingReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            ChatPushNotification pushNotification =
                (ChatPushNotification) intent.getParcelableExtra("PushNotificationPayload");

            Log.d(TAG, "Push Notification received in MainActivity: " + pushNotification.getPayload());

            if (chatAsyncClient != null) {
                Log.d(TAG, "Passing push notification to chatAsyncClient.");
                chatAsyncClient.handlePushNotification(pushNotification);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        AndroidThreeTen.init(this);

        createChatAsyncClient();
        LocalBroadcastManager
            .getInstance(this)
            .registerReceiver(
                firebaseMessagingReceiver,
                new IntentFilter("com.azure.android.communication.chat.sampleapp.pushnotification"));
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

    public void startPushNotification(View view) {
        logAndToast( "Start push notification");
        try {
            startFcmPushNotification();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }

    public void registerPushNotificationListener(View view) {
        logAndToast("Register push notification listeners");

        try {

            chatAsyncClient.addPushNotificationHandler(CHAT_MESSAGE_RECEIVED, (ChatEvent payload) -> {
                Log.i(TAG, "Push Notification CHAT_MESSAGE_RECEIVED.");
                ChatMessageReceivedEvent event = (ChatMessageReceivedEvent) payload;
                Log.i(TAG, "Message received!"
                    + " ThreadId: " + event.getChatThreadId()
                    + " MessageId: " + event.getId()
                    + " Content: " + event.getContent()
                    + " Priority: " + event.getPriority()
                    + " SenderDisplayName: " + event.getSenderDisplayName()
                    + " SenderMri: " + ((CommunicationUserIdentifier)event.getSender()).getId()
                    + " Version: " + event.getVersion()
                    + " CreatedOn: " + event.getCreatedOn()
                    + " Type: " + event.getType()
                    + " RecipientMri: " + ((CommunicationUserIdentifier)event.getRecipient()).getId()
                );
            });

            chatAsyncClient.addPushNotificationHandler(CHAT_MESSAGE_EDITED, (ChatEvent payload) -> {
                Log.i(TAG, "Push Notification CHAT_MESSAGE_EDITED.");
                ChatMessageEditedEvent event = (ChatMessageEditedEvent) payload;
                Log.i(TAG, "Message edited!"
                    + " ThreadId: " + event.getChatThreadId()
                    + " MessageId: " + event.getId()
                    + " Content: " + event.getContent()
                    + " SenderDisplayName: " + event.getSenderDisplayName()
                    + " SenderMri: " + ((CommunicationUserIdentifier)event.getSender()).getId()
                    + " Version: " + event.getVersion()
                    + " CreatedOn: " + event.getCreatedOn()
                    + " EditedOn: " + event.getEditedOn()
                    + " RecipientMri: " + ((CommunicationUserIdentifier)event.getRecipient()).getId()
                );
            });

            chatAsyncClient.addPushNotificationHandler(CHAT_MESSAGE_DELETED, (ChatEvent payload) -> {
                Log.i(TAG, "Push Notification CHAT_MESSAGE_DELETED.");
                ChatMessageDeletedEvent event = (ChatMessageDeletedEvent) payload;
                Log.i(TAG, "Message deleted!"
                    + " ThreadId: " + event.getChatThreadId()
                    + " MessageId: " + event.getId()
                    + " SenderDisplayName: " + event.getSenderDisplayName()
                    + " SenderMri: " + ((CommunicationUserIdentifier)event.getSender()).getId()
                    + " Version: " + event.getVersion()
                    + " CreatedOn: " + event.getCreatedOn()
                    + " DeletedOn: " + event.getDeletedOn()
                    + " RecipientMri: " + ((CommunicationUserIdentifier)event.getRecipient()).getId()
                );
            });

            chatAsyncClient.addPushNotificationHandler(CHAT_THREAD_CREATED, (ChatEvent payload) -> {
                Log.i(TAG, "Push Notification CHAT_THREAD_CREATED.");
                ChatThreadCreatedEvent event = (ChatThreadCreatedEvent) payload;
                Log.i(TAG, "Thread Created!"
                    + " ThreadId: " + event.getChatThreadId()
                    + " Properties_Id: " + event.getProperties().getId()
                    + " Properties_Topic: " + event.getProperties().getTopic()
                    + " Properties_CreatedOn: " + event.getProperties().getCreatedOn()
                    + " Properties_CreatedByMri: " + ((CommunicationUserIdentifier)event.getProperties().getCreatedByCommunicationIdentifier()).getId()
                    + " Participants_size: " + event.getParticipants().size()
                    + " Version: " + event.getVersion()
                    + " CreatedOn: " + event.getCreatedOn()
                    + " CreatedBy_DisplayName: " + event.getCreatedBy().getDisplayName()
                    + " CreatedBy_Mri: " + ((CommunicationUserIdentifier)event.getCreatedBy().getCommunicationIdentifier()).getId()
                );
            });

            chatAsyncClient.addPushNotificationHandler(CHAT_THREAD_PROPERTIES_UPDATED, (ChatEvent payload) -> {
                Log.i(TAG, "Push Notification CHAT_THREAD_PROPERTIES_UPDATED.");
                ChatThreadPropertiesUpdatedEvent event = (ChatThreadPropertiesUpdatedEvent) payload;
                Log.i(TAG, "Thread Updated!"
                    + " ThreadId: " + event.getChatThreadId()
                    + " Properties_Id: " + event.getProperties().getId()
                    + " Properties_Topic: " + event.getProperties().getTopic()
                    + " Version: " + event.getVersion()
                    + " UpdatedOn: " + event.getUpdatedOn()
                    + " UpdatedBy_DisplayName: " + event.getUpdatedBy().getDisplayName()
                    + " UpdatedBy_Mri: " + ((CommunicationUserIdentifier)event.getUpdatedBy().getCommunicationIdentifier()).getId()
                );
            });

            chatAsyncClient.addPushNotificationHandler(CHAT_THREAD_DELETED, (ChatEvent payload) -> {
                Log.i(TAG, "Push Notification CHAT_THREAD_DELETED.");
                ChatThreadDeletedEvent event = (ChatThreadDeletedEvent) payload;
                Log.i(TAG, "Thread Deleted!"
                    + " ThreadId: " + event.getChatThreadId()
                    + " Version: " + event.getVersion()
                    + " DeletedOn: " + event.getDeletedOn()
                    + " DeletedBy_DisplayName: " + event.getDeletedBy().getDisplayName()
                    + " DeletedBy_Mri: " + ((CommunicationUserIdentifier)event.getDeletedBy().getCommunicationIdentifier()).getId()
                );
            });

            chatAsyncClient.addPushNotificationHandler(PARTICIPANTS_ADDED, (ChatEvent payload) -> {
                Log.i(TAG, "Push Notification PARTICIPANTS_ADDED.");
                ParticipantsAddedEvent event = (ParticipantsAddedEvent) payload;
                Log.i(TAG, "Participant Added!"
                    + " ThreadId: " + event.getChatThreadId()
                    + " ParticipantsAdded_size: " + event.getParticipantsAdded().size()
                    + " Version: " + event.getVersion()
                    + " AddedOn: " + event.getAddedOn()
                    + " AddedBy_DisplayName: " + event.getAddedBy().getDisplayName()
                    + " AddedBy_Mri: " + ((CommunicationUserIdentifier)event.getAddedBy().getCommunicationIdentifier()).getId()
                );
            });

            chatAsyncClient.addPushNotificationHandler(PARTICIPANTS_REMOVED, (ChatEvent payload) -> {
                Log.i(TAG, "Push Notification PARTICIPANTS_REMOVED.");
                ParticipantsRemovedEvent event = (ParticipantsRemovedEvent) payload;
                Log.i(TAG, "Participant Removed!"
                    + " ThreadId: " + event.getChatThreadId()
                    + " ParticipantsRemoved_size: " + event.getParticipantsRemoved().size()
                    + " Version: " + event.getVersion()
                    + " RemovedOn: " + event.getRemovedOn()
                    + " RemovedBy_DisplayName: " + event.getRemovedBy().getDisplayName()
                    + " RemovedBy_Mri: " + ((CommunicationUserIdentifier)event.getRemovedBy().getCommunicationIdentifier()).getId()
                );
            });
        } catch (IllegalStateException error) {
            Log.i(TAG, "Push Notification not start yet.");
        }
    }

    public void stopPushNotification(View view) {
        logAndToast( "Stop push notification");
        try {
            stopFcmPushNotification();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }

    public void startRealTimeNotification(View view) {
        logAndToast( "Starting realtime notification");
        try {
            chatAsyncClient.startRealtimeNotifications(getApplicationContext());
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }

    public void registerRealTimeNotificationListener(View view) {
        logAndToast("Register a test listener");

        RealTimeNotificationCallback messageReceivedHandler = (ChatEvent payload) -> {
            eventHandlerCalled++;

            Log.i(TAG, eventHandlerCalled + " messages handled.");
            ChatMessageReceivedEvent event = (ChatMessageReceivedEvent) payload;
            Log.i(TAG, "Message created! ThreadId: " + event.getChatThreadId());
        };
        chatAsyncClient.addEventHandler(CHAT_MESSAGE_RECEIVED, messageReceivedHandler);
        realTimeNotificationCallbacks.put(messageReceivedHandler, CHAT_MESSAGE_RECEIVED);

        RealTimeNotificationCallback messageEditedHandler = (ChatEvent payload) -> {
            eventHandlerCalled++;

            Log.i(TAG, eventHandlerCalled + " messages handled.");
            ChatMessageEditedEvent event = (ChatMessageEditedEvent) payload;
            Log.i(TAG, "Message edited! ThreadId: " + event.getChatThreadId());
        };
        chatAsyncClient.addEventHandler(CHAT_MESSAGE_EDITED, messageEditedHandler);
        realTimeNotificationCallbacks.put(messageEditedHandler, CHAT_MESSAGE_EDITED);

        RealTimeNotificationCallback messageDeletedHandler = (ChatEvent payload) -> {
            eventHandlerCalled++;

            Log.i(TAG, eventHandlerCalled + " messages handled.");
            ChatMessageDeletedEvent event = (ChatMessageDeletedEvent) payload;
            Log.i(TAG, "Message deleted! ThreadId: " + event.getChatThreadId());
        };
        chatAsyncClient.addEventHandler(CHAT_MESSAGE_DELETED, messageDeletedHandler);
        realTimeNotificationCallbacks.put(messageDeletedHandler, CHAT_MESSAGE_DELETED);

        RealTimeNotificationCallback typingIndicatorHandler = (ChatEvent payload) -> {
            eventHandlerCalled++;

            Log.i(TAG, eventHandlerCalled + " messages handled.");
            TypingIndicatorReceivedEvent event = (TypingIndicatorReceivedEvent) payload;
            Log.i(TAG, "Typing indicator received! ThreadId: " + event.getChatThreadId());
        };
        chatAsyncClient.addEventHandler(TYPING_INDICATOR_RECEIVED, typingIndicatorHandler);
        realTimeNotificationCallbacks.put(typingIndicatorHandler, TYPING_INDICATOR_RECEIVED);

        RealTimeNotificationCallback readReceiptHandler = (ChatEvent payload) -> {
            eventHandlerCalled++;

            Log.i(TAG, eventHandlerCalled + " messages handled.");
            ReadReceiptReceivedEvent event = (ReadReceiptReceivedEvent) payload;
            Log.i(TAG, "Read receipt received! ThreadId: " + event.getChatThreadId());
        };
        chatAsyncClient.addEventHandler(READ_RECEIPT_RECEIVED, readReceiptHandler);
        realTimeNotificationCallbacks.put(readReceiptHandler, READ_RECEIPT_RECEIVED);

        RealTimeNotificationCallback threadCreatedHandler = (ChatEvent payload) -> {
            eventHandlerCalled++;

            Log.i(TAG, eventHandlerCalled + " messages handled.");
            ChatThreadCreatedEvent event = (ChatThreadCreatedEvent) payload;
            Log.i(TAG, "Chat thread created! ThreadId: " + event.getChatThreadId());
        };
        chatAsyncClient.addEventHandler(CHAT_THREAD_CREATED, threadCreatedHandler);
        realTimeNotificationCallbacks.put(threadCreatedHandler, CHAT_THREAD_CREATED);

        RealTimeNotificationCallback threadDeletedHandler = (ChatEvent payload) -> {
            eventHandlerCalled++;

            Log.i(TAG, eventHandlerCalled + " messages handled.");
            ChatThreadDeletedEvent event = (ChatThreadDeletedEvent) payload;
            Log.i(TAG, "Chat thread deleted! ThreadId: " + event.getChatThreadId());
        };
        chatAsyncClient.addEventHandler(CHAT_THREAD_DELETED, threadDeletedHandler);
        realTimeNotificationCallbacks.put(threadDeletedHandler, CHAT_THREAD_DELETED);

        RealTimeNotificationCallback threadUpdatedHandler = (ChatEvent payload) -> {
            eventHandlerCalled++;

            Log.i(TAG, eventHandlerCalled + " messages handled.");
            ChatThreadPropertiesUpdatedEvent event = (ChatThreadPropertiesUpdatedEvent) payload;
            Log.i(TAG, "Chat thread properties updated! ThreadId: " + event.getChatThreadId());
        };
        chatAsyncClient.addEventHandler(CHAT_THREAD_PROPERTIES_UPDATED, threadUpdatedHandler);
        realTimeNotificationCallbacks.put(threadUpdatedHandler, CHAT_THREAD_PROPERTIES_UPDATED);

        RealTimeNotificationCallback participantAddedHandler = (ChatEvent payload) -> {
            eventHandlerCalled++;

            Log.i(TAG, eventHandlerCalled + " messages handled.");
            ParticipantsAddedEvent event = (ParticipantsAddedEvent) payload;
            Log.i(TAG, "Participants added! ThreadId: " + event.getChatThreadId());
        };
        chatAsyncClient.addEventHandler(PARTICIPANTS_ADDED, participantAddedHandler);
        realTimeNotificationCallbacks.put(participantAddedHandler, PARTICIPANTS_ADDED);

        RealTimeNotificationCallback participantRemovedHandler = (ChatEvent payload) -> {
            eventHandlerCalled++;

            Log.i(TAG, eventHandlerCalled + " messages handled.");
            ParticipantsRemovedEvent event = (ParticipantsRemovedEvent) payload;
            Log.i(TAG, "Participants removed! ThreadId: " + event.getChatThreadId());
        };
        chatAsyncClient.addEventHandler(PARTICIPANTS_REMOVED, participantRemovedHandler);
        realTimeNotificationCallbacks.put(participantRemovedHandler, PARTICIPANTS_REMOVED);
    }

    public void unregisterRealTimeNotificationListener(View view) {
        logAndToast("Unregister realtime notification listeners");
        for (Map.Entry<RealTimeNotificationCallback, ChatEventType> entry: realTimeNotificationCallbacks.entrySet()) {
            chatAsyncClient.removeEventHandler(entry.getValue(), entry.getKey());
        }
        realTimeNotificationCallbacks.clear();
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

    private void startFcmPushNotification() {
        FirebaseMessaging.getInstance().getToken()
            .addOnCompleteListener(new OnCompleteListener<String>() {
                @Override
                public void onComplete(@NonNull Task<String> task) {
                    if (!task.isSuccessful()) {
                        Log.w(TAG, "Fetching FCM registration token failed", task.getException());
                        return;
                    }

                    // Get new FCM registration token
                    String token = task.getResult();

                    // Log and toast
                    Log.d(TAG, "Fcm push token generated:" + token);
                    Toast.makeText(MainActivity.this, token, Toast.LENGTH_SHORT).show();

                    chatAsyncClient.startPushNotifications(token);
                }
            });
    }

    private void stopFcmPushNotification() {
        chatAsyncClient.stopPushNotifications();
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
