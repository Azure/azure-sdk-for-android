package com.azure.core.network

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import com.azure.core.util.ContextProvider

internal class PreLollipopNetworkConnectivityManager: NetworkConnectivityManager {
    
    private var connectivityReceiver: BroadcastReceiver? = null

    override fun registerListener(callback: (Boolean) -> Unit) {
        connectivityReceiver = object: BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                context?.let {
                    val manager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                    val networkInfo = manager.activeNetworkInfo

                    callback(networkInfo != null && networkInfo.isConnected)
                }
            }
        }

    }

    override fun startListening() {
        connectivityReceiver?.let {
            val filter = IntentFilter()

            // ignore deprecation since this is for Pre-Lollipop only
            @Suppress("DEPRECATION")
            filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION)

            ContextProvider.appContext.registerReceiver(it, filter)
        }
    }
    
    override fun stopListening() {
        ContextProvider.appContext.unregisterReceiver(this.connectivityReceiver)
    }
}