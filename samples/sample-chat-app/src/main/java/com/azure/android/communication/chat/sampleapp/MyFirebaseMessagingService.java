// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.communication.chat.sampleapp;

import android.content.Intent;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.azure.android.communication.chat.models.ChatPushNotification;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.concurrent.Semaphore;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private static final String TAG = "MyFirebaseMsgService";
    public static Semaphore initCompleted = new Semaphore(1);

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        try {
            Log.d(TAG, "Incoming push notification.");

            initCompleted.acquire();

            if (remoteMessage.getData().size() > 0) {
                ChatPushNotification chatPushNotification =
                    new ChatPushNotification().setPayload(remoteMessage.getData());
                sendPushNotificationToActivity(chatPushNotification);
            }

            initCompleted.release();
        } catch (InterruptedException e) {
            Log.e(TAG, "Error receiving push notification.");
        }
    }

    private void sendPushNotificationToActivity(ChatPushNotification chatPushNotification) {
        Log.d(TAG, "Passing push notification to Activity: " + chatPushNotification.getPayload());
        Intent intent = new Intent("com.azure.android.communication.chat.sampleapp.pushnotification");
        intent.putExtra("PushNotificationPayload", chatPushNotification);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }
}