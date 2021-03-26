// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.communication.chat.signaling.chatevents;

public abstract class BaseEvent {
    /**
     * Thread Id of the event.
     */
    private String threadId;

    /**
     * Gets Thread Id of the event..
     *
     * @return Value of Thread Id of the event..
     */
    public String getThreadId() {
        return threadId;
    }

    /**
     * Sets new Thread Id of the event..
     *
     * @param threadId New value of Thread Id of the event..
     */
    public void setThreadId(String threadId) {
        this.threadId = threadId;
    }
}
