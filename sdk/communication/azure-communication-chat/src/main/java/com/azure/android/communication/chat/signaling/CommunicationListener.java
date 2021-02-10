package com.azure.android.communication.chat.signaling;

import com.microsoft.trouterclient.ITrouterConnectionInfo;
import com.microsoft.trouterclient.ITrouterListener;
import com.microsoft.trouterclient.ITrouterRequest;
import com.microsoft.trouterclient.ITrouterResponse;

import org.json.JSONObject;

public class CommunicationListener implements ITrouterListener {

    String chatEventId;

    private RealTimeNotificationCallback listenerFromConsumer;

    public CommunicationListener(String chatEventId, RealTimeNotificationCallback listener) {
        this.chatEventId = chatEventId;
        this.listenerFromConsumer = listener;
    }

    @Override
    public void onTrouterConnected(String endpointUrl, ITrouterConnectionInfo connectionInfo) {
        final String msg = "onTrouterConnected(): url=" + endpointUrl + ", newPublicUrl=" + Boolean.toString(connectionInfo.isNewEndpointUrl());
        System.out.println(msg);
    }

    @Override
    public void onTrouterDisconnected() {
        final String msg = "onTrouterDisconnected()";
        System.out.println(msg);
    }

    @Override
    public void onTrouterRequest(ITrouterRequest iTrouterRequest, ITrouterResponse iTrouterResponse) {
        final String msg = "onTrouterRequest(): #" + Long.toString(iTrouterResponse.getId()) + " " + iTrouterRequest.getMethod() + " " + iTrouterRequest.getUrlPathComponent() + "\n" + iTrouterRequest.getBody();
        System.out.println(msg);
    }

    @Override
    public void onTrouterResponseSent(ITrouterResponse iTrouterResponse, boolean isSuccess) {
        final String msg = "onTrouterResponse(): #" + Long.toString(iTrouterResponse.getId()) + " isSuccess=" + Boolean.toString(isSuccess);
        System.out.println(msg);
        if (isSuccess) {
            // convert payload to chat event here
            JSONObject chatEvent = TrouterUtils.toMessageHandler(chatEventId, iTrouterResponse);
            if (chatEvent != null) {
                listenerFromConsumer.onChatEvent(chatEvent);
            }
        }
    }


    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        CommunicationListener trouterListener = (CommunicationListener) o;
        return this.chatEventId.equals(trouterListener.chatEventId) &&
            this.listenerFromConsumer.equals(trouterListener.listenerFromConsumer);
    }

}
