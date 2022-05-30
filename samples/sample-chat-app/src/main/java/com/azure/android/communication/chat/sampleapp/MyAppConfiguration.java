package com.azure.android.communication.chat.sampleapp;

import static com.azure.android.communication.chat.sampleapp.ApplicationConstants.TAG;
import static com.azure.android.communication.chat.sampleapp.ApplicationConstants.communicationTokenCredential;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Configuration;
import androidx.work.WorkManager;

import com.azure.android.communication.chat.implementation.notifications.fcm.RegistrationRenewalWorkerFactory;

import java9.util.function.Consumer;

public class MyAppConfiguration extends Application implements Configuration.Provider {
    Consumer<Throwable> exceptionHandler = new Consumer<Throwable>() {
        @Override
        public void accept(Throwable throwable) {
            Log.w(TAG, "Registration failed for push notifications!", throwable);
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        WorkManager.initialize(getApplicationContext(), getWorkManagerConfiguration());
    }

    @NonNull
    @Override
    public Configuration getWorkManagerConfiguration() {
        return new Configuration.Builder().
            setWorkerFactory(new RegistrationRenewalWorkerFactory(communicationTokenCredential, exceptionHandler)).build();
    }
}
