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

        private var onChangedCallback : (()->Unit)? = null

        @JvmStatic
        var isOffline = false
            @Synchronized
            set(value){
                if (value==true && onChangedCallback==null){
                    // I've gone offline. Connectivity will tell me when my status changes.
                    onChangedCallback = {field = false}
                    Connectivity.addOnChangedCallback { onChangedCallback }
                } else if (value==false && onChangedCallback!=null){
                    // I'm now back online. No need for Connectivity to keep telling me.
                    Connectivity.removeOnChangedCallback { onChangedCallback }
                    onChangedCallback = null
                }
                field = value
            }

        fun init(context: Context) {
            appContext = context.applicationContext
        }
    }
}