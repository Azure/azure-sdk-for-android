package com.azure.android.communication.chat.sampleapp;

import android.app.Application;
import android.content.ContentProvider;

import androidx.annotation.NonNull;
import androidx.work.Configuration;
import androidx.work.WorkManager;

import com.azure.android.communication.chat.implementation.notifications.fcm.RenewTokenWorkerFactory;
import com.azure.android.communication.common.CommunicationTokenCredential;
import static com.azure.android.communication.chat.sampleapp.ApplicationConstants.*;

public class MyAppConfiguration extends Application implements Configuration.Provider {
    @Override
    public void onCreate() {
        super.onCreate();
        WorkManager.initialize(getApplicationContext(), getWorkManagerConfiguration());
    }

    @NonNull
    @Override
    public Configuration getWorkManagerConfiguration() {
        return new Configuration.Builder().
            setWorkerFactory(new RenewTokenWorkerFactory(new CommunicationTokenCredential(firstUserAccessToken))).build();
    }
}
