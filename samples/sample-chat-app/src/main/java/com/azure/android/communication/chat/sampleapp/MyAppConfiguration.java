package com.azure.android.communication.chat.sampleapp;

import static com.azure.android.communication.chat.sampleapp.ApplicationConstants.communicationTokenCredential;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.work.Configuration;
import androidx.work.WorkManager;

import com.azure.android.communication.chat.implementation.notifications.fcm.RegistrationRenewalWorkerFactory;

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
            setWorkerFactory(new RegistrationRenewalWorkerFactory(communicationTokenCredential)).build();
    }
}
