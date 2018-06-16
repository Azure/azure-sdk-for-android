package com.azure.core.network

import android.annotation.TargetApi
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import com.azure.core.util.ContextProvider

@TargetApi(Build.VERSION_CODES.M)
internal  class MarshmallowNetworkConnectivityManager: NetworkConnectivityManager {

    private var connectivityManager = ContextProvider.appContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private var networkCallback: ConnectivityManager.NetworkCallback? = null

    override fun registerListener(callback: (Boolean) -> Unit) {
        networkCallback = object: ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network?) { callback(true) }

            override fun onLost(network: Network?) { callback(false) }

            override fun onUnavailable() { callback(false) }
        }
    }

    override fun startListening() {
        networkCallback?.let {
            val networkRequest = NetworkRequest.Builder()
                    .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                    .addCapability(NetworkCapabilities.NET_CAPABILITY_NOT_RESTRICTED)
                    .build()

            connectivityManager.registerNetworkCallback(networkRequest, it)
        }
    }

    override fun stopListening() {
        networkCallback?.let { connectivityManager.unregisterNetworkCallback(it) }
    }
}