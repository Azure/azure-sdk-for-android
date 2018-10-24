package com.azure.mobile.azuredataandroidexample_java.Controllers;

import android.app.Application;

import com.azure.data.AzureData;
import com.azure.data.model.PermissionMode;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // configure AzureData - fill in your account name and master key
        AzureData.configure(getApplicationContext(), "", "", PermissionMode.All);
    }
}