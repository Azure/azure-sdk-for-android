// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.communication.chat.sampleapp;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Configuration;
import androidx.work.WorkManager;

import com.azure.android.communication.chat.implementation.notifications.fcm.RegistrationRenewalWorkerFactory;
import com.azure.android.communication.common.CommunicationTokenCredential;
import com.azure.android.communication.common.CommunicationTokenRefreshOptions;
import com.azure.android.core.logging.ClientLogger;
import com.jakewharton.threetenabp.AndroidThreeTen;

import java.util.concurrent.Callable;

import java9.util.function.Consumer;

public class MyAppConfiguration extends Application implements Configuration.Provider {
    public static String FIRST_USER_ID;

    public static String SECOND_USER_ID = "";

    public static String FIRST_USER_ACCESS_TOKEN;

    public static String ACS_ENDPOINT;

    public static CommunicationTokenCredential COMMUNICATION_TOKEN_CREDENTIAL;

    private ClientLogger logger = new ClientLogger(MyAppConfiguration.class);

    private final static String AZURE_FUNCTION_URL = "https://js-refresh.azurewebsites.net/api/HttpTrigger1";

    Consumer<Throwable> exceptionHandler = new Consumer<Throwable>() {
        @Override
        public void accept(Throwable throwable) {
            logger.warning("Registration failed for push notifications!", throwable);
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        AndroidThreeTen.init(this);
        try {
            UserTokenClient userTokenClient = new UserTokenClient(AZURE_FUNCTION_URL);
            //First user context
            userTokenClient.getNewUserContext();
            ACS_ENDPOINT = userTokenClient.getAcsEndpoint();
            FIRST_USER_ID = userTokenClient.getUserId();
            FIRST_USER_ACCESS_TOKEN = userTokenClient.getUserToken();
            Callable<String> tokenRefresher = () -> {
                return fetchTokenFromMyServerForUser(userTokenClient);
            };

            String init = "eyJhbGciOiJSUzI1NiIsImtpZCI6IjYwNUVCMzFEMzBBMjBEQkRBNTMxODU2MkM4QTM2RDFCMzIyMkE2MTkiLCJ4NXQiOiJZRjZ6SFRDaURiMmxNWVZpeUtOdEd6SWlwaGsiLCJ0eXAiOiJKV1QifQ.eyJza3lwZWlkIjoiYWNzOjU1MzE4NmEwLWI5OWYtNDViNi1hZThkLThhNTFiYTMyMDBmMl8wMDAwMDAxZS1iMGI2LWEyY2YtMzVmMy0zNDNhMGQwMDcxNTIiLCJzY3AiOjE3OTIsImNzaSI6IjE3MDk3MDk0MDciLCJleHAiOjE3MDk3MTMwMDcsInJnbiI6ImFtZXIiLCJhY3NTY29wZSI6ImNoYXQiLCJyZXNvdXJjZUlkIjoiNTUzMTg2YTAtYjk5Zi00NWI2LWFlOGQtOGE1MWJhMzIwMGYyIiwicmVzb3VyY2VMb2NhdGlvbiI6InVuaXRlZHN0YXRlcyIsImlhdCI6MTcwOTcwOTQwN30.GLHke_DKvEfaw-en-N1FHwpYEuzTTLo6r1ag-g9Cs0I-xZSmo_bWLEAy5uB1yRxlIe3zMo4jZqvs43yCGYvNEc2k3OND9PDUFjstLwB-_AC8of6XHmmI36b6ALk0TmdYTRYijGf2fkVPmXowGII7WCdynZqreQGQlkcxtXDm6v5MQcZ870csZu1VO6_4z-JfRMFdrPMQNS_Zi-GQYb6LYPdsqxaJaY48dHfuauVqaYBAbhmMgIU3RJ3L9aPLc9CBvLwGJF5GT0h4TJemhKSrLoQVcmFImJmGOPkF0pDRits_5hkiSpxEbtY7uFy6FRfl76i3FPbBcK7lJ4sJu0AJkQ";
            CommunicationTokenRefreshOptions tokenRefreshOptions = new CommunicationTokenRefreshOptions(tokenRefresher)
                .setRefreshProactively(true)
                .setInitialToken(init);
            COMMUNICATION_TOKEN_CREDENTIAL = new CommunicationTokenCredential(tokenRefreshOptions);

//            //Second user context
//            userTokenClient.getNewUserContext();
//            SECOND_USER_ID = userTokenClient.getUserId();
        } catch (Throwable throwable) {
            //Your handling code
            logger.logThrowableAsError(throwable);
        }
        WorkManager.initialize(getApplicationContext(), getWorkManagerConfiguration());


    }

    private String fetchTokenFromMyServerForUser(UserTokenClient userTokenClient) {
        try {
            Log.i("Refresh", "calling azure function to refresh");
            userTokenClient.getNewUserContext();
            return userTokenClient.getUserToken();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
        return null;
    }

    @NonNull
    @Override
    public Configuration getWorkManagerConfiguration() {
        return new Configuration.Builder().
            setWorkerFactory(new RegistrationRenewalWorkerFactory(COMMUNICATION_TOKEN_CREDENTIAL, exceptionHandler)).build();
    }
}
