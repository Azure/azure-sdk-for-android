package com.azure.android.storage.sample;

import android.app.Application;

import com.azure.android.storage.sample.di.AppComponent;
import com.azure.android.storage.sample.di.DaggerAppComponent;
import com.azure.android.storage.sample.di.StorageBlobModule;

public class MainApplication extends Application {
    private AppComponent appComponent;
    private final static String STORAGE_URL = "https://{storageaccountname}.blob.core.windows.net/";

    @Override
    public void onCreate() {
        super.onCreate();
        // Refer: https://guides.codepath.com/android/dependency-injection-with-dagger-2
        //
        appComponent = DaggerAppComponent.builder()
                .storageBlobModule(new StorageBlobModule(STORAGE_URL))
                .build();
    }

    public AppComponent getAppComponent() {
        return appComponent;
    }
}
