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
    private final TrouterClientHost trouterClientHost;
    private ISelfHostedTrouterClient trouter;
    private final String userToken;
    private final Map<String, CommunicationListener> trouterListeners;
    private boolean isRealtimeNotificationsStarted;

    /**
     *
     * @param userToken the skype token
     * @param context the android application context
     */
    public CommunicationSignalingClient(String userToken, Context context) {
        this.logger = new ClientLogger(this.getClass());
        isRealtimeNotificationsStarted = false;
        this.userToken = userToken;
        trouterClientHost = TrouterClientHost.initialize(context, TROUTER_CLIENT_VERSION);
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
     */
    public void start() {
        if (this.isRealtimeNotificationsStarted) {
            return;
        }

        this.isRealtimeNotificationsStarted = true;
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
            trouter = trouterClientHost.createTrouterClient(trouterAuthHeadersProvider,
                new InMemoryConnectionDataCache(), TROUTER_HOSTNAME);
            trouter.withRegistrar(registrar);
            trouter.start();
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
    }

    @Override
    public void on(ChatEventType chatEventType, String listenerId, RealTimeNotificationCallback listener) {
        CommunicationListener communicationListener = new CommunicationListener(chatEventType, listener);
        String loggingName = CommunicationSignalingClient.class.getName();
        if (!trouterListeners.containsKey(listenerId)) {
            switch (chatEventType) {
                case CHAT_MESSAGE_RECEIVED:
                    trouter.registerListener(communicationListener, "/chatMessageReceived", loggingName);
                    break;
                case TYPING_INDICATOR_RECEIVED:
                    trouter.registerListener(communicationListener, "/typingIndicatorReceived", loggingName);
                    break;
                case READ_RECEIPT_RECEIVED:
                    trouter.registerListener(communicationListener, "/readReceiptReceived", loggingName);
                    break;
                case CHAT_MESSAGE_EDITED:
                    trouter.registerListener(communicationListener, "/chatMessageEdited", loggingName);
                    break;
                case CHAT_MESSAGE_DELETED:
                    trouter.registerListener(communicationListener, "/chatMessageDeleted", loggingName);
                    break;
                case CHAT_THREAD_CREATED:
                    trouter.registerListener(communicationListener, "/chatThreadCreated", loggingName);
                    break;
                case CHAT_THREAD_PROPERTIES_UPDATED:
                    trouter.registerListener(communicationListener, "/chatThreadPropertiesUpdated", loggingName);
                    break;
                case CHAT_THREAD_DELETED:
                    trouter.registerListener(communicationListener, "/chatThreadDeleted", loggingName);
                    break;
                case PARTICIPANTS_ADDED:
                    trouter.registerListener(communicationListener, "/participantsAdded", loggingName);
                    break;
                case PARTICIPANTS_REMOVED:
                    trouter.registerListener(communicationListener, "/participantsRemoved", loggingName);
                    break;
                default:
                    return;
            }
            trouterListeners.put(listenerId, communicationListener);
        }
    }

    @Override
    public void off(ChatEventType chatEventType, String listenerId) {
        if (trouterListeners.containsKey(listenerId)) {
            trouter.unregisterListener(trouterListeners.get(listenerId));
            trouterListeners.remove(listenerId);
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
