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
import com.azure.android.communication.chat.models.ChatEventType;
import com.azure.android.communication.common.CommunicationCloudEnvironment;
import com.azure.android.communication.common.CommunicationIdentifier;
import com.azure.android.communication.common.CommunicationUserIdentifier;
import com.azure.android.communication.common.MicrosoftTeamsUserIdentifier;
import com.azure.android.communication.common.PhoneNumberIdentifier;
import com.azure.android.communication.common.UnknownIdentifier;
import com.azure.android.core.logging.ClientLogger;
import com.azure.android.core.serde.jackson.JacksonSerder;
import com.azure.android.core.serde.jackson.SerdeEncoding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.threeten.bp.Instant;
import org.threeten.bp.OffsetDateTime;
import org.threeten.bp.ZoneId;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public final class TrouterUtils {

    private static final String ACS_USER_PREFIX = "8:acs:";
    private static final String SPOOL_USER_PREFIX = "8:spool:";
    private static final String TEAMS_PUBLIC_USER_PREFIX = "8:orgid:";
    private static final String TEAMS_GCCH_USER_PREFIX = "8:gcch:";
    private static final String TEAMS_DOD_USER_PREFIX = "8:dod:";
    private static final String TEAMS_VISITOR_USER_PREFIX = "8:teamsvisitor:";
    private static final String PHONE_NUMBER_PREFIX = "4:";

    private static final ClientLogger CLIENT_LOGGER = new ClientLogger(TrouterUtils.class);
    private static final JacksonSerder JACKSON_SERDER = JacksonSerder.createDefault();
    private static final Pattern PATTERN = Pattern.compile("^\"*|\"*$");

    /**
     * Mapping chat event id to trouter event id code
     */
    static final Map<ChatEventType, Integer> EVENT_IDS_MAPPING = new HashMap<ChatEventType, Integer>() {{
            put(ChatEventType.CHAT_MESSAGE_RECEIVED, 200);
            put(ChatEventType.TYPING_INDICATOR_RECEIVED, 245);
            put(ChatEventType.READ_RECEIPT_RECEIVED, 246);
            put(ChatEventType.CHAT_MESSAGE_EDITED, 247);
            put(ChatEventType.CHAT_MESSAGE_DELETED, 248);
            put(ChatEventType.CHAT_THREAD_CREATED, 257);
            put(ChatEventType.CHAT_THREAD_PROPERTIES_UPDATED, 258);
            put(ChatEventType.CHAT_THREAD_DELETED, 259);
            put(ChatEventType.PARTICIPANTS_ADDED, 260);
            put(ChatEventType.PARTICIPANTS_REMOVED, 261);
        }};

    public static BaseEvent parsePayload(ChatEventType chatEventType, String body) {
        int eventId = 0;
        JSONObject genericPayload = null;
        try {
            eventId = EVENT_IDS_MAPPING.get(chatEventType);
            genericPayload = new JSONObject(body);
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

        return toEventPayload(chatEventType, body);
    }

    public static BaseEvent toEventPayload(ChatEventType eventId, String body) {
        switch (eventId) {
            case CHAT_MESSAGE_RECEIVED:
                return getChatMessageReceived(body);
            case TYPING_INDICATOR_RECEIVED:
                return getTypingIndicatorReceived(body);
            case READ_RECEIPT_RECEIVED:
                return getReadReceiptReceived(body);
            case CHAT_MESSAGE_EDITED:
                return getChatMessageEdited(body);
            case CHAT_MESSAGE_DELETED:
                return getChatMessageDeleted(body);
            case CHAT_THREAD_CREATED:
                return getChatThreadCreated(body);
            case CHAT_THREAD_PROPERTIES_UPDATED:
                return getChatThreadPropertiesUpdated(body);
            case CHAT_THREAD_DELETED:
                return getChatThreadDeleted(body);
            case PARTICIPANTS_ADDED:
                return getParticipantsAdded(body);
            case PARTICIPANTS_REMOVED:
                return getParticipantsRemoved(body);
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

    public static OffsetDateTime extractReadTimeFromConsumptionHorizon(String consumptionHorizon) {
        String readTimeString = consumptionHorizon.split(";")[1];
        return parseEpochTime(Long.parseLong(readTimeString));
    }

    public static ChatMessageType parseChatMessageType(String rawType) {
        if (rawType.equalsIgnoreCase("Text")) {
            return ChatMessageType.TEXT;
        }

        if (rawType.equalsIgnoreCase("RichText/Html")) {
            return ChatMessageType.HTML;
        }

        // Return TEXT if fail to parse
        return ChatMessageType.TEXT;
    }

    public static OffsetDateTime parseEpochTime(Long epochMilli) {
        return OffsetDateTime.ofInstant(
            Instant.ofEpochMilli(epochMilli),
            ZoneId.of("UTC"));
    }

    public static JSONObject getJSONObject(JSONObject payload, String property) throws JSONException {
        return new JSONObject(payload.getString(property));
    }

    public static JSONArray getJSONArray(JSONObject payload, String property) throws JSONException {
        return new JSONArray(payload.getString(property));
    }

    private static BaseEvent getParticipantsRemoved(String body) {
        try {
            ParticipantsRemovedEvent participantsRemovedEvent = JACKSON_SERDER.deserialize(body,
                ParticipantsRemovedEvent.class,
                SerdeEncoding.JSON);
            EventAccessorHelper.setParticipantsRemovedEvent(participantsRemovedEvent);
            return participantsRemovedEvent;
        } catch (IOException e) {
            CLIENT_LOGGER.error(e.getMessage());
            return null;
        }
    }

    private static BaseEvent getParticipantsAdded(String body) {
        try {
            ParticipantsAddedEvent participantsAddedEvent = JACKSON_SERDER.deserialize(body,
                ParticipantsAddedEvent.class,
                SerdeEncoding.JSON);
            EventAccessorHelper.setParticipantsAddedEvent(participantsAddedEvent);
            return participantsAddedEvent;
        } catch (IOException e) {
            CLIENT_LOGGER.error(e.getMessage());
            return null;
        }
    }

    private static BaseEvent getChatThreadDeleted(String body) {
        try {
            ChatThreadDeletedEvent chatThreadDeletedEvent = JACKSON_SERDER.deserialize(body,
                ChatThreadDeletedEvent.class,
                SerdeEncoding.JSON);
            EventAccessorHelper.setChatThreadDeletedEvent(chatThreadDeletedEvent);
            return chatThreadDeletedEvent;
        } catch (IOException e) {
            CLIENT_LOGGER.error(e.getMessage());
            return null;
        }
    }

    private static BaseEvent getChatThreadPropertiesUpdated(String body) {
        try {
            ChatThreadPropertiesUpdatedEvent chatThreadPropertiesUpdatedEvent = JACKSON_SERDER.deserialize(body,
                ChatThreadPropertiesUpdatedEvent.class,
                SerdeEncoding.JSON);
            EventAccessorHelper.setChatThreadPropertiesUpdatedEvent(chatThreadPropertiesUpdatedEvent);
            return chatThreadPropertiesUpdatedEvent;
        } catch (IOException e) {
            CLIENT_LOGGER.error(e.getMessage());
            return null;
        }
    }

    private static BaseEvent getChatThreadCreated(String body) {
        try {
            ChatThreadCreatedEvent chatThreadCreatedEvent = JACKSON_SERDER.deserialize(body,
                ChatThreadCreatedEvent.class,
                SerdeEncoding.JSON);
            EventAccessorHelper.setChatThreadCreatedEvent(chatThreadCreatedEvent);
            return chatThreadCreatedEvent;
        } catch (IOException e) {
            CLIENT_LOGGER.error(e.getMessage());
            return null;
        }
    }

    private static BaseEvent getReadReceiptReceived(String body) {
        try {
            ReadReceiptReceivedEvent readReceiptReceivedEvent = JACKSON_SERDER.deserialize(body,
                ReadReceiptReceivedEvent.class,
                SerdeEncoding.JSON);
            EventAccessorHelper.setReadReceiptReceivedEvent(readReceiptReceivedEvent);
            return readReceiptReceivedEvent;
        } catch (IOException e) {
            CLIENT_LOGGER.error(e.getMessage());
            return null;
        }
    }

    private static BaseEvent getTypingIndicatorReceived(String body) {
        try {
            TypingIndicatorReceivedEvent typingIndicatorReceivedEvent = JACKSON_SERDER.deserialize(body,
                TypingIndicatorReceivedEvent.class,
                SerdeEncoding.JSON);
            EventAccessorHelper.setTypingIndicatorReceivedEvent(typingIndicatorReceivedEvent);
            return typingIndicatorReceivedEvent;
        } catch (IOException e) {
            CLIENT_LOGGER.error(e.getMessage());
            return null;
        }
    }

    private static BaseEvent getChatMessageDeleted(String body) {
        try {
            ChatMessageDeletedEvent chatMessageDeletedEvent = JACKSON_SERDER.deserialize(body,
                ChatMessageDeletedEvent.class,
                SerdeEncoding.JSON);
            EventAccessorHelper.setChatMessageDeletedEvent(chatMessageDeletedEvent);
            return chatMessageDeletedEvent;
        } catch (IOException e) {
            CLIENT_LOGGER.error(e.getMessage());
            return null;
        }
    }

    private static BaseEvent getChatMessageEdited(String body) {
        try {
            ChatMessageEditedEvent chatMessageEditedEvent = JACKSON_SERDER.deserialize(body,
                ChatMessageEditedEvent.class,
                SerdeEncoding.JSON);
            EventAccessorHelper.setChatMessageEditedEvent(chatMessageEditedEvent);
            return chatMessageEditedEvent;
        } catch (IOException e) {
            CLIENT_LOGGER.error(e.getMessage());
            return null;
        }
    }

    private static BaseEvent getChatMessageReceived(String body) {
        try {
            ChatMessageReceivedEvent chatMessageReceivedEvent = JACKSON_SERDER.deserialize(body,
                ChatMessageReceivedEvent.class,
                SerdeEncoding.JSON);
            EventAccessorHelper.setChatMessageReceivedEvent(chatMessageReceivedEvent);
            return chatMessageReceivedEvent;
        } catch (IOException e) {
            CLIENT_LOGGER.error(e.getMessage());
            return null;
        }
    }
}
