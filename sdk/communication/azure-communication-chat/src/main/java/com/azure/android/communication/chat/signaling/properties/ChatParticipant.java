// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.communication.chat.signaling.properties;

import com.azure.android.communication.common.CommunicationIdentifier;

/**
 * An Azure Communication chat participant.
 */
public class ChatParticipant {
    /**
     * The user identity of the event initiator.
     * in the format `8:acs...`.
     */
    private CommunicationIdentifier user;

    /**
     * The display name of the event initiator.
     */
    private String displayName;

    /**
     * Time from which the chat history is shared with the chat participant.
     * The timestamp is in ISO8601 format: `yyyy-MM-ddTHH:mm:ssZ`.
     */
    private String shareHistoryTime;


    /**
     * Sets new The user identity of the event initiator.
     * in the format `8:acs...`..
     *
     * @param user New value of The user identity of the event initiator.
     *             in the format `8:acs...`..
     */
    public void setUser(CommunicationIdentifier user) {
        this.user = user;
    }

    /**
     * Sets new The display name of the event initiator..
     *
     * @param displayName New value of The display name of the event initiator..
     */
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Gets The display name of the event initiator..
     *
     * @return Value of The display name of the event initiator..
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Sets new Time from which the chat history is shared with the chat participant.
     * The timestamp is in ISO8601 format: `yyyy-MM-ddTHH:mm:ssZ`..
     *
     * @param shareHistoryTime New value of Time from which the chat history is shared with the chat participant.
     *                         The timestamp is in ISO8601 format: `yyyy-MM-ddTHH:mm:ssZ`..
     */
    public void setShareHistoryTime(String shareHistoryTime) {
        this.shareHistoryTime = shareHistoryTime;
    }

    /**
     * Gets The user identity of the event initiator.
     * in the format `8:acs...`..
     *
     * @return Value of The user identity of the event initiator.
     * in the format `8:acs...`..
     */
    public CommunicationIdentifier getUser() {
        return user;
    }

    /**
     * Gets Time from which the chat history is shared with the chat participant.
     * The timestamp is in ISO8601 format: `yyyy-MM-ddTHH:mm:ssZ`..
     *
     * @return Value of Time from which the chat history is shared with the chat participant.
     * The timestamp is in ISO8601 format: `yyyy-MM-ddTHH:mm:ssZ`..
     */
    public String getShareHistoryTime() {
        return shareHistoryTime;
    }
}
