package com.azure.core.network

interface NetworkConnectivityManager {
    fun registerListener(callback: (Boolean) -> Unit)
    fun startListening()
    fun stopListening()
}