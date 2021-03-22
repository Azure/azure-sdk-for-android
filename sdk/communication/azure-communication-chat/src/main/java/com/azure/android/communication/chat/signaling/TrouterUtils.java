// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.communication.chat.signaling;

import com.azure.android.communication.chat.signaling.properties.ChatEventId;
import com.azure.android.core.logging.ClientLogger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

class TrouterUtils {

    private static final ClientLogger logger = new ClientLogger(TrouterUtils.class);
    /**
     * Mapping chat event id to trouter event id code
     */
    static Map<ChatEventId, Integer> eventIdsMapping = new HashMap<ChatEventId, Integer>() {{
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

    public static JSONObject toMessageHandler(String chatEventId, String responseBody) {
        int eventId = 0;
        JSONObject genericPayload = null;
        try {
            eventId = eventIdsMapping.get(ChatEventId.valueOf(chatEventId));
            genericPayload = new JSONObject(responseBody);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }

        try {
            if (genericPayload == null || genericPayload.getInt("_eventId") != eventId) {
                return null;
            }
        } catch (JSONException e) {
            logger.error(e.getMessage());
        }

        return toEventPayload(chatEventId, genericPayload);
    }

    public static JSONObject toEventPayload(String eventId, JSONObject payload) {
        if (eventId.equals(ChatEventId.chatMessageReceived.name())) {
            JSONObject eventPayload = new JSONObject();

            try {
                eventPayload.put("threadId", payload.get("groupId"));

                JSONObject communicationUserId = new JSONObject();
                communicationUserId.put("communicationUserId", payload.get("senderId"));
                eventPayload.put("sender", communicationUserId);

                JSONObject recipientCommunicationUserId = new JSONObject();
                communicationUserId.put("communicationUserId", payload.get("recipientId"));
                eventPayload.put("recipient", recipientCommunicationUserId);

                eventPayload.put("id", payload.get("messageId"));
                eventPayload.put("senderDisplayName", payload.get("senderDisplayName"));
                eventPayload.put("createdOn", payload.get("originalArrivalTime"));
                eventPayload.put("version", payload.get("version"));
                eventPayload.put("type", payload.get("messageType"));
                eventPayload.put("content", payload.get("messageBody"));
                eventPayload.put("priority", payload.get("priority"));
            } catch (JSONException e) {
                logger.error(e.getMessage());
            }

            return eventPayload;
        }

        if (eventId.equals(ChatEventId.chatMessageEdited.name())) {
            JSONObject eventPayload = new JSONObject();

            try {
                eventPayload.put("threadId", payload.get("groupId"));

                JSONObject communicationUserId = new JSONObject();
                communicationUserId.put("communicationUserId", payload.get("senderId"));
                eventPayload.put("sender", communicationUserId);

                JSONObject recipientCommunicationUserId = new JSONObject();
                communicationUserId.put("communicationUserId", payload.get("recipientId"));
                eventPayload.put("recipient", recipientCommunicationUserId);

                eventPayload.put("id", payload.get("messageId"));
                eventPayload.put("senderDisplayName", payload.get("senderDisplayName"));
                eventPayload.put("createdOn", payload.get("originalArrivalTime"));
                eventPayload.put("version", payload.get("version"));
                eventPayload.put("content", payload.get("messageBody"));
                eventPayload.put("editedOn", payload.get("edittime"));
            } catch (JSONException e) {
                logger.error(e.getMessage());
            }

            return eventPayload;
        }

        if (eventId.equals(ChatEventId.chatMessageDeleted.name())) {
            JSONObject eventPayload = new JSONObject();

            try {
                eventPayload.put("threadId", payload.get("groupId"));

                JSONObject communicationUserId = new JSONObject();
                communicationUserId.put("communicationUserId", payload.get("senderId"));
                eventPayload.put("sender", communicationUserId);

                JSONObject recipientCommunicationUserId = new JSONObject();
                communicationUserId.put("communicationUserId", payload.get("recipientId"));
                eventPayload.put("recipient", recipientCommunicationUserId);

                eventPayload.put("id", payload.get("messageId"));
                eventPayload.put("senderDisplayName", payload.get("senderDisplayName"));
                eventPayload.put("createdOn", payload.get("originalArrivalTime"));
                eventPayload.put("version", payload.get("version"));
                eventPayload.put("deletedOn", payload.get("deletetime"));
            } catch (JSONException e) {
                logger.error(e.getMessage());
            }

            return eventPayload;
        }

        if (eventId.equals(ChatEventId.typingIndicatorReceived.name())) {
            JSONObject eventPayload = new JSONObject();

            try {
                eventPayload.put("threadId", payload.get("groupId"));

                JSONObject communicationUserId = new JSONObject();
                communicationUserId.put("communicationUserId", payload.get("senderId"));
                eventPayload.put("sender", communicationUserId);

                JSONObject recipientCommunicationUserId = new JSONObject();
                communicationUserId.put("communicationUserId", payload.get("recipientId"));
                eventPayload.put("recipient", recipientCommunicationUserId);

                eventPayload.put("receivedOn", payload.get("originalArrivalTime"));
                eventPayload.put("version", payload.get("version"));
            } catch (JSONException e) {
                logger.error(e.getMessage());
            }

            return eventPayload;
        }

        if (eventId.equals(ChatEventId.readReceiptReceived.name())) {
            JSONObject eventPayload = new JSONObject();

            try {
                eventPayload.put("threadId", payload.get("groupId"));

                JSONObject communicationUserId = new JSONObject();
                communicationUserId.put("communicationUserId", payload.get("senderId"));
                eventPayload.put("sender", communicationUserId);

                JSONObject recipientCommunicationUserId = new JSONObject();
                communicationUserId.put("communicationUserId", payload.get("recipientId"));
                eventPayload.put("recipient", recipientCommunicationUserId);

                eventPayload.put("chatMessageId", payload.get("messageId"));
                eventPayload.put("readOn", new Date().toString());
            } catch (JSONException e) {
                logger.error(e.getMessage());
            }

            return eventPayload;
        }

        if (eventId.equals(ChatEventId.chatThreadCreated.name())) {
            JSONObject eventPayload = new JSONObject();

            try {
                eventPayload.put("threadId", payload.get("threadId"));

                JSONObject communicationUserId = new JSONObject();
                communicationUserId.put("communicationUserId", payload.getJSONObject("createdBy").get("participantId"));

                JSONObject createdBy = new JSONObject();
                createdBy.put("user", communicationUserId);
                createdBy.put("displayName", payload.getJSONObject("createdBy").get("displayName"));

                JSONArray chatParticipants = new JSONArray();
                JSONArray members = payload.getJSONArray("members");
                for (int i = 0 ; i < members.length(); i++) {
                    JSONObject member = members.getJSONObject(i);
                    communicationUserId = new JSONObject();
                    communicationUserId.put("communicationUserId", member.get("participantId"));

                    JSONObject chatParticipant = new JSONObject();
                    chatParticipant.put("user", communicationUserId);
                    chatParticipant.put("displayName", member.get("displayName"));

                    chatParticipants.put(chatParticipant);
                }

                eventPayload.put("createdOn", payload.get("createTime"));
                eventPayload.put("createdBy", createdBy);
                eventPayload.put("version", payload.get("version"));
                eventPayload.put("participants", chatParticipants);
                eventPayload.put("properties", payload.getJSONObject("properties"));
            } catch (JSONException e) {
                logger.error(e.getMessage());
            }

            return eventPayload;
        }

        if (eventId.equals(ChatEventId.chatThreadPropertiesUpdated.name())) {
            JSONObject eventPayload = new JSONObject();

            try {
                eventPayload.put("threadId", payload.get("threadId"));

                JSONObject communicationUserId = new JSONObject();
                communicationUserId.put("communicationUserId", payload.getJSONObject("editedBy").get("participantId"));

                JSONObject updatedBy = new JSONObject();
                updatedBy.put("user", communicationUserId);
                updatedBy.put("displayName", payload.getJSONObject("editedBy").get("displayName"));

                eventPayload.put("updatedOn", payload.get("editTime"));
                eventPayload.put("updatedBy", updatedBy);
                eventPayload.put("version", payload.get("version"));
                eventPayload.put("properties", payload.getJSONObject("properties"));
            } catch (JSONException e) {
                logger.error(e.getMessage());
            }

            return eventPayload;
        }

        if (eventId.equals(ChatEventId.chatThreadDeleted.name())) {
            JSONObject eventPayload = new JSONObject();

            try {
                eventPayload.put("threadId", payload.get("threadId"));

                JSONObject communicationUserId = new JSONObject();
                communicationUserId.put("communicationUserId", payload.getJSONObject("deletedBy").get("participantId"));

                JSONObject deletedBy = new JSONObject();
                deletedBy.put("user", communicationUserId);
                deletedBy.put("displayName", payload.getJSONObject("deletedBy").get("displayName"));

                eventPayload.put("deletedOn", payload.get("deleteTime"));
                eventPayload.put("deletedBy", deletedBy);
                eventPayload.put("version", payload.get("version"));
            } catch (JSONException e) {
                logger.error(e.getMessage());
            }

            return eventPayload;
        }

        if (eventId.equals(ChatEventId.participantsAdded.name())) {
            JSONObject eventPayload = new JSONObject();

            try {
                eventPayload.put("threadId", payload.get("threadId"));

                JSONObject communicationUserId = new JSONObject();
                communicationUserId.put("communicationUserId", payload.getJSONObject("addedBy").get("participantId"));

                JSONObject addedBy = new JSONObject();
                addedBy.put("user", communicationUserId);
                addedBy.put("displayName", payload.getJSONObject("addedBy").get("displayName"));

                JSONArray chatParticipants = new JSONArray();
                JSONArray members = payload.getJSONArray("participantsAdded");
                for (int i = 0 ; i < members.length(); i++) {
                    JSONObject member = members.getJSONObject(i);
                    communicationUserId = new JSONObject();
                    communicationUserId.put("communicationUserId", member.get("participantId"));

                    JSONObject chatParticipant = new JSONObject();
                    chatParticipant.put("user", communicationUserId);
                    chatParticipant.put("displayName", member.get("displayName"));
                    chatParticipant.put("shareHistoryTime", new Date(member.getString("shareHistoryTime")).toString());

                    chatParticipants.put(chatParticipant);
                }

                eventPayload.put("addedOn", payload.get("time"));
                eventPayload.put("addedBy", addedBy);
                eventPayload.put("version", payload.get("version"));
                eventPayload.put("participantsAdded", chatParticipants);
            } catch (JSONException e) {
                logger.error(e.getMessage());
            }

            return eventPayload;
        }

        if (eventId.equals(ChatEventId.participantsRemoved.name())) {
            JSONObject eventPayload = new JSONObject();

            try {
                eventPayload.put("threadId", payload.get("threadId"));

                JSONObject communicationUserId = new JSONObject();
                communicationUserId.put("communicationUserId", payload.getJSONObject("removedBy").get("participantId"));

                JSONObject removedBy = new JSONObject();
                removedBy.put("user", communicationUserId);
                removedBy.put("displayName", payload.getJSONObject("removedBy").get("displayName"));

                JSONArray chatParticipants = new JSONArray();
                JSONArray members = payload.getJSONArray("participantsRemoved");
                for (int i = 0 ; i < members.length(); i++) {
                    JSONObject member = members.getJSONObject(i);
                    communicationUserId = new JSONObject();
                    communicationUserId.put("communicationUserId", member.get("participantId"));

                    JSONObject chatParticipant = new JSONObject();
                    chatParticipant.put("user", communicationUserId);
                    chatParticipant.put("displayName", member.get("displayName"));
                    chatParticipant.put("shareHistoryTime", new Date(member.getString("shareHistoryTime")).toString());

                    chatParticipants.put(chatParticipant);
                }

                eventPayload.put("removedOn", payload.get("time"));
                eventPayload.put("removedBy", removedBy);
                eventPayload.put("version", payload.get("version"));
                eventPayload.put("participantsRemoved", chatParticipants);
            } catch (JSONException e) {
                logger.error(e.getMessage());
            }

            return eventPayload;
        }

        return null;
    }
}
