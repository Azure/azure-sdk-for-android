package com.azure.core.network

import android.net.Network
import android.net.NetworkInfo
import com.azure.core.util.ContextProvider
import com.github.pwittchen.reactivenetwork.library.rx2.ReactiveNetwork
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

class Connectivity {

    companion object {

        private var connectivityListener: ((Boolean) -> Unit)? = null

        private var connectivityListenerDisposable: Disposable? = null

        fun registerListener(callback: (Boolean) -> Unit) {
            connectivityListener = callback
        }

        fun startListening() {
            connectivityListenerDisposable = ReactiveNetwork.observeNetworkConnectivity(ContextProvider.appContext)
                    .subscribeOn(Schedulers.io())
                    .subscribe { connectivity ->
                        connectivityListener?.let { it(connectivity.detailedState == NetworkInfo.DetailedState.CONNECTED) }
                    }
        }

        fun stopListening() {
            synchronized(this) {
                connectivityListener = null
                connectivityListenerDisposable?.dispose()
                connectivityListenerDisposable = null
            }
        }
    }
}