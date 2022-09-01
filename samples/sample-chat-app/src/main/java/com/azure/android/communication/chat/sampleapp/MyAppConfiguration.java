// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.communication.chat.sampleapp;

import static com.azure.android.communication.chat.sampleapp.ApplicationConstants.ACS_ENDPOINT;
import static com.azure.android.communication.chat.sampleapp.ApplicationConstants.COMMUNICATION_TOKEN_CREDENTIAL;
import static com.azure.android.communication.chat.sampleapp.ApplicationConstants.FIRST_USER_ACCESS_TOKEN;
import static com.azure.android.communication.chat.sampleapp.ApplicationConstants.FIRST_USER_ID;
import static com.azure.android.communication.chat.sampleapp.ApplicationConstants.SECOND_USER_ID;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.work.Configuration;
import androidx.work.WorkManager;

import com.azure.android.communication.chat.implementation.notifications.fcm.RegistrationRenewalWorkerFactory;
import com.azure.android.communication.common.CommunicationTokenCredential;
import com.azure.android.core.logging.ClientLogger;

import java9.util.function.Consumer;

public class MyAppConfiguration extends Application implements Configuration.Provider {
    private ClientLogger logger = new ClientLogger(MyAppConfiguration.class);

    private final static String AZURE_FUNTION_URL = "https://acs-chat-js.azurewebsites.net/api/HttpTrigger1?";

    Consumer<Throwable> exceptionHandler = new Consumer<Throwable>() {
        @Override
        public void accept(Throwable throwable) {
            logger.warning("Registration failed for push notifications!", throwable);
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        try {
            UserTokenClient userTokenClient = new UserTokenClient(AZURE_FUNTION_URL);
            //First user context
            userTokenClient.getNewUserContext();
            ACS_ENDPOINT = userTokenClient.getACSEndpoint();
            FIRST_USER_ID = userTokenClient.getUserID();
            FIRST_USER_ACCESS_TOKEN = userTokenClient.getUserToken();
            COMMUNICATION_TOKEN_CREDENTIAL = new CommunicationTokenCredential(FIRST_USER_ACCESS_TOKEN);
            //Second user context
            userTokenClient.getNewUserContext();
            SECOND_USER_ID = userTokenClient.getUserID();
        } catch (Throwable throwable) {
            //Your handling code
            logger.logThrowableAsError(throwable);
        }
        WorkManager.initialize(getApplicationContext(), getWorkManagerConfiguration());
    }

    @NonNull
    @Override
    public Configuration getWorkManagerConfiguration() {
        return new Configuration.Builder().
            setWorkerFactory(new RegistrationRenewalWorkerFactory(COMMUNICATION_TOKEN_CREDENTIAL, exceptionHandler)).build();
    }
}
