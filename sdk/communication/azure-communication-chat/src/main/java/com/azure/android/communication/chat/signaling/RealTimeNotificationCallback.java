// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.communication.chat.signaling;

import org.json.JSONObject;

@FunctionalInterface
public interface RealTimeNotificationCallback {

    public void onChatEvent(JSONObject chatEvent);

}
