package com.azure.core.util

import android.content.Context
import android.net.ConnectivityManager

fun isNetworkAvailable(context : Context) : Boolean {
    val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    val activeNetwork = cm.activeNetworkInfo
    return activeNetwork != null && activeNetwork.isConnectedOrConnecting
}