// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.communication.chat.signaling.chatevents;

/**
 * Chat thread event
 */
public abstract class ChatThreadEvent extends BaseEvent {


    /**
     * Version of the thread. This version is an epoch time in a numeric unsigned Int64 format:
     * `1593117207131`
     */
    private String version;


    /**
     * Sets new Version of the thread. This version is an epoch time in a numeric unsigned Int64 format:
     * `1593117207131`.
     *
     * @param version New value of Version of the thread.
     *                This version is an epoch time in a numeric unsigned Int64 format: `1593117207131`.
     */
    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * Gets Version of the thread. This version is an epoch time in a numeric unsigned Int64 format:
     * `1593117207131`.
     *
     * @return Value of Version of the thread. This version is an epoch time in a numeric unsigned Int64 format:
     * `1593117207131`.
     */
    public String getVersion() {
        return version;
    }
}
