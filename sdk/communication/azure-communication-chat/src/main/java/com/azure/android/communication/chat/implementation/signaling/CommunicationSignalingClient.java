// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.communication.chat.implementation.signaling;

import android.content.Context;

import com.azure.android.communication.chat.models.ChatEventType;
import com.azure.android.communication.chat.models.RealTimeNotificationCallback;
import com.azure.android.core.logging.ClientLogger;
import com.microsoft.trouterclient.ISelfHostedTrouterClient;
import com.microsoft.trouterclient.ITrouterAuthHeadersProvider;
import com.microsoft.trouterclient.ITrouterConnectionDataCache;
import com.microsoft.trouterclient.TrouterClientHost;
import com.microsoft.trouterclient.UserActivityState;
import com.microsoft.trouterclient.registration.ISkypetokenProvider;
import com.microsoft.trouterclient.registration.TrouterSkypetokenAuthHeaderProvider;
import com.microsoft.trouterclient.registration.TrouterUrlRegistrar;
import com.microsoft.trouterclient.registration.TrouterUrlRegistrationData;

import java.util.HashMap;
import java.util.Map;

import static com.azure.android.communication.chat.BuildConfig.PLATFORM;
import static com.azure.android.communication.chat.BuildConfig.PLATFORM_UI_VERSION;
import static com.azure.android.communication.chat.BuildConfig.TROUTER_APPLICATION_ID;
import static com.azure.android.communication.chat.BuildConfig.TROUTER_CLIENT_VERSION;
import static com.azure.android.communication.chat.BuildConfig.TROUTER_HOSTNAME;
import static com.azure.android.communication.chat.BuildConfig.TROUTER_MAX_REGISTRATION_TTLS;
import static com.azure.android.communication.chat.BuildConfig.TROUTER_REGISTRATION_HOSTNAME_AND_BASE_PATH;
import static com.azure.android.communication.chat.BuildConfig.TROUTER_TEMPLATE_KEY;

/**
 * The concrete class of signaling client for communication
 */
public class CommunicationSignalingClient implements SignalingClient {
    private final ClientLogger logger;
    private TrouterClientHost trouterClientHost;
    private ISelfHostedTrouterClient trouter;
    private String userToken;
    private final Map<RealTimeNotificationCallback, CommunicationListener> trouterListeners;
    private boolean isRealtimeNotificationsStarted;

    public CommunicationSignalingClient() {
        this.logger = new ClientLogger(this.getClass());
        isRealtimeNotificationsStarted = false;
        trouterListeners = new HashMap<>();
    }

    /**
     * flag to indicate if signaling client has started
     * @return boolean if signaling client has started
     */
    public boolean hasStarted() {
        return this.isRealtimeNotificationsStarted;
    }

    /**
     * Start the realtime connection.
     * @param skypeUserToken the skype user token
     * @param context the android application context
     */
    public void start(String skypeUserToken, Context context) {
        if (this.isRealtimeNotificationsStarted) {
            return;
        }

        this.isRealtimeNotificationsStarted = true;
        this.userToken = skypeUserToken;
        ISkypetokenProvider skypetokenProvider = new ISkypetokenProvider() {
            @Override
            public String getSkypetoken(boolean forceRefresh) {
                return userToken;
            }
        };

        ITrouterAuthHeadersProvider trouterAuthHeadersProvider =
            new TrouterSkypetokenAuthHeaderProvider(skypetokenProvider);

        TrouterUrlRegistrationData registrationData = new TrouterUrlRegistrationData(
            null,
            TROUTER_APPLICATION_ID,
            PLATFORM,
            PLATFORM_UI_VERSION,
            TROUTER_TEMPLATE_KEY,
            null,
            ""
        );
        final TrouterUrlRegistrar registrar = new TrouterUrlRegistrar(
            skypetokenProvider,
            registrationData,
            TROUTER_REGISTRATION_HOSTNAME_AND_BASE_PATH,
            Integer.parseInt(TROUTER_MAX_REGISTRATION_TTLS)
        );

        try {
            trouterListeners.clear();
            trouterClientHost = TrouterClientHost.initialize(context, TROUTER_CLIENT_VERSION);
            trouter = trouterClientHost.createTrouterClient(trouterAuthHeadersProvider,
                new InMemoryConnectionDataCache(), TROUTER_HOSTNAME);
            trouter.withRegistrar(registrar);
            trouter.start();
            trouter.setUserActivityState(UserActivityState.ACTIVITY_ACTIVE);
        } catch (Throwable e) {
            logger.error(e.getMessage());
        }
    }

    /**
     * Stop the realtime connection and unsubscribe all event handlers.
     */
    public void stop() {
        if (!isRealtimeNotificationsStarted) {
            return;
        }

        this.isRealtimeNotificationsStarted = false;
        this.trouter.close();
        this.trouterListeners.clear();
    }

    @Override
    public void on(ChatEventType chatEventType, RealTimeNotificationCallback listener) {
        CommunicationListener communicationListener = new CommunicationListener(chatEventType, listener);
        String loggingName = CommunicationSignalingClient.class.getName();
        if (!trouterListeners.containsKey(listener)) {
            if (ChatEventType.CHAT_MESSAGE_RECEIVED.equals(chatEventType)) {
                trouter.registerListener(communicationListener, "/chatMessageReceived", loggingName);
            } else if (ChatEventType.TYPING_INDICATOR_RECEIVED.equals(chatEventType)) {
                trouter.registerListener(communicationListener, "/typingIndicatorReceived", loggingName);
            } else if (ChatEventType.READ_RECEIPT_RECEIVED.equals(chatEventType)) {
                trouter.registerListener(communicationListener, "/readReceiptReceived", loggingName);
            } else if (ChatEventType.CHAT_MESSAGE_EDITED.equals(chatEventType)) {
                trouter.registerListener(communicationListener, "/chatMessageEdited", loggingName);
            } else if (ChatEventType.CHAT_MESSAGE_DELETED.equals(chatEventType)) {
                trouter.registerListener(communicationListener, "/chatMessageDeleted", loggingName);
            } else if (ChatEventType.CHAT_THREAD_CREATED.equals(chatEventType)) {
                trouter.registerListener(communicationListener, "/chatThreadCreated", loggingName);
            } else if (ChatEventType.CHAT_THREAD_PROPERTIES_UPDATED.equals(chatEventType)) {
                trouter.registerListener(communicationListener, "/chatThreadPropertiesUpdated", loggingName);
            } else if (ChatEventType.CHAT_THREAD_DELETED.equals(chatEventType)) {
                trouter.registerListener(communicationListener, "/chatThreadDeleted", loggingName);
            } else if (ChatEventType.PARTICIPANTS_ADDED.equals(chatEventType)) {
                trouter.registerListener(communicationListener, "/participantsAdded", loggingName);
            } else if (ChatEventType.PARTICIPANTS_REMOVED.equals(chatEventType)) {
                trouter.registerListener(communicationListener, "/participantsRemoved", loggingName);
            } else {
                return;
            }
            trouterListeners.put(listener, communicationListener);
        }
    }

    @Override
    public void off(ChatEventType chatEventType, RealTimeNotificationCallback listener) {
        if (trouterListeners.containsKey(listener)) {
            trouter.unregisterListener(trouterListeners.get(listener));
            trouterListeners.remove(listener);
        }
    }

    static class InMemoryConnectionDataCache implements ITrouterConnectionDataCache {
        private String cachedData = "";

        @Override
        public void store(String s) {
            cachedData = s;
        }

        @Override
        public String load() {
            return cachedData;
        }
    }

}
