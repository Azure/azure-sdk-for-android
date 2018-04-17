package com.azure.core.util

import android.content.Context
import com.azure.core.network.Connectivity
import com.github.pwittchen.reactivenetwork.library.rx2.ReactiveNetwork
import io.reactivex.schedulers.Schedulers

/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

class ContextProvider {

    companion object {

        lateinit var appContext: Context

        @JvmStatic
        var isOffline = false
            get() {
                Connectivity.addOnChangedCallback {
                    field = false
                }
                return field
            }

        fun init(context: Context) {
            appContext = context.applicationContext
        }
    }
}