// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.communication.chat.models;

import com.azure.android.core.rest.annotation.Fluent;

import java.io.Serializable;
import java.util.Map;

/**
 * The chat push notification payload from FCM.
 */
@Fluent
public class ChatPushNotification implements Serializable {
    /**
     * The payload for incoming chat push notification.
     */
    private Map<String, String> payload;

    /**
     * Get the push notification payload.
     * @return push notification payload.
     */
    public Map<String, String> getPayload() {
        return this.payload;
    }

    /**
     * Set the push notification payload.
     * @param payload push notification payload.
     * @return the ChatPushNotification object itself.
     */
    public ChatPushNotification setPayload(Map<String, String> payload) {
        this.payload = payload;
        return this;
    }
}
