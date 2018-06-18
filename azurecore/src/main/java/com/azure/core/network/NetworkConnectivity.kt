package com.azure.core.network

import android.os.Build

/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

class NetworkConnectivity {

    companion object {
        val manager: NetworkConnectivityManager
            get() =
                when {
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.M        -> MarshmallowNetworkConnectivityManager()
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP -> LollipopNetworkConnectivityManager()
                    else                                                  -> PreLollipopNetworkConnectivityManager()
                }
    }
}