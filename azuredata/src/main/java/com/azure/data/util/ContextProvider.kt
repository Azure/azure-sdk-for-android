package com.azure.data.util

import android.content.Context

/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

class ContextProvider {

    companion object {

        lateinit var appContext: Context
        var verboseLogging: Boolean = false

        fun init(context: Context, verboseLogging: Boolean = false) {
            this.appContext = context
            this.verboseLogging = verboseLogging
        }
    }
}