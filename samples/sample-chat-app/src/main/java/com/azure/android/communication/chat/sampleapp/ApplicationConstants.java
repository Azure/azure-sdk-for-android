package com.azure.android.communication.chat.sampleapp;

import com.azure.android.communication.common.CommunicationTokenCredential;

public class ApplicationConstants {
    // Replace FIRST_USER_ID and SECOND_USER_ID with valid communication user identifiers from your ACS instance.
    public final static String FIRST_USER_ID = "8:acs:357e39d2-a29a-4bf6-88cc-fda0afc2c0ed_00000011-9cac-39eb-0d8b-08482200cca5";
    public final static String SECOND_USER_ID = "";
    // Replace userAccessToken with a valid communication service token for your ACS instance.
    public static final String FIRST_USER_ACCESS_TOKEN = "eyJhbGciOiJSUzI1NiIsImtpZCI6IjEwNCIsIng1dCI6IlJDM0NPdTV6UENIWlVKaVBlclM0SUl4Szh3ZyIsInR5cCI6IkpXVCJ9.eyJza3lwZWlkIjoiYWNzOjM1N2UzOWQyLWEyOWEtNGJmNi04OGNjLWZkYTBhZmMyYzBlZF8wMDAwMDAxMS05Y2FjLTM5ZWItMGQ4Yi0wODQ4MjIwMGNjYTUiLCJzY3AiOjE3OTIsImNzaSI6IjE2NTM1MDg1NzYiLCJleHAiOjE2NTM1OTQ5NzYsImFjc1Njb3BlIjoiY2hhdCIsInJlc291cmNlSWQiOiIzNTdlMzlkMi1hMjlhLTRiZjYtODhjYy1mZGEwYWZjMmMwZWQiLCJpYXQiOjE2NTM1MDg1NzZ9.UFIWLXUFXGU7nYx1UiVk1RCuJPJUQ9N33piQnJEm_wMq5ehQiclaQmtcZEKkL6lkVA5m7IwBqIB6QVwqqDCXg4KviKS85Cv5NKC4baZ1PasOgI2LRxySnYdwdZtGUXKePAWeIWkB8EHXre2XJ6MgAOkJI1i7SN0mB16gyckaNtbbIYEMCaHpjNOKagkfPyMofx-BE8DS_IPvBWIV-0ZYmvF-qQn4bS6mrpHWm03exQUtFwfMMDKX9tRRixF32PaL2EXjKq75W0JIcw8aM97Rx_XQpqaRnB8ST5WGZEmtcX8sY8Q5131J0sJtTv8aNcPZqGR8DQ-XTw9HBro8a1U5gw";
    public static final String ENDPOINT = "https://chat-sdktester-e2e.communication.azure.com";
    public static final String SDK_VERSION = "1.2.0-beta.1";
    public final static String SDK_NAME = "azure-communication-com.azure.android.communication.chat";
    public final static String APPLICATION_ID = "Chat_Test_App";
    public final static String TAG = "[Chat Test App]";
    public final static CommunicationTokenCredential communicationTokenCredential = new CommunicationTokenCredential(FIRST_USER_ACCESS_TOKEN);
}
