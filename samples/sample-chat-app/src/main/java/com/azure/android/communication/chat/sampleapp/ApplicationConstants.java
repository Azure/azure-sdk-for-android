package com.azure.android.communication.chat.sampleapp;

import com.azure.android.communication.common.CommunicationTokenCredential;

public class ApplicationConstants {
    // Replace firstUserId and secondUserId with valid communication user identifiers from your ACS instance.
    public final static String firstUserId = "8:acs:357e39d2-a29a-4bf6-88cc-fda0afc2c0ed_00000011-78ee-0489-4ff7-343a0d00d4df";
    public final static String secondUserId = "";
    // Replace userAccessToken with a valid communication service token for your ACS instance.
    public static final String firstUserAccessToken = "eyJhbGciOiJSUzI1NiIsImtpZCI6IjEwNCIsIng1dCI6IlJDM0NPdTV6UENIWlVKaVBlclM0SUl4Szh3ZyIsInR5cCI6IkpXVCJ9.eyJza3lwZWlkIjoiYWNzOjM1N2UzOWQyLWEyOWEtNGJmNi04OGNjLWZkYTBhZmMyYzBlZF8wMDAwMDAxMS03OGVlLTA0ODktNGZmNy0zNDNhMGQwMGQ0ZGYiLCJzY3AiOjE3OTIsImNzaSI6IjE2NTI5MDg5NDkiLCJleHAiOjE2NTI5OTUzNDksImFjc1Njb3BlIjoiY2hhdCIsInJlc291cmNlSWQiOiIzNTdlMzlkMi1hMjlhLTRiZjYtODhjYy1mZGEwYWZjMmMwZWQiLCJpYXQiOjE2NTI5MDg5NDl9.jJhsVEr74QKsRi5wV7mXoFPSmSUra9EpImVxXH3n3kOkYreSPczDcaaZ7to6wwZjZ0cDXn6Sz6v64R8fusSHKSxYosIBjVD1_N26eOgTjDdQdUrOI8EiFLPl06EmE1DWztGBDZ04FRwVtxuZGsxEHl1ArC_Umns7Yol9aJyBMGidSBlmLAxDAYMiArpaVY_3b1T7A51sQ9NO73s0lKLpOvgt-x_h5beEW52Xs7sYWrbIBLZk9oU0c0PtsTQ-o32f50gruUB85UlIykCDOfmPVpONJS_cjmeloFIM1UjcOs6u4dAHnlorjOAU14J8eU0vVTMI4hMzCofnNVmMMCYvUg";
    public static final String endpoint = "https://chat-sdktester-e2e.communication.azure.com";
    public static final String sdkVersion = "1.2.0-beta.1";
    public final static String SDK_NAME = "azure-communication-com.azure.android.communication.chat";
    public final static String APPLICATION_ID = "Chat_Test_App";
    public final static String TAG = "[Chat Test App]";
    public final static CommunicationTokenCredential communicationTokenCredential = new CommunicationTokenCredential(firstUserAccessToken);
}
