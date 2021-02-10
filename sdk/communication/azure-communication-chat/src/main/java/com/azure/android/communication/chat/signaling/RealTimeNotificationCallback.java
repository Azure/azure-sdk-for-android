package com.azure.android.communication.chat.signaling;

import org.json.JSONObject;

public interface RealTimeNotificationCallback {

    public void onChatEvent(JSONObject chatEvent);

}
