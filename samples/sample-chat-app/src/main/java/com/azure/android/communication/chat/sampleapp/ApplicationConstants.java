package com.azure.android.communication.chat.sampleapp;

import com.azure.android.communication.common.CommunicationTokenCredential;

public class ApplicationConstants {
    // Replace firstUserId and secondUserId with valid communication user identifiers from your ACS instance.
    public final static String firstUserId = "8:acs:357e39d2-a29a-4bf6-88cc-fda0afc2c0ed_00000011-78ee-0489-4ff7-343a0d00d4df";
    public final static String secondUserId = "";
    // Replace userAccessToken with a valid communication service token for your ACS instance.
    public static final String firstUserAccessToken = "eyJhbGciOiJSUzI1NiIsImtpZCI6IjEwNCIsIng1dCI6IlJDM0NPdTV6UENIWlVKaVBlclM0SUl4Szh3ZyIsInR5cCI6IkpXVCJ9.eyJza3lwZWlkIjoiYWNzOjM1N2UzOWQyLWEyOWEtNGJmNi04OGNjLWZkYTBhZmMyYzBlZF8wMDAwMDAxMS03OGVlLTA0ODktNGZmNy0zNDNhMGQwMGQ0ZGYiLCJzY3AiOjE3OTIsImNzaSI6IjE2NTI5OTU3ODMiLCJleHAiOjE2NTMwODIxODMsImFjc1Njb3BlIjoiY2hhdCIsInJlc291cmNlSWQiOiIzNTdlMzlkMi1hMjlhLTRiZjYtODhjYy1mZGEwYWZjMmMwZWQiLCJpYXQiOjE2NTI5OTU3ODN9.ldDHdf2QVY1j7C3zXPE4tpFQNkvJQcN9q8_R_5sr55w0C169kae8WfT8MRkW14UhYZGDqx6aDRpbg2zY7iW4sNKHjS9CSrEnjWMmPJUOsevFS1ekLNDlx0hm5T0cVzR4wLqnJhjZrp7Ql8ChrZaB1qM6F4I76v1qY3IYX-TSayUER0ym-sOMTkk3ruf0U7rJHWNBvZyWq-ln4MwA9PVqMn7Vl3Ur3QCr0M0BKMX5nwkXFgQ6PwRrcRtL5b8tT6h7OzdRNojGxYXvJOz7_sFB_YkSQcXSLciNsYFHoBQ4V57MNCrCBXB-x9z6g1V9u8o3z1XPPz-eXi5BomwlsAAOEQ";
    public static final String endpoint = "https://chat-sdktester-e2e.communication.azure.com";
    public static final String sdkVersion = "1.2.0-beta.1";
    public final static String SDK_NAME = "azure-communication-com.azure.android.communication.chat";
    public final static String APPLICATION_ID = "Chat_Test_App";
    public final static String TAG = "[Chat Test App]";
    public final static CommunicationTokenCredential communicationTokenCredential = new CommunicationTokenCredential(firstUserAccessToken);
}
