package com.azure.mobile.azuredataandroidexample

import android.app.Application
import android.content.Context
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

        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, exception ->
            // Save the fact we crashed out.
            getSharedPreferences(TAG, Context.MODE_PRIVATE).edit()
                    .putBoolean(KEY_APP_CRASHED, true).apply()
            // Chain default exception handler.
            defaultHandler?.uncaughtException(thread, exception)
        }

        val bRestartAfterCrash = getSharedPreferences(TAG, Context.MODE_PRIVATE)
                .getBoolean(KEY_APP_CRASHED, false)
        if (bRestartAfterCrash) {
            // Clear crash flag.
            isRestartedFromCrash = true

            getSharedPreferences(TAG, Context.MODE_PRIVATE).edit()
                    .putBoolean(KEY_APP_CRASHED, false).apply()
        }
    }

    companion object {

        fun activityResumed() {
            isActivityVisible = true
        }

        fun activityPaused() {
            isActivityVisible = false
        }

        var isActivityVisible: Boolean = false
            private set

        private val TAG = "AppDelegate"
        private val KEY_APP_CRASHED = "KEY_APP_CRASHED"

        var isRestartedFromCrash = false
            private set

        fun clearIsRestartedFromCrash() {
            isRestartedFromCrash = false
        }
    }
}