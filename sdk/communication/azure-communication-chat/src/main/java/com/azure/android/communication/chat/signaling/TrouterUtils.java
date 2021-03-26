// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.communication.chat.signaling;

import com.azure.android.communication.chat.signaling.chatevents.BaseEvent;
import com.azure.android.communication.chat.signaling.chatevents.ChatMessageDeletedEvent;
import com.azure.android.communication.chat.signaling.chatevents.ChatMessageEditedEvent;
import com.azure.android.communication.chat.signaling.chatevents.ChatMessageReceivedEvent;
import com.azure.android.communication.chat.signaling.chatevents.ChatThreadCreatedEvent;
import com.azure.android.communication.chat.signaling.chatevents.ChatThreadDeletedEvent;
import com.azure.android.communication.chat.signaling.chatevents.ChatThreadPropertiesUpdatedEvent;
import com.azure.android.communication.chat.signaling.chatevents.ParticipantsAddedEvent;
import com.azure.android.communication.chat.signaling.chatevents.ParticipantsRemovedEvent;
import com.azure.android.communication.chat.signaling.chatevents.ReadReceiptReceivedEvent;
import com.azure.android.communication.chat.signaling.chatevents.TypingIndicatorReceivedEvent;
import com.azure.android.communication.chat.signaling.properties.ChatEventId;
import com.azure.android.communication.chat.signaling.properties.ChatParticipant;
import com.azure.android.communication.chat.signaling.properties.ChatThreadProperties;
import com.azure.android.communication.common.CommunicationCloudEnvironment;
import com.azure.android.communication.common.CommunicationIdentifier;
import com.azure.android.communication.common.CommunicationUserIdentifier;
import com.azure.android.communication.common.MicrosoftTeamsUserIdentifier;
import com.azure.android.communication.common.PhoneNumberIdentifier;
import com.azure.android.communication.common.UnknownIdentifier;
import com.azure.android.core.logging.ClientLogger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class TrouterUtils {

    private static final ClientLogger CLIENT_LOGGER = new ClientLogger(TrouterUtils.class);
    /**
     * Mapping chat event id to trouter event id code
     */
    static final Map<ChatEventId, Integer> EVENT_IDS_MAPPING = new HashMap<ChatEventId, Integer>() {{
            put(ChatEventId.chatMessageReceived, 200);
            put(ChatEventId.typingIndicatorReceived, 245);
            put(ChatEventId.readReceiptReceived, 246);
            put(ChatEventId.chatMessageEdited, 247);
            put(ChatEventId.chatMessageDeleted, 248);
            put(ChatEventId.chatThreadCreated, 257);
            put(ChatEventId.chatThreadPropertiesUpdated, 258);
            put(ChatEventId.chatThreadDeleted, 259);
            put(ChatEventId.participantsAdded, 260);
            put(ChatEventId.participantsRemoved, 261);
        }};

    public static BaseEvent toMessageHandler(ChatEventId chatEventId, String responseBody) {
        int eventId = 0;
        JSONObject genericPayload = null;
        try {
            eventId = EVENT_IDS_MAPPING.get(chatEventId);
            genericPayload = new JSONObject(responseBody);
        } catch (Exception e) {
            CLIENT_LOGGER.error(e.getMessage());
        }

        try {
            if (genericPayload == null || genericPayload.getInt("_eventId") != eventId) {
                return null;
            }
        } catch (JSONException e) {
            CLIENT_LOGGER.error(e.getMessage());
        }

        return toEventPayload(chatEventId, genericPayload);
    }

    public static BaseEvent toEventPayload(ChatEventId eventId, JSONObject payload) {
        switch (eventId) {
            case chatMessageReceived:
                return getChatMessageReceived(payload);
            case typingIndicatorReceived:
                return getTypingIndicatorReceived(payload);
            case readReceiptReceived:
                return getReadReceiptReceived(payload);
            case chatMessageEdited:
                return getChatMessageEdited(payload);
            case chatMessageDeleted:
                return getChatMessageDeleted(payload);
            case chatThreadCreated:
                return getChatThreadCreated(payload);
            case chatThreadPropertiesUpdated:
                return getChatThreadPropertiesUpdated(payload);
            case chatThreadDeleted:
                return getChatThreadDeleted(payload);
            case participantsAdded:
                return getParticipantsAdded(payload);
            case participantsRemoved:
                return getParticipantsRemoved(payload);
            default:
                return null;
        }
    }

    private static BaseEvent getParticipantsRemoved(JSONObject payload) {
        ParticipantsRemovedEvent eventPayload = new ParticipantsRemovedEvent();

        try {
            eventPayload.setThreadId(payload.getString("threadId"));

            ChatParticipant removedBy = new ChatParticipant();
            removedBy.setUser(
                getUserIdentifier(getJSONObject(payload, "removedBy").getString("participantId")));
            removedBy.setDisplayName(getJSONObject(payload, "removedBy").getString("displayName"));
            eventPayload.setRemovedBy(removedBy);

            List<ChatParticipant> chatParticipants = new ArrayList<>();
            JSONArray members = getJSONArray(payload, "participantsRemoved");
            for (int i = 0; i < members.length(); i++) {
                JSONObject member = members.getJSONObject(i);

                CommunicationIdentifier communicationUser =
                    getUserIdentifier(member.getString("participantId"));
                ChatParticipant chatParticipant = new ChatParticipant();
                chatParticipant.setUser(communicationUser);
                chatParticipant.setDisplayName(member.getString("displayName"));
                chatParticipant.setShareHistoryTime(new Date(member.getString("shareHistoryTime")).toString());

                chatParticipants.add(chatParticipant);
            }

            eventPayload.setRemovedOn(payload.getString("time"));
            eventPayload.setVersion(payload.getString("version"));
            eventPayload.setParticipantsRemoved(chatParticipants);
        } catch (JSONException e) {
            CLIENT_LOGGER.error(e.getMessage());
        }

        return eventPayload;
    }

    private static BaseEvent getParticipantsAdded(JSONObject payload) {
        ParticipantsAddedEvent eventPayload = new ParticipantsAddedEvent();

        try {
            eventPayload.setThreadId(payload.getString("threadId"));

            ChatParticipant addedBy = new ChatParticipant();
            addedBy.setUser(
                getUserIdentifier(getJSONObject(payload, "addedBy").getString("participantId")));
            addedBy.setDisplayName(getJSONObject(payload, "addedBy").getString("displayName"));
            eventPayload.setAddedBy(addedBy);

            List<ChatParticipant> chatParticipants = new ArrayList<>();
            JSONArray members = getJSONArray(payload, "participantsAdded");
            for (int i = 0; i < members.length(); i++) {
                JSONObject member = members.getJSONObject(i);

                CommunicationIdentifier communicationUser =
                    getUserIdentifier(member.getString("participantId"));
                ChatParticipant chatParticipant = new ChatParticipant();
                chatParticipant.setUser(communicationUser);
                chatParticipant.setDisplayName(member.getString("displayName"));
                chatParticipant.setShareHistoryTime(new Date(member.getString("shareHistoryTime")).toString());

                chatParticipants.add(chatParticipant);
            }

            eventPayload.setAddedOn(payload.getString("time"));
            eventPayload.setVersion(payload.getString("version"));
            eventPayload.setParticipantsAdded(chatParticipants);
        } catch (JSONException e) {
            CLIENT_LOGGER.error(e.getMessage());
        }

        return eventPayload;
    }

    private static BaseEvent getChatThreadDeleted(JSONObject payload) {
        ChatThreadDeletedEvent eventPayload = new ChatThreadDeletedEvent();

        try {
            eventPayload.setThreadId(payload.getString("threadId"));

            ChatParticipant deletedBy = new ChatParticipant();
            deletedBy.setUser(
                getUserIdentifier(getJSONObject(payload, "deletedBy").getString("participantId")));
            deletedBy.setDisplayName(getJSONObject(payload, "deletedBy").getString("displayName"));
            eventPayload.setDeletedBy(deletedBy);

            eventPayload.setDeletedOn(payload.getString("deleteTime"));
            eventPayload.setVersion(payload.getString("version"));
        } catch (JSONException e) {
            CLIENT_LOGGER.error(e.getMessage());
        }

        return eventPayload;
    }

    private static BaseEvent getChatThreadPropertiesUpdated(JSONObject payload) {
        ChatThreadPropertiesUpdatedEvent eventPayload = new ChatThreadPropertiesUpdatedEvent();

        try {
            eventPayload.setThreadId(payload.getString("threadId"));

            eventPayload.setUpdatedOn(payload.getString("editTime"));

            ChatParticipant updatedBy = new ChatParticipant();
            updatedBy.setUser(
                getUserIdentifier(getJSONObject(payload, "editedBy").getString("participantId")));
            updatedBy.setDisplayName(getJSONObject(payload, "editedBy").getString("displayName"));
            eventPayload.setUpdatedBy(updatedBy);

            eventPayload.setVersion(payload.getString("version"));

            ChatThreadProperties chatThreadProperties = new ChatThreadProperties();
            chatThreadProperties.setTopic(getJSONObject(payload, "properties").getString("topic"));
            eventPayload.setProperties(chatThreadProperties);
        } catch (JSONException e) {
            CLIENT_LOGGER.error(e.getMessage());
        }

        return eventPayload;
    }

    private static BaseEvent getChatThreadCreated(JSONObject payload) {
        ChatThreadCreatedEvent eventPayload = new ChatThreadCreatedEvent();

        try {
            eventPayload.setThreadId(payload.getString("threadId"));

            ChatParticipant createdBy = new ChatParticipant();
            createdBy.setUser(
                getUserIdentifier(getJSONObject(payload, "createdBy").getString("participantId")));
            createdBy.setDisplayName(getJSONObject(payload, "createdBy").getString("displayName"));

            List<ChatParticipant> chatParticipants = new ArrayList<>();
            JSONArray members = getJSONArray(payload, "members");
            for (int i = 0; i < members.length(); i++) {
                JSONObject member = members.getJSONObject(i);
                CommunicationIdentifier communicationUser =
                    getUserIdentifier(member.getString("participantId"));

                ChatParticipant chatParticipant = new ChatParticipant();
                chatParticipant.setUser(communicationUser);
                chatParticipant.setDisplayName(member.getString("displayName"));

                chatParticipants.add(chatParticipant);
            }

            eventPayload.setCreatedOn(payload.getString("createTime"));
            eventPayload.setCreatedBy(createdBy);
            eventPayload.setVersion(payload.getString("version"));
            eventPayload.setParticipants(chatParticipants);

            ChatThreadProperties chatThreadProperties = new ChatThreadProperties();
            chatThreadProperties.setTopic(getJSONObject(payload, "properties").getString("topic"));
            eventPayload.setProperties(chatThreadProperties);
        } catch (JSONException e) {
            CLIENT_LOGGER.error(e.getMessage());
        }

        return eventPayload;
    }

    private static BaseEvent getReadReceiptReceived(JSONObject payload) {
        ReadReceiptReceivedEvent eventPayload = new ReadReceiptReceivedEvent();

        try {
            eventPayload.setThreadId(payload.getString("groupId"));

            eventPayload.setSender(getUserIdentifier(payload.getString("senderId")));
            eventPayload.setRecipient(getUserIdentifier(payload.getString("recipientId")));

            eventPayload.setChatMessageId(payload.getString("messageId"));
            eventPayload.setReadOn(new Date().toString());
        } catch (JSONException e) {
            CLIENT_LOGGER.error(e.getMessage());
        }

        return eventPayload;
    }

    private static BaseEvent getTypingIndicatorReceived(JSONObject payload) {
        TypingIndicatorReceivedEvent eventPayload = new TypingIndicatorReceivedEvent();

        try {
            eventPayload.setThreadId(payload.getString("groupId"));

            eventPayload.setSender(getUserIdentifier(payload.getString("senderId")));
            eventPayload.setRecipient(getUserIdentifier(payload.getString("recipientId")));

            eventPayload.setReceivedOn(payload.getString("originalArrivalTime"));
            eventPayload.setVersion(payload.getString("version"));
        } catch (JSONException e) {
            CLIENT_LOGGER.error(e.getMessage());
        }

        return eventPayload;
    }

    private static BaseEvent getChatMessageDeleted(JSONObject payload) {
        ChatMessageDeletedEvent eventPayload = new ChatMessageDeletedEvent();

        try {
            eventPayload.setThreadId(payload.getString("groupId"));

            eventPayload.setSender(getUserIdentifier(payload.getString("senderId")));
            eventPayload.setRecipient(getUserIdentifier(payload.getString("recipientId")));


            eventPayload.setId(payload.getString("messageId"));
            eventPayload.setSenderDisplayName(payload.getString("senderDisplayName"));
            eventPayload.setCreatedOn(payload.getString("originalArrivalTime"));
            eventPayload.setVersion(payload.getString("version"));
            eventPayload.setDeletedOn(payload.getString("deletetime"));
        } catch (JSONException e) {
            CLIENT_LOGGER.error(e.getMessage());
        }

        return eventPayload;
    }

    private static BaseEvent getChatMessageEdited(JSONObject payload) {
        ChatMessageEditedEvent eventPayload = new ChatMessageEditedEvent();

        try {
            eventPayload.setThreadId(payload.getString("groupId"));

            eventPayload.setSender(getUserIdentifier(payload.getString("senderId")));
            eventPayload.setRecipient(getUserIdentifier(payload.getString("recipientId")));

            eventPayload.setId(payload.getString("messageId"));
            eventPayload.setSenderDisplayName(payload.getString("senderDisplayName"));
            eventPayload.setCreatedOn(payload.getString("originalArrivalTime"));
            eventPayload.setVersion(payload.getString("version"));
            eventPayload.setContent(payload.getString("messageBody"));
            eventPayload.setEditedOn(payload.getString("edittime"));
        } catch (JSONException e) {
            CLIENT_LOGGER.error(e.getMessage());
        }

        return eventPayload;
    }

    private static BaseEvent getChatMessageReceived(JSONObject payload) {
        ChatMessageReceivedEvent eventPayload = new ChatMessageReceivedEvent();

        try {
            eventPayload.setThreadId(payload.getString("groupId"));
            eventPayload.setSender(getUserIdentifier(payload.getString("senderId")));
            eventPayload.setRecipient(getUserIdentifier(payload.getString("recipientId")));

            eventPayload.setId(payload.getString("messageId"));
            eventPayload.setSenderDisplayName(payload.getString("senderDisplayName"));
            eventPayload.setCreatedOn(payload.getString("originalArrivalTime"));
            eventPayload.setVersion(payload.getString("version"));
            eventPayload.setType(payload.getString("messageType"));
            eventPayload.setContent(payload.getString("messageBody"));
            eventPayload.setPriority(payload.getString("priority"));
        } catch (JSONException e) {
            CLIENT_LOGGER.error(e.getMessage());
        }

        return eventPayload;
    }

    private static CommunicationIdentifier getUserIdentifier(String mri) {
        if (mri.startsWith("8:orgid")) {
            MicrosoftTeamsUserIdentifier userIdentifier = new MicrosoftTeamsUserIdentifier(mri, false);
            userIdentifier.setCloudEnvironment(CommunicationCloudEnvironment.PUBLIC);
            return userIdentifier;
        } else if (mri.startsWith("8:dod")) {
            MicrosoftTeamsUserIdentifier userIdentifier = new MicrosoftTeamsUserIdentifier(mri, false);
            userIdentifier.setCloudEnvironment(CommunicationCloudEnvironment.DOD);
            return userIdentifier;
        } else if (mri.startsWith("8:gcch")) {
            MicrosoftTeamsUserIdentifier userIdentifier = new MicrosoftTeamsUserIdentifier(mri, false);
            userIdentifier.setCloudEnvironment(CommunicationCloudEnvironment.GCCH);
            return userIdentifier;
        } else if (mri.startsWith("8:teamsvisitor")) {
            return new MicrosoftTeamsUserIdentifier(mri, true);
        } else if (mri.startsWith("4:")) {
            return new PhoneNumberIdentifier(mri);
        } else if (mri.startsWith("8:acs:") || mri.startsWith("8:spool:")) {
            return new CommunicationUserIdentifier(mri);
        } else {
            return new UnknownIdentifier(mri);
        }
    }

    private static JSONObject getJSONObject(JSONObject payload, String property) throws JSONException {
        return new JSONObject(payload.getString(property));
    }

    private static JSONArray getJSONArray(JSONObject payload, String property) throws JSONException {
        return new JSONArray(payload.getString(property));
    }
}
