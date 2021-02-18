// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.communication.chat.signaling;

import com.azure.android.core.util.logging.ClientLogger;
import com.microsoft.trouterclient.ITrouterConnectionInfo;
import com.microsoft.trouterclient.ITrouterListener;
import com.microsoft.trouterclient.ITrouterRequest;
import com.microsoft.trouterclient.ITrouterResponse;

import org.json.JSONObject;

class CommunicationListener implements ITrouterListener {

    private ClientLogger logger;
    private String chatEventId;
    private RealTimeNotificationCallback listenerFromConsumer;

    public CommunicationListener(String chatEventId, RealTimeNotificationCallback listener) {
        this.chatEventId = chatEventId;
        this.listenerFromConsumer = listener;
        this.logger = ClientLogger.getDefault(this.getClass());
    }

    @Override
    public void onTrouterConnected(String endpointUrl, ITrouterConnectionInfo connectionInfo) {
        final String msg = "onTrouterConnected(): url=" + endpointUrl + ", newPublicUrl=" + Boolean.toString(connectionInfo.isNewEndpointUrl());
        logger.info(msg);
    }

    @Override
    public void onTrouterDisconnected() {
        final String msg = "onTrouterDisconnected()";
        logger.info(msg);
    }

    @Override
    public void onTrouterRequest(ITrouterRequest iTrouterRequest, ITrouterResponse iTrouterResponse) {
        final String msg = "onTrouterRequest(): #" + Long.toString(iTrouterResponse.getId()) + " " + iTrouterRequest.getMethod() + " " + iTrouterRequest.getUrlPathComponent() + "\n" + iTrouterRequest.getBody();
        logger.info(msg);
    }

    @Override
    public void onTrouterResponseSent(ITrouterResponse iTrouterResponse, boolean isSuccess) {
        final String msg = "onTrouterResponse(): #" + Long.toString(iTrouterResponse.getId()) + " isSuccess=" + Boolean.toString(isSuccess);
        logger.info(msg);
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
