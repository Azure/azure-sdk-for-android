// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.communication.chat.signaling;

import com.azure.android.core.logging.ClientLogger;
import com.microsoft.trouterclient.ITrouterConnectionInfo;
import com.microsoft.trouterclient.ITrouterListener;
import com.microsoft.trouterclient.ITrouterRequest;
import com.microsoft.trouterclient.ITrouterResponse;

import org.json.JSONObject;

final class CommunicationListener implements ITrouterListener {

    private final ClientLogger logger;
    private final String chatEventId;
    private final RealTimeNotificationCallback listenerFromConsumer;

    CommunicationListener(String chatEventId, RealTimeNotificationCallback listener) {
        this.chatEventId = chatEventId;
        this.listenerFromConsumer = listener;
        this.logger = new ClientLogger(this.getClass());
    }

    @Override
    public void onTrouterConnected(String endpointUrl, ITrouterConnectionInfo connectionInfo) {
        final String msg = "onTrouterConnected(): url=" + endpointUrl + ", newPublicUrl="
            + Boolean.toString(connectionInfo.isNewEndpointUrl());
        logger.info(msg);
    }

    @Override
    public void onTrouterDisconnected() {
        final String msg = "onTrouterDisconnected()";
        logger.info(msg);
    }

    @Override
    public void onTrouterRequest(ITrouterRequest iTrouterRequest, ITrouterResponse iTrouterResponse) {
        final String msg = "onTrouterRequest(): #" + Long.toString(iTrouterResponse.getId()) + " "
            + iTrouterRequest.getMethod() + " " + iTrouterRequest.getUrlPathComponent() + "\n"
            + iTrouterRequest.getBody();
        logger.info(msg);
        // convert payload to chat event here
        JSONObject chatEvent = TrouterUtils.toMessageHandler(chatEventId, iTrouterRequest.getBody());
        if (chatEvent != null) {
            listenerFromConsumer.onChatEvent(chatEvent);
        }
    }

    @Override
    public void onTrouterResponseSent(ITrouterResponse iTrouterResponse, boolean isSuccess) {
        final String msg = "onTrouterResponse(): #" + Long.toString(iTrouterResponse.getId())
            + " isSuccess=" + Boolean.toString(isSuccess);
        logger.info(msg);
    }

}
