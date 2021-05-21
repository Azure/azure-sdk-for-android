// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.communication.chat;

import com.azure.android.communication.chat.handwritten.models.AddChatParticipantsOptions;
import com.azure.android.communication.chat.handwritten.models.ChatMessage;

import com.azure.android.communication.chat.handwritten.models.ChatThread;
import com.azure.android.communication.chat.handwritten.models.ChatParticipant;
import com.azure.android.communication.chat.handwritten.models.ChatMessageReadReceipt;
import com.azure.android.communication.chat.handwritten.models.CreateChatThreadOptions;
import com.azure.android.communication.chat.handwritten.models.CreateChatThreadResult;
import com.azure.android.communication.chat.handwritten.models.SendChatMessageOptions;
import com.azure.android.communication.chat.handwritten.models.UpdateChatMessageOptions;
import com.azure.android.communication.common.CommunicationUserIdentifier;
import com.azure.android.communication.common.CommunicationTokenCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.netty.NettyAsyncHttpClientBuilder;
import com.azure.core.http.rest.PagedIterable;

import java.util.ArrayList;
import java.util.List;

/**
 * WARNING: MODIFYING THIS FILE WILL REQUIRE CORRESPONDING UPDATES TO README.md FILE. LINE NUMBERS
 * ARE USED TO EXTRACT APPROPRIATE CODE SEGMENTS FROM THIS FILE. ADD NEW CODE AT THE BOTTOM TO AVOID CHANGING
 * LINE NUMBERS OF EXISTING CODE SAMPLES.
 *
 * Class containing code snippets that will be injected to README.md.
 */

public class ReadmeSamples {

    /**
     * Sample code for creating a sync chat client.
     *
     * @return the chat client.
     */
    public ChatClient createChatClient() {
        String endpoint = "https://<RESOURCE_NAME>.communcationservices.azure.com";

        // Create an HttpClient builder of your choice and customize it
        // Use com.azure.core.http.netty.NettyAsyncHttpClientBuilder if that suits your needs
        NettyAsyncHttpClientBuilder httpClientBuilder = new NettyAsyncHttpClientBuilder();
        HttpClient httpClient = httpClientBuilder.build();

        // Your user access token retrieved from your trusted service
        String token = "SECRET";
        CommunicationTokenCredential credential = new CommunicationTokenCredential(token);

        // Initialize the chat client
        final ChatClientBuilder builder = new ChatClientBuilder();
        builder.endpoint(endpoint)
            .credential(credential)
            .httpClient(httpClient);
        ChatClient chatClient = builder.buildClient();

        return chatClient;
    }

    /**
     * Sample code for creating a chat thread using the sync chat client.
     */
    public void createChatThread() {
        ChatClient chatClient = createChatClient();

        CommunicationUserIdentifier user1 = new CommunicationUserIdentifier("Id 1");
        CommunicationUserIdentifier user2 = new CommunicationUserIdentifier("Id 2");

        List<ChatParticipant> participants = new ArrayList<ChatParticipant>();

        ChatParticipant firstParticipant = new ChatParticipant()
            .setCommunicationIdentifier(user1)
            .setDisplayName("Participant Display Name 1");

        ChatParticipant secondParticipant = new ChatParticipant()
            .setCommunicationIdentifier(user2)
            .setDisplayName("Participant Display Name 2");

        participants.add(firstParticipant);
        participants.add(secondParticipant);

        CreateChatThreadOptions createChatThreadOptions = new CreateChatThreadOptions()
            .setTopic("Topic")
            .setParticipants(participants);
        CreateChatThreadResult result = chatClient.createChatThread(createChatThreadOptions);
        String chatThreadId = result.getChatThread().getId();
    }

    /**
     * Sample code for getting a chat thread using the sync chat client.
     */
    public void getChatThread() {
        ChatClient chatClient = createChatClient();
        String chatThreadId = "Id";
        ChatThreadClient chatThreadClient = chatClient.getChatThreadClient(chatThreadId);
        ChatThreadProperties chatThreadProperties = chatThreadClient.getProperties();
    }

    /**
     * Sample code for deleting a chat thread using the sync chat client.
     */
    public void deleteChatThread() {
        ChatClient chatClient = createChatClient();

        String chatThreadId = "Id";
        chatClient.deleteChatThread(chatThreadId);
    }

    /**
     * Sample code for getting a sync chat thread client using the sync chat client.
     *
     * @return the chat thread client.
     */
    public ChatThreadClient getChatThreadClient() {
        ChatClient chatClient = createChatClient();

        String chatThreadId = "Id";
        ChatThreadClient chatThreadClient = chatClient.getChatThreadClient(chatThreadId);

        return chatThreadClient;
    }

    /**
     * Sample code for updating a chat thread topic using the sync chat thread client.
     */
    public void updateTopic() {
        ChatThreadClient chatThreadClient = getChatThreadClient();

        chatThreadClient.updateTopic("New Topic");
    }



    /**
     * Sample code for sending a chat message using the sync chat thread client.
     */
    public void sendChatMessage() {

        ChatThreadClient chatThreadClient = getChatThreadClient();

        SendChatMessageOptions sendChatMessageOptions = new SendChatMessageOptions()
            .setContent("Message content")
            .setSenderDisplayName("Sender Display Name");


        String chatMessageId = chatThreadClient.sendMessage(sendChatMessageOptions).getId();
    }

    /**
     * Sample code for getting a chat message using the sync chat thread client.
     */
    public void getChatMessage() {
        ChatThreadClient chatThreadClient = getChatThreadClient();

        String chatMessageId = "Id";
        ChatMessage chatMessage = chatThreadClient.getMessage(chatMessageId);
    }

    /**
     * Sample code getting the thread messages using the sync chat thread client.
     */
    public void getChatMessages() {
        ChatThreadClient chatThreadClient = getChatThreadClient();

        Page<ChatMessage> chatMessagesResponse = chatThreadClient.getMessagesFirstPage();
        List<ChatMessage> chatMessages = chatMessagesResponse.getElements();
        String nextLink = chatMessagesResponse.getContinuationToken();
        while (nextLink != null) {
            chatMessagesResponse = chatThreadClient.getMessagesNextPage(nextLink);
            chatMessages = chatMessagesResponse.getElements();
            nextLink = chatMessagesResponse.getContinuationToken();
        }
    }

    /**
     * Sample code updating a thread message using the sync chat thread client.
     */
    public void updateChatMessage() {
        ChatThreadClient chatThreadClient = getChatThreadClient();

        String chatMessageId = "Id";
        UpdateChatMessageOptions updateChatMessageOptions = new UpdateChatMessageOptions()
            .setContent("Updated message content");

        chatThreadClient.updateMessage(chatMessageId, updateChatMessageOptions);
    }

    /**
     * Sample code deleting a thread message using the sync chat thread client.
     */
    public void deleteChatMessage() {
        ChatThreadClient chatThreadClient = getChatThreadClient();

        String chatMessageId = "Id";
        chatThreadClient.deleteMessage(chatMessageId);
    }

    /**
     * Sample code listing chat participants using the sync chat thread client.
     */
    public void listChatParticipants() {
        ChatThreadClient chatThreadClient = getChatThreadClient();

        Page<ChatParticipant> chatParticipantsResponse = chatThreadClient.getParticipantsFirstPage();
        List<ChatParticipant> chatParticipants = chatParticipantsResponse.getElements();
        String nextLink = chatParticipantsResponse.getContinuationToken();
        while (nextLink != null) {
            chatParticipantsResponse = chatThreadClient.getParticipantsNextPage(nextLink);
            chatParticipants = chatParticipantsResponse.getElements();
            nextLink = chatParticipantsResponse.getContinuationToken();
        }
    }

    /**
     * Sample code adding chat participants using the sync chat thread client.
     */
    public void addChatParticipants() {
        ChatThreadClient chatThreadClient = getChatThreadClient();

        CommunicationUserIdentifier user1 = new CommunicationUserIdentifier("Id 1");
        CommunicationUserIdentifier user2 = new CommunicationUserIdentifier("Id 2");

        List<ChatParticipant> participants = new ArrayList<ChatParticipant>();

        ChatParticipant firstParticipant = new ChatParticipant()
            .setCommunicationIdentifier(user1)
            .setDisplayName("Display Name 1");

        ChatParticipant secondParticipant = new ChatParticipant()
            .setCommunicationIdentifier(user2)
            .setDisplayName("Display Name 2");

        participants.add(firstParticipant);
        participants.add(secondParticipant);

        AddChatParticipantsOptions addChatParticipantsOptions = new AddChatParticipantsOptions()
            .setParticipants(participants);
        chatThreadClient.addParticipants(addChatParticipantsOptions);
    }

    /**
     * Sample code removing a chat participant using the sync chat thread client.
     */
    public void removeChatParticipant() {
        ChatThreadClient chatThreadClient = getChatThreadClient();

        CommunicationUserIdentifier identifier = new CommunicationUserIdentifier("Id");

        chatThreadClient.removeParticipant(identifier);
    }

    /**
     * Sample code sending a read receipt using the sync chat thread client.
     */
    public void sendReadReceipt() {
        ChatThreadClient chatThreadClient = getChatThreadClient();

        String chatMessageId = "Id";
        chatThreadClient.sendReadReceipt(chatMessageId);
    }

    /**
     * Sample code listing read receipts using the sync chat thread client.
     */
    public void listReadReceipts() {
        ChatThreadClient chatThreadClient = getChatThreadClient();

        Page<ChatMessageReadReceipt> readReceiptsResponse = chatThreadClient.getReadReceiptsFirstPage();
        List<ChatMessageReadReceipt> chatMessageReadReceipts = readReceiptsResponse.getElements();
        String nextLink = readReceiptsResponse.getContinuationToken();
        while (nextLink != null) {
            readReceiptsResponse = chatThreadClient.getReadReceiptsNextPage(nextLink);
            chatMessageReadReceipts = readReceiptsResponse.getElements();
            nextLink = readReceiptsResponse.getContinuationToken();
        }
    }

    /**
     * Sample code sending a read receipt using the sync chat thread client.
     */
    public void sendTypingNotification() {
        ChatThreadClient chatThreadClient = getChatThreadClient();

        chatThreadClient.sendTypingNotification();
    }
}
