// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.communication.chat.implementation.notifications.signaling;

import android.text.TextUtils;

import com.azure.android.communication.chat.implementation.notifications.NotificationUtils;
import com.azure.android.communication.chat.models.ChatEvent;
import com.azure.android.communication.chat.models.ChatEventType;
import com.azure.android.communication.chat.models.RealTimeNotificationCallback;
import com.azure.android.core.logging.ClientLogger;
import com.microsoft.trouterclient.ITrouterConnectionInfo;
import com.microsoft.trouterclient.ITrouterListener;
import com.microsoft.trouterclient.ITrouterRequest;
import com.microsoft.trouterclient.ITrouterResponse;

import java.util.List;

final class CommunicationListener implements ITrouterListener {

    private final ClientLogger logger;
    private final ChatEventType chatEventType;
    private final RealTimeNotificationCallback listenerFromConsumer;

    CommunicationListener(ChatEventType chatEventType, RealTimeNotificationCallback listener) {
        this.chatEventType = chatEventType;
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
        ChatEvent chatEvent = NotificationUtils.parseTrouterNotificationPayload(chatEventType, iTrouterRequest.getBody());
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

    @Override
    public void onTrouterUserActivityStateAccepted(String correlationVector) {
        final String msg = "onTrouterUserActivityStateAccepted(): correlationVector=" + correlationVector;
        logger.info(msg);
    }

    @Override
    public void onTrouterMessageLoss(List<String> flowTags) {
        final String msg = "onTrouterMessageLoss(): flowTags=" + TextUtils.join(",", flowTags);
        logger.info(msg);
    }
}
