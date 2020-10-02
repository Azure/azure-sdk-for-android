package com.azure.android.communication.chat;

public class MessagesMocker {
    private static int count = 0;
    public static String mockThreadMessages(int n) {
        StringBuilder jsonBuilder = new StringBuilder();
        jsonBuilder.append(String.format("{\"value\":["));
        boolean hasPrevious = false;
        for(int i = 0; i < n; i++) {
            if (hasPrevious) jsonBuilder.append(", ");
            jsonBuilder.append(mockMessage(i));
            hasPrevious = true;
        }
        jsonBuilder.append(String.format("], \"nextLink\": \"nextPageLink%s\"}", ++count));
        return jsonBuilder.toString();
    }

    private static String mockMessage(int i) {
        StringBuilder jsonBuilder = new StringBuilder();
        jsonBuilder.append("{");
        jsonBuilder.append(String.format("\"id\": \"id%s\", ", i));
        jsonBuilder.append(String.format("\"priority\": \"Normal\", \"version\": \"1\", \"content\": \"content%s\",", i));
        jsonBuilder.append(String.format("\"type\": \"type%s\",", i));
        jsonBuilder.append(String.format("\"senderDisplayName\": \"sender%s\", ", i));
        jsonBuilder.append(String.format("\"senderId\":\"sender%s\" }", i));
        return jsonBuilder.toString();
    }
}
