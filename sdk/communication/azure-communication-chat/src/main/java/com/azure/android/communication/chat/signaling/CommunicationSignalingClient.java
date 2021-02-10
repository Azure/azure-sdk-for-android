package com.azure.android.communication.chat.signaling;

import android.content.Context;

import com.azure.android.communication.chat.signaling.properties.ChatEventId;
import com.microsoft.trouterclient.ISelfHostedTrouterClient;
import com.microsoft.trouterclient.ITrouterAuthHeadersProvider;
import com.microsoft.trouterclient.ITrouterConnectionDataCache;
import com.microsoft.trouterclient.TrouterClientHost;
import com.microsoft.trouterclient.registration.ISkypetokenProvider;
import com.microsoft.trouterclient.registration.TrouterSkypetokenAuthHeaderProvider;
import com.microsoft.trouterclient.registration.TrouterUrlRegistrar;
import com.microsoft.trouterclient.registration.TrouterUrlRegistrationData;

import java.util.HashSet;
import java.util.Set;

import static com.azure.android.communication.chat.signaling.SignalingConfig.defaultRegistrarHostnameAndBasePath;
import static com.azure.android.communication.chat.signaling.SignalingConfig.defaultTrouterHostname;
import static com.azure.android.communication.chat.signaling.SignalingConfig.platform;
import static com.azure.android.communication.chat.signaling.SignalingConfig.platformUiVersion;
import static com.azure.android.communication.chat.signaling.SignalingConfig.testApplicationId;
import static com.azure.android.communication.chat.signaling.SignalingConfig.testTemplateKey;
import static com.azure.android.communication.chat.signaling.SignalingConfig.trouterClientVersion;

public class CommunicationSignalingClient implements SignalingClient {
    private TrouterClientHost trouterClientHost;
    private ISelfHostedTrouterClient trouter;
    String userToken;
    Set<CommunicationListener> trouterListeners;

    public CommunicationSignalingClient(String userToken, Context context) {
        try {
            this.userToken = userToken;
            trouterClientHost = TrouterClientHost.initialize(context, trouterClientVersion);
        } catch (Throwable t) {
            System.out.println(t.getMessage());
        }
        trouterListeners = new HashSet<>();
    }

    public void start() {
        String skypetoken = userToken;
        ISkypetokenProvider skypetokenProvider = new ISkypetokenProvider() {
            @Override
            public String getSkypetoken(boolean forceRefresh) {
                return skypetoken;
            }
        };

        ITrouterAuthHeadersProvider trouterAuthHeadersProvider = new TrouterSkypetokenAuthHeaderProvider(skypetokenProvider);

        class InMemoryConnectionDataCache implements ITrouterConnectionDataCache {
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

        TrouterUrlRegistrationData registrationData = new TrouterUrlRegistrationData(
            null,
            testApplicationId,
            platform,
            platformUiVersion,
            testTemplateKey,
            null,
            ""
        );
        final TrouterUrlRegistrar registrar = new TrouterUrlRegistrar(
            skypetokenProvider,
            registrationData,
            defaultRegistrarHostnameAndBasePath,
            3600
        );

        try {
            trouter = trouterClientHost.createTrouterClient(trouterAuthHeadersProvider, new InMemoryConnectionDataCache(), defaultTrouterHostname);
            trouter.withRegistrar(registrar);
            trouter.start();
        } catch (Throwable e) {
            System.out.println(e.getMessage());
        }
    }

    public void stop() {
        this.trouter.close();
    }

    @Override
    public void on(String chatEventId, RealTimeNotificationCallback listener) {
        CommunicationListener communicationListener = new CommunicationListener(chatEventId, listener);
        String loggingName = CommunicationSignalingClient.class.getName();
        if (!this.trouterListeners.contains(communicationListener)) {
            switch (ChatEventId.valueOf(chatEventId)) {
                case chatMessageReceived:
                    trouter.registerListener(communicationListener, "/chatMessageReceived", loggingName);
                    break;
                case typingIndicatorReceived:
                    trouter.registerListener(communicationListener, "/typingIndicatorReceived", loggingName);
                    break;
                case readReceiptReceived:
                    trouter.registerListener(communicationListener, "/readReceiptReceived", loggingName);
                    break;
                case chatMessageEdited:
                    trouter.registerListener(communicationListener, "/chatMessageEdited", loggingName);
                    break;
                case chatMessageDeleted:
                    trouter.registerListener(communicationListener, "/chatMessageDeleted", loggingName);
                    break;
                case chatThreadCreated:
                    trouter.registerListener(communicationListener, "/chatThreadCreated", loggingName);
                    break;
                case chatThreadPropertiesUpdated:
                    trouter.registerListener(communicationListener, "/chatThreadPropertiesUpdated", loggingName);
                    break;
                case chatThreadDeleted:
                    trouter.registerListener(communicationListener, "/chatThreadDeleted", loggingName);
                    break;
                case participantsAdded:
                    trouter.registerListener(communicationListener, "/participantsAdded", loggingName);
                    break;
                case participantsRemoved:
                    trouter.registerListener(communicationListener, "/participantsRemoved", loggingName);
                    break;
                default:
                    return;
            }
            this.trouterListeners.add(communicationListener);
        }
    }

    @Override
    public void off(String chatEventId, RealTimeNotificationCallback listener) {
        CommunicationListener trouterListener = new CommunicationListener(chatEventId, listener);
        if (this.trouterListeners.contains(trouterListener)) {
            this.trouter.unregisterListener((trouterListener));
            this.trouterListeners.remove(trouterListener);
        }
    }

}
