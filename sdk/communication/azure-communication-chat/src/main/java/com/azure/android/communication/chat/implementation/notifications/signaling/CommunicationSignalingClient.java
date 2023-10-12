// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.communication.chat.implementation.notifications.signaling;

import static com.azure.android.communication.chat.BuildConfig.PLATFORM;
import static com.azure.android.communication.chat.BuildConfig.PLATFORM_UI_VERSION;
import static com.azure.android.communication.chat.BuildConfig.TROUTER_APPLICATION_ID;
import static com.azure.android.communication.chat.BuildConfig.TROUTER_CLIENT_VERSION;
import static com.azure.android.communication.chat.BuildConfig.TROUTER_HOSTNAME;
import static com.azure.android.communication.chat.BuildConfig.TROUTER_HOSTNAME_DOD;
import static com.azure.android.communication.chat.BuildConfig.TROUTER_HOSTNAME_EUDB;
import static com.azure.android.communication.chat.BuildConfig.TROUTER_HOSTNAME_GCCH;
import static com.azure.android.communication.chat.BuildConfig.TROUTER_MAX_REGISTRATION_TTLS;
import static com.azure.android.communication.chat.BuildConfig.TROUTER_REGISTRATION_HOSTNAME;
import static com.azure.android.communication.chat.BuildConfig.TROUTER_REGISTRATION_HOSTNAME_DOD;
import static com.azure.android.communication.chat.BuildConfig.TROUTER_REGISTRATION_HOSTNAME_GCCH;
import static com.azure.android.communication.chat.BuildConfig.TROUTER_TEMPLATE_KEY;

import android.content.Context;

import com.azure.android.communication.chat.implementation.notifications.NotificationUtils;
import com.azure.android.communication.chat.implementation.notifications.NotificationUtils.CloudType;
import com.azure.android.communication.chat.models.ChatEventType;
import com.azure.android.communication.chat.models.RealTimeNotificationCallback;
import com.azure.android.communication.common.CommunicationTokenCredential;
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

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import java9.util.function.Consumer;

/**
 * The concrete class of signaling client for communication
 */
public class CommunicationSignalingClient implements SignalingClient {
    private final ClientLogger logger;
    private TrouterClientHost trouterClientHost;
    private ISelfHostedTrouterClient trouter;
    private String userToken;
    private final CommunicationTokenCredential communicationTokenCredential;
    private final Map<RealTimeNotificationCallback, CommunicationListener> trouterListeners;
    private boolean isRealtimeNotificationsStarted;
    private int tokenFetchRetries;
    private final HashSet<String> countriesEUDB = new HashSet<>(Arrays.asList("europe", "france", "germany", "norway", "switzerland", "sweden"));

    public CommunicationSignalingClient(CommunicationTokenCredential communicationTokenCredential) {
        this.communicationTokenCredential = communicationTokenCredential;
        this.logger = new ClientLogger(this.getClass());
        isRealtimeNotificationsStarted = false;
        trouterListeners = new HashMap<>();
        tokenFetchRetries = 0;
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
        this.userToken = skypeUserToken;
        ISkypetokenProvider skypetokenProvider = new ISkypetokenProvider() {
            @Override
            public String getSkypetoken(boolean forceRefresh) {
                if (forceRefresh) {
                    tokenFetchRetries += 1;
                    if (tokenFetchRetries > NotificationUtils.MAX_TOKEN_FETCH_RETRY_COUNT) {
                        stop();
                        logger.error("Access token is expired and failed to fetch a valid one after "
                            + NotificationUtils.MAX_TOKEN_FETCH_RETRY_COUNT
                            + " retries.");
                        return null;
                    }
                } else {
                    tokenFetchRetries = 0;
                }

                return userToken;
            }
        };

        start(context, skypetokenProvider, skypeUserToken);
    }

    /**
     * Start the realtime connection.
     * @param context the android application context
     * @param errorHandler error handler callback for registration failures
     */
    public void start(Context context, Consumer<Throwable> errorHandler) {
        ISkypetokenProvider skypetokenProvider = new ISkypetokenProvider() {
            @Override
            public String getSkypetoken(boolean forceRefresh) {
                if (forceRefresh) {
                    tokenFetchRetries += 1;
                    if (tokenFetchRetries > NotificationUtils.MAX_TOKEN_FETCH_RETRY_COUNT) {
                        stop();
                        Throwable throwable = new Throwable("Access token is expired and failed to fetch a valid one after "
                            + NotificationUtils.MAX_TOKEN_FETCH_RETRY_COUNT
                            + " retries.");
                        logger.logThrowableAsError(throwable);
                        errorHandler.accept(throwable);
                        return null;
                    }
                } else {
                    tokenFetchRetries = 0;
                }

                String skypeUserToken = null;
                try {
                    skypeUserToken = communicationTokenCredential.getToken().get().getToken();
                } catch (InterruptedException | ExecutionException e) {
                    logger.error("Get skype user token failed for realtime notification: " + e.getMessage());
                    // Return a empty but not null skype user token to trigger retry
                    skypeUserToken = "";
                }

                return skypeUserToken;
            }
        };

        String skypeUserToken = null;
        try {
            skypeUserToken = communicationTokenCredential.getToken().get().getToken();
        } catch (InterruptedException | ExecutionException e) {
            logger.error("Get skype user token failed for realtime notification: " + e.getMessage());
            errorHandler.accept(e);
            return;
        }

        start(context, skypetokenProvider, skypeUserToken);
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

    private void start(Context context, ISkypetokenProvider skypetokenProvider, String skypeUserToken) {
        if (this.isRealtimeNotificationsStarted) {
            return;
        }

        CloudType cloudType = NotificationUtils.getUserCloudTypeFromSkypeToken(skypeUserToken);
        String resourceLocation = NotificationUtils.decodeResourceLocationFromJwtToken(skypeUserToken);
        boolean isEUDBCountry = resourceLocation != null && countriesEUDB.contains(resourceLocation.toLowerCase());
        String trouterUrl;
        String registrarUrl;

        switch (cloudType) {
            case Dod:
                trouterUrl = TROUTER_HOSTNAME_DOD;
                registrarUrl = TROUTER_REGISTRATION_HOSTNAME_DOD;
                break;

            case Gcch:
                trouterUrl = TROUTER_HOSTNAME_GCCH;
                registrarUrl = TROUTER_REGISTRATION_HOSTNAME_GCCH;
                break;

            case Public:
            default:
                trouterUrl = isEUDBCountry ? TROUTER_HOSTNAME_EUDB : TROUTER_HOSTNAME;
                registrarUrl = TROUTER_REGISTRATION_HOSTNAME;
                break;
        }

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
        TrouterUrlRegistrar registrar = new TrouterUrlRegistrar(
            skypetokenProvider,
            registrationData,
            registrarUrl,
            Integer.parseInt(TROUTER_MAX_REGISTRATION_TTLS)
        );

        try {
            trouterListeners.clear();
            trouterClientHost = TrouterClientHost.initialize(context, TROUTER_CLIENT_VERSION);
            trouter = trouterClientHost.createTrouterClient(trouterAuthHeadersProvider,
                new InMemoryConnectionDataCache(), trouterUrl);
            trouter.withRegistrar(registrar);
            trouter.start();
            trouter.setUserActivityState(UserActivityState.ACTIVITY_ACTIVE);
            this.isRealtimeNotificationsStarted = true;
        } catch (Throwable e) {
            logger.error(e.getMessage());
        }
    }
}
