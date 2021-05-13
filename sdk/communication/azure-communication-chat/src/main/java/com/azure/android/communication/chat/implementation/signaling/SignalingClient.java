// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.communication.chat.implementation.signaling;

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
     */
    void start();
    /**
     * Stop the realtime connection and unsubscribe all event handlers.
     */
    void stop();

    /**
     * Listen to Chat events.
     * @param chatEventType the chat event kind
     * @param listenerId a listener id that is used to identify the listner
     * @param listener the listener callback function
     */
    void on(ChatEventType chatEventType, String listenerId, RealTimeNotificationCallback listener);

    /**
     * Stop listening to Chat events.
     * @param chatEventType the chat event kind
     * @param listenerId the listener id that is to off
     */
    void off(ChatEventType chatEventType, String listenerId);
}
