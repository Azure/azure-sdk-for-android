package com.azure.core.util

import android.support.annotation.NonNull

/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */
class MapCompat {

    companion object {

        fun <K, V> getOrDefault(@NonNull map: Map<K, V>, key: K, defaultValue: V): V? {

            val v = map[key]

            return if (v != null || map.containsKey(key))
                v
            else
                defaultValue
        }
    }
}