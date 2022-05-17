package com.azure.android.communication.chat.sampleapp;

import com.azure.android.communication.common.CommunicationTokenCredential;

public class ApplicationConstants {
    // Replace firstUserId and secondUserId with valid communication user identifiers from your ACS instance.
    public final static String firstUserId = "8:acs:357e39d2-a29a-4bf6-88cc-fda0afc2c0ed_00000011-4290-60f0-7f07-113a0d009f7a";
    public final static String secondUserId = "";
    // Replace userAccessToken with a valid communication service token for your ACS instance.
    public static final String firstUserAccessToken = "eyJhbGciOiJSUzI1NiIsImtpZCI6IjEwNCIsIng1dCI6IlJDM0NPdTV6UENIWlVKaVBlclM0SUl4Szh3ZyIsInR5cCI6IkpXVCJ9.eyJza3lwZWlkIjoiYWNzOjM1N2UzOWQyLWEyOWEtNGJmNi04OGNjLWZkYTBhZmMyYzBlZF8wMDAwMDAxMS03MTU5LTc4NDUtNGZmNy0zNDNhMGQwMDY0ODgiLCJzY3AiOjE3OTIsImNzaSI6IjE2NTI3ODE3MzIiLCJleHAiOjE2NTI4NjgxMzIsImFjc1Njb3BlIjoiY2hhdCIsInJlc291cmNlSWQiOiIzNTdlMzlkMi1hMjlhLTRiZjYtODhjYy1mZGEwYWZjMmMwZWQiLCJpYXQiOjE2NTI3ODE3MzJ9.ezdYqCzk-q4w6JtfvEhT_91P5Ks9wL-wPE_tgemUHXOXRlx1uP61oMk3NNvUFceZPVBB_KN7e6EC3YridmmPCb54YIyCI60L3m59M-4VtRaFsApCo02tfGmuQKyUMLhIDb_vSUtCuMvZCVoajWCzYZEJPnx76yRP48G0fC3gE8L7uPrlJ4ee_CqoOKYugsrN88ryqGHkcYCLx-R4Im1j2SMxFKEwfT3kMCVopYU-9NKv1BpJ60m-6TrCPDyH_oyNc8iqPSQYU_StseKDo2WkyVPFsGalEXGhe3gPidiVKpbbGJM_vFP-nkn1vUYP5NCV6YW8j-u9WXZMvgyUH6iEdA";
    public static final String endpoint = "https://chat-sdktester-e2e.communication.azure.com";
    public static final String sdkVersion = "1.2.0-beta.1";
    public final static String SDK_NAME = "azure-communication-com.azure.android.communication.chat";
    public final static String APPLICATION_ID = "Chat_Test_App";
    public final static String TAG = "[Chat Test App]";
    public final static CommunicationTokenCredential communicationTokenCredential = new CommunicationTokenCredential(firstUserAccessToken);
}
