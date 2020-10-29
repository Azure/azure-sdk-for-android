package com.azure.android.communication.chat;

public class ThreadsMocker {
    private static int count = 0;
    public static String mockThreads(int n) {
        StringBuilder jsonBuilder = new StringBuilder();
        jsonBuilder.append(String.format("{\"value\":["));
        boolean hasPrevious = false;
        for(int i =0; i < n; i++) {
            if (hasPrevious) jsonBuilder.append(", ");
            jsonBuilder.append(mockThreadInfo(i));
            hasPrevious = true;
        }
        jsonBuilder.append(String.format("], \"nextLink\": \"nextPageLink%s\"}", ++count));
        return jsonBuilder.toString();
    }

    private static String mockThreadInfo(int i) {
        return String.format("{\"id\": %1$s, \"topic\": \"topic%1$s\", \"isDeleted\": false}", i);
    }
}
