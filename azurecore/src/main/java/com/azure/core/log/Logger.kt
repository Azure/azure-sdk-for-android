@file:Suppress("NOTHING_TO_INLINE")

package com.azure.core.log

/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

import android.util.Log
import com.azure.core.http.HttpHeader
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import timber.log.Timber

@PublishedApi internal val defaultLogLevel = Log.VERBOSE
@PublishedApi internal val defaultLogTree  = Timber.DebugTree()

inline fun v(message: () -> String)                 = log(Log.VERBOSE) { Timber.v(message()) }
inline fun v(t: Throwable)                          = log(Log.VERBOSE) { Timber.v(t) }
inline fun v(t: Throwable, message: () -> String)   = log(Log.VERBOSE) { Timber.v(t, message()) }

inline fun d(message: () -> String)                 = log(Log.DEBUG) { Timber.d(message()) }
inline fun d(t: Throwable)                          = log(Log.DEBUG) { Timber.d(t) }
inline fun d(t: Throwable, message: () -> String)   = log(Log.DEBUG) { Timber.d(t, message()) }

inline fun i(message: () -> String)                 = log(Log.INFO) { Timber.i(message()) }
inline fun i(t: Throwable)                          = log(Log.INFO) { Timber.i(t) }
inline fun i(t: Throwable, message: () -> String)   = log(Log.INFO) { Timber.i(t, message()) }

inline fun w(message: () -> String)                 = log(Log.WARN) { Timber.w(message()) }
inline fun w(t: Throwable)                          = log(Log.WARN) { Timber.w(t) }
inline fun w(t: Throwable, message: () -> String)   = log(Log.WARN) { Timber.w(t, message()) }

inline fun e(message: () -> String)                 = log(Log.ERROR) { Timber.e(message()) }
inline fun e(t: Throwable)                          = log(Log.ERROR) { Timber.e(t) }
inline fun e(t: Throwable, message: () -> String)   = log(Log.ERROR) { Timber.e(t, message()) }

inline fun wtf(message: () -> String)               = log(Log.ASSERT) { Timber.wtf(message()) }
inline fun wtf(t: Throwable)                        = log(Log.ASSERT) { Timber.wtf(t) }
inline fun wtf(t: Throwable, message: () -> String) = log(Log.ASSERT) { Timber.wtf(t, message()) }

/**
 * Start logging at the current log level
 */
fun startLogging() {

    if (Timber.treeCount() == 0) {
        Timber.plant(defaultLogTree)
    }
}

/**
 * Start logging at this new log level.
 */
fun startLogging(level: Int = defaultLogLevel) {

    logLevel = level
    startLogging()
}

/**
 * Stop logging.
 */
fun stopLogging() {

    logLevel = defaultLogLevel

    if (Timber.forest().contains(defaultLogTree)) {
        Timber.uproot(defaultLogTree)
    }
}

/**
 *
 */
fun configureNetworkLogging(okHttpBuilder: OkHttpClient.Builder) {

    if (logLevel <= Log.DEBUG) {

        val okHttpLogger = HttpLoggingInterceptor { msg -> d { msg } }

        okHttpLogger.level = HttpLoggingInterceptor.Level.BODY
        // don't log the auth token header
        okHttpLogger.redactHeader(HttpHeader.Authorization.value)

        okHttpBuilder.addInterceptor(okHttpLogger)
    }
}

/**
 * The current log level. Logs at this level or higher will be sent to the system log
 */
var logLevel = defaultLogLevel

/** @suppress */
fun startLogging(tree : Timber.Tree){
    Timber.plant(tree)
}

/** @suppress */
fun stopLogging(tree : Timber.Tree){
    Timber.uproot(tree)
}

/** @suppress */
@PublishedApi
internal inline fun log(level: Int, block: () -> Unit) {

    if (level >= logLevel && Timber.treeCount() > 0) block()
}