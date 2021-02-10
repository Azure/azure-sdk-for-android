package com.azure.android.communication.chat.signaling;

public interface SignalingClient {
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
     */
    void on(String chatEventId, RealTimeNotificationCallback listener);
    /**
     * Stop listening to Chat events.
     */
    void off(String chatEventId, RealTimeNotificationCallback listener);
}
