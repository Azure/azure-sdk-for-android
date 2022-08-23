// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.communication.chat.sampleapp;

import com.azure.android.communication.common.CommunicationTokenCredential;

public class ApplicationConstants {
    // Replace FIRST_USER_ID and SECOND_USER_ID with valid communication user identifiers from your ACS instance.
    public final static String FIRST_USER_ID = "";
    public final static String SECOND_USER_ID = "";
    // Replace userAccessToken with a valid communication service token for your ACS instance.
    public static final String FIRST_USER_ACCESS_TOKEN = "";
    public static final String ENDPOINT = "";
    public static final String SDK_VERSION = "1.2.0-beta.1";
    public final static String SDK_NAME = "azure-communication-com.azure.android.communication.chat";
    public final static String APPLICATION_ID = "Chat_Test_App";
    public final static String TAG = "[Chat Test App]";
    public final static CommunicationTokenCredential COMMUNICATION_TOKEN_CREDENTIAL = new CommunicationTokenCredential(FIRST_USER_ACCESS_TOKEN);
}
