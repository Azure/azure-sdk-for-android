// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.communication.chat.implementation.notifications.signaling;

import android.content.Context;

import com.azure.android.communication.chat.models.ChatEventType;
import com.azure.android.communication.chat.models.RealTimeNotificationCallback;

/**
 * The signaling client interface
 */
public interface SignalingClient {

    /**
     * flag to indicate if signaling client has started
     * @return boolean if signaling client has started
     */
    boolean hasStarted();

    /**
     * Start the realtime connection.
     * @param context the android application context
     */
    void start(Context context);
    /**
     * Stop the realtime connection and unsubscribe all event handlers.
     */
    void stop();

    /**
     * Listen to Chat events.
     * @param chatEventType the chat event kind
     * @param listener the listener callback function
     */
    void on(ChatEventType chatEventType, RealTimeNotificationCallback listener);

    /**
     * Stop listening to Chat events.
     * @param chatEventType the chat event kind
     * @param listener the listener callback function
     */
    void off(ChatEventType chatEventType, RealTimeNotificationCallback listener);
}
