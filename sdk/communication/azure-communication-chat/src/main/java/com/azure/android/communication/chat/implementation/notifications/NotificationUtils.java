// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.communication.chat.implementation.notifications;

import android.text.TextUtils;
import android.util.Base64;

import com.azure.android.communication.chat.implementation.notifications.signaling.EventAccessorHelper;
import com.azure.android.communication.chat.models.ChatEvent;
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
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.json.JSONException;
import org.json.JSONObject;
import org.threeten.bp.Instant;
import org.threeten.bp.OffsetDateTime;
import org.threeten.bp.ZoneId;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public final class NotificationUtils {

    private static final String ACS_USER_PREFIX = "8:acs:";
    private static final String ACS_GCCH_USER_PREFIX = "8:gcch-acs:";
    private static final String ACS_DOD_USER_PREFIX = "8:dod-acs:";
    private static final String SPOOL_USER_PREFIX = "8:spool:";
    private static final String TEAMS_PUBLIC_USER_PREFIX = "8:orgid:";
    private static final String TEAMS_GCCH_USER_PREFIX = "8:gcch:";
    private static final String TEAMS_DOD_USER_PREFIX = "8:dod:";
    private static final String TEAMS_VISITOR_USER_PREFIX = "8:teamsvisitor:";
    private static final String PHONE_NUMBER_PREFIX = "4:";

    private static final ClientLogger CLIENT_LOGGER = new ClientLogger(NotificationUtils.class);
    private static final JacksonSerder JACKSON_SERDER = JacksonSerder.createDefault();
    private static final ObjectMapper JSON_MAPPER = new ObjectMapper();

    public static final int MAX_TOKEN_FETCH_RETRY_COUNT = 3;
    public static final int MAX_REGISTRATION_RETRY_COUNT = 3;
    public static final int MAX_REGISTRATION_RETRY_DELAY_S = 30;
    public static final int REGISTRATION_RENEW_IN_ADVANCE_S = 30;

    private static final String CIPHER_TRANSFORMATION = "AES/CBC/PKCS5Padding";
    private static final int INITIALIZATION_VECTOR_SIZE = 16;
    private static final int CIPHER_MODE_SIZE = 1;
    private static final int HMAC_SIZE = 32;

    public enum CloudType {
        Public,
        Gcch,
        Dod
    }

    /**
     * Mapping skype id prefix to cloud type
     */
    private static final Map<String, CloudType> SKYPE_ID_CLOUD_TYPES_MAPPING = new HashMap<String, CloudType>() {{
        put("acs", CloudType.Public);
        put("spool", CloudType.Public);
        put("orgid", CloudType.Public);
        put("gcch", CloudType.Gcch);
        put("gcch-acs", CloudType.Gcch);
        put("dod", CloudType.Dod);
        put("dod-acs", CloudType.Dod);
    }};

    /**
     * Mapping chat event id to notification event id code
     */
    private static final Map<ChatEventType, Integer> EVENT_IDS_MAPPING = new HashMap<ChatEventType, Integer>() {{
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

    /**
     * Mapping notification event id code to chat event id
     */
    private static final Map<Integer, ChatEventType> EVENT_TYPE_MAPPING = new HashMap<Integer, ChatEventType>() {{
        put(200, ChatEventType.CHAT_MESSAGE_RECEIVED);
        put(245, ChatEventType.TYPING_INDICATOR_RECEIVED);
        put(246, ChatEventType.READ_RECEIPT_RECEIVED);
        put(247, ChatEventType.CHAT_MESSAGE_EDITED);
        put(248, ChatEventType.CHAT_MESSAGE_DELETED);
        put(257, ChatEventType.CHAT_THREAD_CREATED);
        put(258, ChatEventType.CHAT_THREAD_PROPERTIES_UPDATED);
        put(259, ChatEventType.CHAT_THREAD_DELETED);
        put(260, ChatEventType.PARTICIPANTS_ADDED);
        put(261, ChatEventType.PARTICIPANTS_REMOVED);
    }};

    public static ChatEvent parseTrouterNotificationPayload(ChatEventType chatEventType, String body) {
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

    public static ChatEvent toEventPayload(ChatEventType chatEventType, String body) {
        if (ChatEventType.CHAT_MESSAGE_RECEIVED.equals(chatEventType)) {
            return getChatMessageReceived(body);
        } else if (ChatEventType.TYPING_INDICATOR_RECEIVED.equals(chatEventType)) {
            return getTypingIndicatorReceived(body);
        } else if (ChatEventType.READ_RECEIPT_RECEIVED.equals(chatEventType)) {
            return getReadReceiptReceived(body);
        } else if (ChatEventType.CHAT_MESSAGE_EDITED.equals(chatEventType)) {
            return getChatMessageEdited(body);
        } else if (ChatEventType.CHAT_MESSAGE_DELETED.equals(chatEventType)) {
            return getChatMessageDeleted(body);
        } else if (ChatEventType.CHAT_THREAD_CREATED.equals(chatEventType)) {
            return getChatThreadCreated(body);
        } else if (ChatEventType.CHAT_THREAD_PROPERTIES_UPDATED.equals(chatEventType)) {
            return getChatThreadPropertiesUpdated(body);
        } else if (ChatEventType.CHAT_THREAD_DELETED.equals(chatEventType)) {
            return getChatThreadDeleted(body);
        } else if (ChatEventType.PARTICIPANTS_ADDED.equals(chatEventType)) {
            return getParticipantsAdded(body);
        } else if (ChatEventType.PARTICIPANTS_REMOVED.equals(chatEventType)) {
            return getParticipantsRemoved(body);
        }
        return null;
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
        } else if (rawId.startsWith(ACS_USER_PREFIX)
            || rawId.startsWith(ACS_GCCH_USER_PREFIX)
            || rawId.startsWith(ACS_DOD_USER_PREFIX)
            || rawId.startsWith(SPOOL_USER_PREFIX)) {
            return new CommunicationUserIdentifier(rawId);
        } else {
            return new UnknownIdentifier(rawId);
        }
    }

    public static boolean isValidEventId(int eventId) {
        return EVENT_TYPE_MAPPING.containsKey(eventId);
    }

    public static ChatEventType getChatEventTypeByEventId(int eventId) {
        if (EVENT_TYPE_MAPPING.containsKey(eventId)) {
            return EVENT_TYPE_MAPPING.get(eventId);
        }

        return null;
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

    public static Map<String, String> parseChatMessageMetadata(String rawMetadata) {
        Map<String, String> metadata = Collections.<String, String>emptyMap();

        if (rawMetadata == null || TextUtils.isEmpty(rawMetadata)) {
            return metadata;
        }

        try {
            ObjectMapper mapper = new ObjectMapper();
            metadata = mapper.readValue(rawMetadata, new TypeReference<HashMap<String, String>>() { });
        } catch (Exception e) {
            CLIENT_LOGGER.error(e.getMessage());
        }

        return metadata;
    }

    public static OffsetDateTime parseEpochTime(Long epochMilli) {
        return OffsetDateTime.ofInstant(
            Instant.ofEpochMilli(epochMilli),
            ZoneId.of("UTC"));
    }

    public static CloudType getUserCloudTypeFromSkypeToken(String skypeToken) {
        String skypeId = decodeSkypeIdFromJwtToken(skypeToken);
        String prefix = skypeId.substring(0, skypeId.indexOf(":"));

        if (SKYPE_ID_CLOUD_TYPES_MAPPING.containsKey(prefix)) {
            return SKYPE_ID_CLOUD_TYPES_MAPPING.get(prefix);
        }

        return CloudType.Public;
    }

    public static boolean verifyEncryptedPayload(
        byte[] encryptionKey,
        byte[] iv,
        byte[] cipherText,
        byte[] hmac,
        SecretKey authKey) throws Throwable {
        byte[] encryptionKeyIvCipherText = new byte[encryptionKey.length + iv.length + cipherText.length];
        System.arraycopy(encryptionKey, 0, encryptionKeyIvCipherText, 0, encryptionKey.length);
        System.arraycopy(iv, 0, encryptionKeyIvCipherText, encryptionKey.length, iv.length);
        System.arraycopy(cipherText, 0, encryptionKeyIvCipherText,
            encryptionKey.length + iv.length, cipherText.length);

        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] sha256Hash = digest.digest(authKey.getEncoded());
        Mac hmacSHA256 = Mac.getInstance("HmacSHA256");
        hmacSHA256.init(new SecretKeySpec(sha256Hash, "HmacSHA256"));
        byte[] result = hmacSHA256.doFinal(encryptionKeyIvCipherText);

        return Arrays.equals(hmac, result);
    }

    public static String decryptPushNotificationPayload(
        byte[] iv,
        byte[] cipherText,
        SecretKey cryptoKey) throws Throwable {
        Cipher cipher = Cipher.getInstance(CIPHER_TRANSFORMATION);
        cipher.init(Cipher.DECRYPT_MODE, cryptoKey, new IvParameterSpec(iv));

        byte[] plainText = cipher.doFinal(cipherText);
        return new String(plainText, StandardCharsets.UTF_8);
    }

    public static byte[] extractEncryptionKey(byte[] result) {
        return Arrays.copyOfRange(result, 0, CIPHER_MODE_SIZE);
    }

    public static byte[] extractInitializationVector(byte[] result) {
        return Arrays.copyOfRange(result, CIPHER_MODE_SIZE, CIPHER_MODE_SIZE + INITIALIZATION_VECTOR_SIZE);
    }

    public static byte[] extractCipherText(byte[] result) {
        return Arrays.copyOfRange(
            result,
            CIPHER_MODE_SIZE + INITIALIZATION_VECTOR_SIZE,
            CIPHER_MODE_SIZE + INITIALIZATION_VECTOR_SIZE + getCipherTextSize(result));
    }

    public static byte[] extractHmac(byte[] result) {
        return Arrays.copyOfRange(result, result.length - HMAC_SIZE, result.length);
    }

    public static int getCipherTextSize(byte[] result) {
        return result.length - HMAC_SIZE - CIPHER_MODE_SIZE - INITIALIZATION_VECTOR_SIZE;
    }

    private static ChatEvent getParticipantsRemoved(String body) {
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

    private static ChatEvent getParticipantsAdded(String body) {
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

    private static ChatEvent getChatThreadDeleted(String body) {
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

    private static ChatEvent getChatThreadPropertiesUpdated(String body) {
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

    private static ChatEvent getChatThreadCreated(String body) {
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

    private static ChatEvent getReadReceiptReceived(String body) {
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

    private static ChatEvent getTypingIndicatorReceived(String body) {
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

    private static ChatEvent getChatMessageDeleted(String body) {
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

    private static ChatEvent getChatMessageEdited(String body) {
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

    private static ChatEvent getChatMessageReceived(String body) {
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

    private static String decodeSkypeIdFromJwtToken(String jwtToken) {
        try {
            String[] tokenParts = jwtToken.split("\\.");
            String tokenPayload = tokenParts[1];
            byte[] decodedBytes = Base64.decode(tokenPayload, Base64.DEFAULT);
            String decodedPayloadJson = new String(decodedBytes, Charset.forName("UTF-8"));

            ObjectNode payloadObj = JSON_MAPPER.readValue(decodedPayloadJson, ObjectNode.class);

            return payloadObj.get("skypeid").asText();
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("'jwtToken' is not a valid token string", e);
        }
    }
}
