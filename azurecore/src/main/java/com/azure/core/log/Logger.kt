@file:Suppress("NOTHING_TO_INLINE")

package com.azure.core.log

/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

import android.util.Log
import timber.log.Timber

inline fun v(message: () -> String)                 = log(Log.VERBOSE) { Timber.v(message()) }
inline fun v(t: Throwable)                          = log(Log.VERBOSE) {Timber.v(t)}
inline fun v(t: Throwable, message: () -> String)   = log(Log.VERBOSE) { Timber.v(t, message()) }

inline fun d(message: () -> String)                 = log(Log.DEBUG) { Timber.d(message()) }
inline fun d(t: Throwable)                          = log(Log.DEBUG) {Timber.d(t)}
inline fun d(t: Throwable, message: () -> String)   = log(Log.DEBUG) { Timber.d(t, message()) }

inline fun i(message: () -> String)                 = log(Log.DEBUG) { Timber.i(message()) }
inline fun i(t: Throwable)                          = log(Log.DEBUG) {Timber.i(t)}
inline fun i(t: Throwable, message: () -> String)   = log(Log.DEBUG) { Timber.i(t, message()) }

inline fun w(message: () -> String)                 = log(Log.WARN) { Timber.w(message()) }
inline fun w(t: Throwable)                          = log(Log.WARN) {Timber.w(t)}
inline fun w(t: Throwable, message: () -> String)   = log(Log.WARN) { Timber.w(t, message()) }

inline fun e(message: () -> String)                 = log(Log.ERROR) { Timber.e(message()) }
inline fun e(t: Throwable)                          = log(Log.ERROR) {Timber.e(t)}
inline fun e(t: Throwable, message: () -> String)   = log(Log.ERROR) { Timber.e(t, message()) }

inline fun wtf(message: () -> String)               = log(Log.ASSERT) { Timber.wtf(message()) }
inline fun wtf(t: Throwable)                        = log(Log.ASSERT) {Timber.wtf(t)}
inline fun wtf(t: Throwable, message: () -> String) = log(Log.ASSERT) { Timber.wtf(t, message()) }

fun startLogging(level: Int){
    lowestLogLevel = level
    if (Timber.treeCount()==0) {
        Timber.plant(Timber.DebugTree())
    }
}

/** @suppress */
@PublishedApi
internal var lowestLogLevel = Integer.MAX_VALUE

/** @suppress */
@PublishedApi
internal inline fun log(level: Int, block: () -> Unit) {
    if (level>=lowestLogLevel && Timber.treeCount() > 0) block()
}