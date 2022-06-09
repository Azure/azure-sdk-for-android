// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.sampleapp;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.azure.android.core.util.events.EventHandler;

public class MainActivity extends AppCompatActivity {
    MessagingClient client;
    EventHandler<MessagingClient.MessageSentEvent> messageSentHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        client = new MessagingClient();
        messageSentHandler = event ->
            Toast.makeText(this, "Sent message with id: " + event.getMessageId(), Toast.LENGTH_SHORT).show();

        client.addOnMessageSentEventHandler(messageSentHandler);
        client.addOnMessageReceivedEventHandler((MessagingClient.MessageReceivedEvent event) ->
            Toast.makeText(this, "Received message with id: " + event.getMessageId() + "and contents: "
                + event.getContents(), Toast.LENGTH_SHORT).show());
    }

    public void sendMessage(View view) {
        client.sendMessage();
    }

    public void receiveMessage(View view) {
        client.receiveMessage();
    }

    public void removeMessageSentEventHandler(View view) {
        client.removeOnMessageSentEventHandler(messageSentHandler);
    }
}
