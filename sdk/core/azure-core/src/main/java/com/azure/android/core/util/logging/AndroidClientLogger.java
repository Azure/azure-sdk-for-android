// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.util.logging;

import android.util.Log;

/**
 * This is a fluent logger helper class that implements the {@link ClientLogger} interface and uses the Android
 * {@link Log} class and its methods.
 * <p>
 * The default log level is INFO
 * <p>
 * <strong>Log level hierarchy</strong>
 * <ol>
 * <li>{@link AndroidClientLogger#error(String) Error}</li>
 * <li>{@link AndroidClientLogger#warning(String) Warning}</li>
 * <li>{@link AndroidClientLogger#info(String) Info}</li>
 * <li>{@link AndroidClientLogger#debug(String) Verbose}</li>
 */
final class AndroidClientLogger implements ClientLogger {
    private final String tag;

    /**
     * Retrieves a logger for the name of the given class.
     *
     * @param clazz Class creating the logger.
     */
    AndroidClientLogger(Class<?> clazz) {
        this(clazz.getName());
    }

    /**
     * Retrieves a logger for the given tag.
     *
     * @param tag Class name creating the logger.
     */
    AndroidClientLogger(String tag) {
        this.tag = tag;
        logLevel = LOG_LEVEL_INFO;
    }

    /**
     * The log level.
     */
    @LogLevel
    private int logLevel;

    /**
     * Returns this logger's log level.
     *
     * @return The log level.
     */
    @LogLevel
    public int getLogLevel() {
        return logLevel;
    }

    /**
     * Sets the log level for this logger.
     */
    public void setLogLevel(@LogLevel int logLevel) {
        this.logLevel = logLevel;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void debug(String message) {
        if (LOG_LEVEL_DEBUG >= this.logLevel) {
            Log.d(tag, message);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void debug(String message, Throwable throwable) {
        if (LOG_LEVEL_DEBUG >= this.logLevel) {
            Log.d(tag, message, throwable);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void info(String message) {
        if (LOG_LEVEL_INFO >= this.logLevel) {
            Log.i(tag, message);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void info(String message, Throwable throwable) {
        if (LOG_LEVEL_INFO >= this.logLevel) {
            Log.i(tag, message, throwable);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void warning(String message) {
        if (LOG_LEVEL_WARNING>= this.logLevel) {
            Log.w(tag, message);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void warning(String message, Throwable throwable) {
        if (LOG_LEVEL_WARNING>= this.logLevel) {
            Log.w(tag, message, throwable);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void error(String message) {
        if (LOG_LEVEL_ERROR >= this.logLevel) {
            Log.e(tag, message);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void error(String message, Throwable throwable) {
        if (LOG_LEVEL_ERROR >= this.logLevel) {
            Log.e(tag, message, throwable);
        }
    }
}
