// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.communication.chat.models;

import com.azure.android.communication.chat.implementation.signaling.EventAccessorHelper;
import com.azure.android.communication.chat.implementation.signaling.TrouterUtils;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.json.JSONException;
import org.json.JSONObject;
import org.threeten.bp.OffsetDateTime;

/**
 * Event for a received read receipt.
 */
public final class ReadReceiptReceivedEvent extends ChatUserEvent {
    /**
     * The id of the last read chat message.
     */
    @JsonProperty(value = "messageId")
    private String chatMessageId;

    /**
     * The timestamp when the message was read. The timestamp is in RFC3339 format: yyyy-MM-ddTHH:mm:ssZ
     */
    @JsonProperty(value = "readOn")
    private OffsetDateTime readOn;

    /**
     * The message body which contains consumption horizon for the read receipt.
     * A serialized JSON string property in notification payload.
     */
    @JsonProperty(value = "messageBody", access = JsonProperty.Access.WRITE_ONLY)
    private String messageBody;

    static {
        EventAccessorHelper.setReadReceiptReceivedEventAccessor(event -> {
            ReadReceiptReceivedEvent readReceiptReceivedEvent = (ReadReceiptReceivedEvent) event;
            readReceiptReceivedEvent
                .setReadOn()
                .setSender()
                .setRecipient()
                .setThreadId();
        });
    }

    /**
     * Gets the id of the last read chat message.
     *
     * @return Value of The id of the last read chat message.
     */
    public String getChatMessageId() {
        return chatMessageId;
    }

    /**
     * Gets the timestamp when the message was read. The timestamp is in RFC3339 format: yyyy-MM-ddTHH:mm:ssZ.
     *
     * @return Value of The timestamp when the message was read.
     *         The timestamp is in RFC3339 format: yyyy-MM-ddTHH:mm:ssZ.
     */
    public OffsetDateTime getReadOn() {
        return readOn;
    }

    /**
     * Sets the timestamp when the message was read.
     */
    ReadReceiptReceivedEvent setReadOn() {
        try {
            JSONObject messageBodyJsonObject = new JSONObject(this.messageBody);
            this.readOn = TrouterUtils.extractReadTimeFromConsumptionHorizon(
                messageBodyJsonObject.getString("consumptionhorizon"));

        } catch (JSONException e) {
            return this;
        }

        return this;
    }
}
