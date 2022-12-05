// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.communication.chat.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.azure.android.core.rest.annotation.Fluent;

import java.util.HashMap;
import java.util.Map;

/**
 * The chat push notification payload from FCM.
 */
@Fluent
public final class ChatPushNotification implements Parcelable {
    /**
     * The payload for incoming chat push notification.
     */
    private Map<String, String> payload;

    /**
     * Constructs a new ChatPushNotification
     */
    public ChatPushNotification() { }

    /**
     * Constructs a new ChatPushNotification
     * @param in Parcel model to construct ChatPushNotification
     */
    protected ChatPushNotification(Parcel in) {
        int size = in.readInt();
        this.payload = new HashMap<>(size);
        for (int i = 0; i < size; i++) {
            String key = in.readString();
            String value = in.readString();
            this.payload.put(key, value);
        }
    }

    /**
     * {@inheritdoc}
     */
    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(this.payload.size());
        for (Map.Entry<String, String> entry : this.payload.entrySet()) {
            out.writeString(entry.getKey());
            out.writeString(entry.getValue());
        }
    }

    /**
     * {@inheritdoc}
     */
    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<ChatPushNotification> CREATOR = new Creator<ChatPushNotification>() {
        @Override
        public ChatPushNotification createFromParcel(Parcel in) {
            return new ChatPushNotification(in);
        }

        @Override
        public ChatPushNotification[] newArray(int size) {
            return new ChatPushNotification[size];
        }
    };

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