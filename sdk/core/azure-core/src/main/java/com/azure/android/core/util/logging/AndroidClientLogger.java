// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.util.logging;

import android.util.Log;

import org.slf4j.LoggerFactory;

/**
 * This is a fluent logger helper class that implements the {@link ClientLogger} interface.
 *
 * <p>This logger logs formattable messages that use {@code {}} as the placeholder. When a {@link Throwable throwable}
 * is the last argument of the format varargs and the logger is enabled for
 * {@link AndroidClientLogger#debug(String) debug}, the stack trace for the throwable is logged.</p>
 *
 * <p><strong>Log level hierarchy</strong></p>
 * <ol>
 * <li>{@link AndroidClientLogger#error(String) Error}</li>
 * <li>{@link AndroidClientLogger#warning(String) Warning}</li>
 * <li>{@link AndroidClientLogger#info(String) Info}</li>
 * <li>{@link AndroidClientLogger#debug(String) Verbose}</li>
 * </ol>
 *
 */
public class AndroidClientLogger implements ClientLogger{
    private final String TAG;
    private LogLevel logLevel;

    /**
     * Retrieves a logger for the passed class using the {@link LoggerFactory}.
     *
     * @param clazz Class creating the logger.
     */
    public AndroidClientLogger(Class<?> clazz) {
        this(clazz.getName());
    }

    /**
     * Retrieves a logger for the passed class name using the {@link LoggerFactory}.
     *
     * @param tag Class name creating the logger.
     */
    public AndroidClientLogger(String tag) {
        TAG = tag;
    }

    public LogLevel getLogLevel() {
        return logLevel;
    }

    public void setLogLevel(LogLevel logLevel) {
        this.logLevel = logLevel;
    }

    public void debug(String message) {
        if (canLogAtLevel(LogLevel.DEBUG)) {
            Log.d(TAG, message);
        }
    }

    public void debug(String message, Throwable throwable) {
        if (canLogAtLevel(LogLevel.DEBUG)) {
            Log.d(TAG, message, throwable);
        }
    }

    public void info(String message) {
        if (canLogAtLevel(LogLevel.INFORMATIONAL)) {
            Log.i(TAG, message);
        }
    }

    public void info(String message, Throwable throwable) {
        if (canLogAtLevel(LogLevel.INFORMATIONAL)) {
            Log.i(TAG, message, throwable);
        }
    }

    public void warning(String message) {
        if (canLogAtLevel(LogLevel.WARNING)) {
            Log.w(TAG, message);
        }
    }

    public void warning(String message, Throwable throwable) {
        if (canLogAtLevel(LogLevel.WARNING)) {
            Log.w(TAG, message, throwable);
        }
    }

    public void error(String message) {
        if (canLogAtLevel(LogLevel.WARNING)) {
            Log.e(TAG, message);
        }
    }

    public void error(String message, Throwable throwable) {
        if (canLogAtLevel(LogLevel.WARNING)) {
            Log.e(TAG, message, throwable);
        }
    }

    /**
     * Determines if the environment and logger support logging at the given log level.
     *
     * @param logLevel Logging level for the log message.
     * @return Flag indicating if the environment and logger are configured to support logging at the given log level.
     */
    private boolean canLogAtLevel(LogLevel logLevel) {
        // Do not log if logLevel is null or not set.
        if (logLevel == null) {
            return false;
        }

        return (logLevel.getLogLevel() >= this.logLevel.getLogLevel());
    }
}
