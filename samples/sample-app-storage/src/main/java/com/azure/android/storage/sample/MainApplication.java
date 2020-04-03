package com.azure.android.storage.sample;

import android.app.Application;

import com.azure.android.storage.sample.config.StorageConfiguration;
import com.azure.android.storage.sample.di.AppComponent;
import com.azure.android.storage.sample.di.DaggerAppComponent;
import com.azure.android.storage.sample.di.StorageBlobModule;

public class MainApplication extends Application {
    private AppComponent appComponent;

    @Override
    public void onCreate() {
        super.onCreate();
        // Refer to: https://guides.codepath.com/android/dependency-injection-with-dagger-2

        String blobServiceUrl = StorageConfiguration.create(getApplicationContext()).getBlobServiceUrl();
        appComponent = DaggerAppComponent.builder()
                .storageBlobModule(new StorageBlobModule(blobServiceUrl))
                .build();
    }

    public AppComponent getAppComponent() {
        return appComponent;
    }
}
