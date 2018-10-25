package com.azure.mobile.azuredataandroidexample

import android.app.Application
import com.azure.data.AzureData
import com.azure.data.model.PermissionMode

/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

class App : Application() {

    override fun onCreate() {
        super.onCreate()

        // Need to fill in the accountName + master key here!!
        AzureData.configure(this, "", "", PermissionMode.All)

    }
}