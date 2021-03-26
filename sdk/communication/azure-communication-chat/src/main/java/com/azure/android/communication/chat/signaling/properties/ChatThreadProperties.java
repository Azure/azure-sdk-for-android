// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.communication.chat.signaling.properties;

/**
 * Properties of an Azure Communication chat thread.
 */
public class ChatThreadProperties {
    /**
     * Thread topic.
     */
    private String topic;


    /**
     * Sets new Thread topic..
     *
     * @param topic New value of Thread topic..
     */
    public void setTopic(String topic) {
        this.topic = topic;
    }

    /**
     * Gets Thread topic..
     *
     * @return Value of Thread topic..
     */
    public String getTopic() {
        return topic;
    }
}
