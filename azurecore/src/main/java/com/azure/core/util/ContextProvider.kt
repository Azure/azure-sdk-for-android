package com.azure.core.util

import android.content.Context
import com.azure.core.network.Connectivity

/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

class ContextProvider {

    companion object {

        lateinit var appContext: Context

        fun init(context: Context) {
            appContext = context.applicationContext
        }
    }
}