package com.azure.android.communication.chat.signaling.chatevents;

public class ChatThreadEvent {
    /**
     * Thread Id of the event.
     */
    String threadId;

    /**
     * Version of the thread. This version is an epoch time in a numeric unsigned Int64 format:
     * `1593117207131`
     */
    String version;
}
