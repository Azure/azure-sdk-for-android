package com.anuchandy.learn.msal;

import android.app.Application;

import com.anuchandy.learn.msal.di.AppComponent;
import com.anuchandy.learn.msal.di.DaggerAppComponent;
import com.anuchandy.learn.msal.di.StorageBlobModule;

public class MainApplication extends Application {
    private AppComponent appComponent;
    private final static String STORAGE_URL = "https://anustorageandroid.blob.core.windows.net/";

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
