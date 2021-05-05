// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.communication.chat.implementation.signaling;

import com.azure.android.communication.chat.models.BaseEvent;
import com.azure.android.communication.chat.models.ChatMessageDeletedEvent;
import com.azure.android.communication.chat.models.ChatMessageEditedEvent;
import com.azure.android.communication.chat.models.ChatMessageReceivedEvent;
import com.azure.android.communication.chat.models.ChatMessageType;
import com.azure.android.communication.chat.models.ChatThreadCreatedEvent;
import com.azure.android.communication.chat.models.ChatThreadDeletedEvent;
import com.azure.android.communication.chat.models.ChatThreadPropertiesUpdatedEvent;
import com.azure.android.communication.chat.models.ParticipantsAddedEvent;
import com.azure.android.communication.chat.models.ParticipantsRemovedEvent;
import com.azure.android.communication.chat.models.ReadReceiptReceivedEvent;
import com.azure.android.communication.chat.models.TypingIndicatorReceivedEvent;
import com.azure.android.communication.chat.models.ChatEventKind;
import com.azure.android.communication.chat.models.ChatParticipant;
import com.azure.android.communication.chat.models.ChatThreadProperties;
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
import org.threeten.bp.Instant;
import org.threeten.bp.OffsetDateTime;
import org.threeten.bp.ZoneId;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class TrouterUtils {

    private static final String ACS_USER_PREFIX = "8:acs:";
    private static final String SPOOL_USER_PREFIX = "8:spool:";
    private static final String TEAMS_PUBLIC_USER_PREFIX = "8:orgid:";
    private static final String TEAMS_GCCH_USER_PREFIX = "8:gcch:";
    private static final String TEAMS_DOD_USER_PREFIX = "8:dod:";
    private static final String TEAMS_VISITOR_USER_PREFIX = "8:teamsvisitor:";
    private static final String PHONE_NUMBER_PREFIX = "4:";

    private static final ClientLogger CLIENT_LOGGER = new ClientLogger(TrouterUtils.class);
    /**
     * Mapping chat event id to trouter event id code
     */
    static final Map<ChatEventKind, Integer> EVENT_IDS_MAPPING = new HashMap<ChatEventKind, Integer>() {{
            put(ChatEventKind.CHAT_MESSAGE_RECEIVED, 200);
            put(ChatEventKind.TYPING_INDICATOR_RECEIVED, 245);
            put(ChatEventKind.READ_RECEIPT_RECEIVED, 246);
            put(ChatEventKind.CHAT_MESSAGE_EDITED, 247);
            put(ChatEventKind.CHAT_MESSAGE_DELETED, 248);
            put(ChatEventKind.CHAT_THREAD_CREATED, 257);
            put(ChatEventKind.CHAT_THREAD_PROPERTIES_UPDATED, 258);
            put(ChatEventKind.CHAT_THREAD_DELETED, 259);
            put(ChatEventKind.PARTICIPANTS_ADDED, 260);
            put(ChatEventKind.PARTICIPANTS_REMOVED, 261);
        }};

    public static BaseEvent toMessageHandler(ChatEventKind chatEventKind, String responseBody) {
        int eventId = 0;
        JSONObject genericPayload = null;
        try {
            eventId = EVENT_IDS_MAPPING.get(chatEventKind);
            genericPayload = new JSONObject(responseBody);
        } catch (Exception e) {
            CLIENT_LOGGER.error(e.getMessage());
        }

        try {
            if (genericPayload == null || genericPayload.getInt("eventId") != eventId) {
                return null;
            }
        } catch (JSONException e) {
            CLIENT_LOGGER.error(e.getMessage());
        }

        return toEventPayload(chatEventKind, genericPayload);
    }

    public static BaseEvent toEventPayload(ChatEventKind eventId, JSONObject payload) {
        switch (eventId) {
            case CHAT_MESSAGE_RECEIVED:
                return getChatMessageReceived(payload);
            case TYPING_INDICATOR_RECEIVED:
                return getTypingIndicatorReceived(payload);
            case READ_RECEIPT_RECEIVED:
                return getReadReceiptReceived(payload);
            case CHAT_MESSAGE_EDITED:
                return getChatMessageEdited(payload);
            case CHAT_MESSAGE_DELETED:
                return getChatMessageDeleted(payload);
            case CHAT_THREAD_CREATED:
                return getChatThreadCreated(payload);
            case CHAT_THREAD_PROPERTIES_UPDATED:
                return getChatThreadPropertiesUpdated(payload);
            case CHAT_THREAD_DELETED:
                return getChatThreadDeleted(payload);
            case PARTICIPANTS_ADDED:
                return getParticipantsAdded(payload);
            case PARTICIPANTS_REMOVED:
                return getParticipantsRemoved(payload);
            default:
                return null;
        }
    }

    public static CommunicationIdentifier getCommunicationIdentifier(String rawId) {
        if (rawId.startsWith(TEAMS_PUBLIC_USER_PREFIX)) {
            return new MicrosoftTeamsUserIdentifier(rawId.substring(TEAMS_PUBLIC_USER_PREFIX.length()), false)
                .setRawId(rawId)
                .setCloudEnvironment(CommunicationCloudEnvironment.PUBLIC);
        } else if (rawId.startsWith(TEAMS_DOD_USER_PREFIX)) {
            return new MicrosoftTeamsUserIdentifier(rawId.substring(TEAMS_DOD_USER_PREFIX.length()), false)
                .setRawId(rawId)
                .setCloudEnvironment(CommunicationCloudEnvironment.DOD);
        } else if (rawId.startsWith(TEAMS_GCCH_USER_PREFIX)) {
            return new MicrosoftTeamsUserIdentifier(rawId.substring(TEAMS_GCCH_USER_PREFIX.length()), false)
                .setRawId(rawId)
                .setCloudEnvironment(CommunicationCloudEnvironment.GCCH);
        } else if (rawId.startsWith(TEAMS_VISITOR_USER_PREFIX)) {
            return new MicrosoftTeamsUserIdentifier(rawId.substring(TEAMS_VISITOR_USER_PREFIX.length()), true)
                .setRawId(rawId);
        } else if (rawId.startsWith(PHONE_NUMBER_PREFIX)) {
            return new PhoneNumberIdentifier(rawId.substring(PHONE_NUMBER_PREFIX.length()))
                .setRawId(rawId);
        } else if (rawId.startsWith(ACS_USER_PREFIX) || rawId.startsWith(SPOOL_USER_PREFIX)) {
            return new CommunicationUserIdentifier(rawId);
        } else {
            return new UnknownIdentifier(rawId);
        }
    }

    private static BaseEvent getParticipantsRemoved(JSONObject payload) {
        ParticipantsRemovedEvent eventPayload = new ParticipantsRemovedEvent();

        try {
            eventPayload.setThreadId(payload.getString("threadId"));

            ChatParticipant removedBy = new ChatParticipant();
            removedBy.setCommunicationIdentifier(
                getCommunicationIdentifier(
                    getJSONObject(payload, "removedBy").getString("participantId")));
            removedBy.setDisplayName(getJSONObject(payload, "removedBy").getString("displayName"));
            eventPayload.setRemovedBy(removedBy);

            List<ChatParticipant> chatParticipants = new ArrayList<>();
            JSONArray members = getJSONArray(payload, "participantsRemoved");
            for (int i = 0; i < members.length(); i++) {
                JSONObject member = members.getJSONObject(i);

                CommunicationIdentifier communicationUser =
                    getCommunicationIdentifier(member.getString("participantId"));
                ChatParticipant chatParticipant = new ChatParticipant();
                chatParticipant.setCommunicationIdentifier(communicationUser);
                chatParticipant.setDisplayName(member.getString("displayName"));
                chatParticipant.setShareHistoryTime(parseEpochTime(member.getLong("shareHistoryTime")));

                chatParticipants.add(chatParticipant);
            }

            eventPayload.setRemovedOn(parseTimeStamp(payload.getString("time")));
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
            addedBy.setCommunicationIdentifier(
                getCommunicationIdentifier(
                    getJSONObject(payload, "addedBy").getString("participantId")));
            addedBy.setDisplayName(getJSONObject(payload, "addedBy").getString("displayName"));
            eventPayload.setAddedBy(addedBy);

            List<ChatParticipant> chatParticipants = new ArrayList<>();
            JSONArray members = getJSONArray(payload, "participantsAdded");
            for (int i = 0; i < members.length(); i++) {
                JSONObject member = members.getJSONObject(i);

                CommunicationIdentifier communicationUser =
                    getCommunicationIdentifier(member.getString("participantId"));
                ChatParticipant chatParticipant = new ChatParticipant();
                chatParticipant.setCommunicationIdentifier(communicationUser);
                chatParticipant.setDisplayName(member.getString("displayName"));
                chatParticipant.setShareHistoryTime(parseEpochTime(member.getLong("shareHistoryTime")));

                chatParticipants.add(chatParticipant);
            }

            eventPayload.setAddedOn(parseTimeStamp(payload.getString("time")));
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
            deletedBy.setCommunicationIdentifier(
                getCommunicationIdentifier(
                    getJSONObject(payload, "deletedBy").getString("participantId")));
            deletedBy.setDisplayName(getJSONObject(payload, "deletedBy").getString("displayName"));
            eventPayload.setDeletedBy(deletedBy);

            eventPayload.setDeletedOn(parseTimeStamp(payload.getString("deleteTime")));
            eventPayload.setVersion(payload.getString("version"));
        } catch (JSONException e) {
            CLIENT_LOGGER.error(e.getMessage());
        }

        return eventPayload;
    }

    private static BaseEvent getChatThreadPropertiesUpdated(JSONObject payload) {
        ChatThreadPropertiesUpdatedEvent eventPayload = new ChatThreadPropertiesUpdatedEvent();

        try {
            String threadId = payload.getString("threadId");
            eventPayload.setThreadId(threadId);

            eventPayload.setUpdatedOn(parseTimeStamp(payload.getString("editTime")));

            ChatParticipant updatedBy = new ChatParticipant();
            updatedBy.setCommunicationIdentifier(
                getCommunicationIdentifier(
                    getJSONObject(payload, "editedBy").getString("participantId")));
            updatedBy.setDisplayName(getJSONObject(payload, "editedBy").getString("displayName"));
            eventPayload.setUpdatedBy(updatedBy);

            eventPayload.setVersion(payload.getString("version"));

            ChatThreadProperties chatThreadProperties = new ChatThreadProperties();
            chatThreadProperties
                .setId(threadId)
                .setTopic(getJSONObject(payload, "properties").getString("topic"));
            eventPayload.setProperties(chatThreadProperties);
        } catch (JSONException e) {
            CLIENT_LOGGER.error(e.getMessage());
        }

        return eventPayload;
    }

    private static BaseEvent getChatThreadCreated(JSONObject payload) {
        ChatThreadCreatedEvent eventPayload = new ChatThreadCreatedEvent();

        try {
            String threadId = payload.getString("threadId");
            eventPayload.setThreadId(threadId);

            ChatParticipant createdBy = new ChatParticipant();
            CommunicationIdentifier createdByCommunicationIdentifier = getCommunicationIdentifier(
                getJSONObject(payload, "createdBy").getString("participantId"));
            createdBy.setCommunicationIdentifier(createdByCommunicationIdentifier);
            createdBy.setDisplayName(getJSONObject(payload, "createdBy").getString("displayName"));

            List<ChatParticipant> chatParticipants = new ArrayList<>();
            JSONArray members = getJSONArray(payload, "members");
            for (int i = 0; i < members.length(); i++) {
                JSONObject member = members.getJSONObject(i);
                CommunicationIdentifier communicationUser =
                    getCommunicationIdentifier(member.getString("participantId"));

                ChatParticipant chatParticipant = new ChatParticipant();
                chatParticipant.setCommunicationIdentifier(communicationUser);
                chatParticipant.setDisplayName(member.getString("displayName"));

                chatParticipants.add(chatParticipant);
            }

            OffsetDateTime createTime = parseTimeStamp(payload.getString("createTime"));
            eventPayload.setCreatedOn(createTime);
            eventPayload.setCreatedBy(createdBy);
            eventPayload.setVersion(payload.getString("version"));
            eventPayload.setParticipants(chatParticipants);

            ChatThreadProperties chatThreadProperties = new ChatThreadProperties();
            chatThreadProperties
                .setId(threadId)
                .setTopic(getJSONObject(payload, "properties").getString("topic"))
                .setCreatedByCommunicationIdentifier(createdByCommunicationIdentifier)
                .setCreatedOn(createTime);
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

            eventPayload.setSender(getCommunicationIdentifier(payload.getString("senderId")));
            eventPayload.setRecipient(getCommunicationIdentifier(payload.getString("recipientMri")));

            eventPayload.setChatMessageId(payload.getString("messageId"));

            String consumptionHorizon = payload.getString("consumptionhorizon");
            eventPayload.setReadOn(extractReadTimeFromConsumptionHorizon(consumptionHorizon));
        } catch (JSONException e) {
            CLIENT_LOGGER.error(e.getMessage());
        }

        return eventPayload;
    }

    private static BaseEvent getTypingIndicatorReceived(JSONObject payload) {
        TypingIndicatorReceivedEvent eventPayload = new TypingIndicatorReceivedEvent();

        try {
            eventPayload.setThreadId(payload.getString("groupId"));

            eventPayload.setSender(getCommunicationIdentifier(payload.getString("senderId")));
            eventPayload.setRecipient(getCommunicationIdentifier(payload.getString("recipientMri")));

            eventPayload.setReceivedOn(parseTimeStamp(payload.getString("originalArrivalTime")));
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

            eventPayload.setSender(getCommunicationIdentifier(payload.getString("senderId")));
            eventPayload.setRecipient(getCommunicationIdentifier(payload.getString("recipientMri")));


            eventPayload.setId(payload.getString("messageId"));
            eventPayload.setSenderDisplayName(payload.getString("senderDisplayName"));
            eventPayload.setCreatedOn(parseTimeStamp(payload.getString("originalArrivalTime")));
            eventPayload.setVersion(payload.getString("version"));
            eventPayload.setDeletedOn(parseTimeStamp(payload.getString("deletetime")));
        } catch (JSONException e) {
            CLIENT_LOGGER.error(e.getMessage());
        }

        return eventPayload;
    }

    private static BaseEvent getChatMessageEdited(JSONObject payload) {
        ChatMessageEditedEvent eventPayload = new ChatMessageEditedEvent();

        try {
            eventPayload.setThreadId(payload.getString("groupId"));

            eventPayload.setSender(getCommunicationIdentifier(payload.getString("senderId")));
            eventPayload.setRecipient(getCommunicationIdentifier(payload.getString("recipientMri")));

            eventPayload.setId(payload.getString("messageId"));
            eventPayload.setSenderDisplayName(payload.getString("senderDisplayName"));
            eventPayload.setCreatedOn(parseTimeStamp(payload.getString("originalArrivalTime")));
            eventPayload.setVersion(payload.getString("version"));
            eventPayload.setContent(payload.getString("messageBody"));
            eventPayload.setEditedOn(parseTimeStamp(payload.getString("edittime")));
        } catch (JSONException e) {
            CLIENT_LOGGER.error(e.getMessage());
        }

        return eventPayload;
    }

    private static BaseEvent getChatMessageReceived(JSONObject payload) {
        ChatMessageReceivedEvent eventPayload = new ChatMessageReceivedEvent();

        try {
            eventPayload.setThreadId(payload.getString("groupId"));
            eventPayload.setSender(getCommunicationIdentifier(payload.getString("senderId")));
            eventPayload.setRecipient(getCommunicationIdentifier(payload.getString("recipientMri")));

            eventPayload.setId(payload.getString("messageId"));
            eventPayload.setSenderDisplayName(payload.getString("senderDisplayName"));
            eventPayload.setCreatedOn(parseTimeStamp(payload.getString("originalArrivalTime")));
            eventPayload.setVersion(payload.getString("version"));
            eventPayload.setType(parseChatMessageType(payload.getString("messageType")));
            eventPayload.setContent(payload.getString("messageBody"));
            eventPayload.setPriority(payload.getString("priority"));
        } catch (JSONException e) {
            CLIENT_LOGGER.error(e.getMessage());
        }

        return eventPayload;
    }

    private static OffsetDateTime parseEpochTime(Long epochMilli) {
        return OffsetDateTime.ofInstant(
            Instant.ofEpochMilli(epochMilli),
            ZoneId.of("UTC"));
    }

    private static OffsetDateTime parseTimeStamp(String timeStamp) {
        return OffsetDateTime.ofInstant(
            Instant.parse(timeStamp),
            ZoneId.of("UTC"));
    }

    private static OffsetDateTime extractReadTimeFromConsumptionHorizon(String consumptionHorizon) {
        String readTimeString = consumptionHorizon.split(";")[1];
        return parseEpochTime(Long.parseLong(readTimeString));
    }

    private static ChatMessageType parseChatMessageType(String rawType) {
        if (rawType.equalsIgnoreCase("Text")) {
            return ChatMessageType.TEXT;
        }

        if (rawType.equalsIgnoreCase("RichText/Html")) {
            return ChatMessageType.HTML;
        }

        // Return TEXT if fail to parse
        return ChatMessageType.TEXT;
    }

    private static JSONObject getJSONObject(JSONObject payload, String property) throws JSONException {
        return new JSONObject(payload.getString(property));
    }

    private static JSONArray getJSONArray(JSONObject payload, String property) throws JSONException {
        return new JSONArray(payload.getString(property));
    }
}
