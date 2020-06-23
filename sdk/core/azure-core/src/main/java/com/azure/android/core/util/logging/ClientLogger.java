// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.util.logging;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public interface ClientLogger {
    /**
     * Creates and returns a default {@link ClientLogger} for the name of the given class.
     *
     * @param clazz Class creating the logger.
     * @return An {@link AndroidClientLogger}.
     */
    static ClientLogger getDefault(Class<?> clazz) {
        return getDefault(clazz.getName());
    }

    /**
     * Creates and returns a default {@link ClientLogger} for the given tag.
     *
     * @param tag Tag for the logger.
     * @return An {@link AndroidClientLogger}.
     */
    static ClientLogger getDefault(String tag) {
        return new AndroidClientLogger(tag);
    }

    /**
     * This interface represents the logging levels used in Azure SDKs.
     */
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({LOG_LEVEL_DEBUG, LOG_LEVEL_INFO, LOG_LEVEL_WARNING, LOG_LEVEL_ERROR})
    @interface LogLevel {
    }

    /**
     * Indicates that log level is at the debug level.
     */
    int LOG_LEVEL_DEBUG = 1;

    /**
     * Indicates that log level is at the informational level.
     */
    int LOG_LEVEL_INFO = 2;

    /**
     * Indicates that log level is at the warning level.
     */
    int LOG_LEVEL_WARNING = 3;

    /**
     * Indicates that log level is at the error level.
     */
    int LOG_LEVEL_ERROR = 4;


    /**
     * Gets the current log level.
     *
     * @return The current log level.
     */
    @LogLevel
    int getLogLevel();

    /**
     * Sets the log level for the logger.
     *
     * @param logLevel The log level.
     */
    void setLogLevel(@LogLevel int logLevel);

    /**
     * Logs a message at the {@code debug} log level.
     *
     * @param message The message to log
     */
    void debug(String message);

    /**
     * Logs a message at the {@code debug} log level.
     *
     * @param message   The message to log
     * @param throwable An exception to log.
     */
    void debug(String message, Throwable throwable);

    /**
     * Logs a message at the {@code informational} log level.
     *
     * @param message The message to log
     */
    void info(String message);

    /**
     * Logs a message at the {@code informational} log level.
     *
     * @param message   The message to log
     * @param throwable An exception to log.
     */
    void info(String message, Throwable throwable);

    /**
     * Logs a message at the {@code warning} log level.
     *
     * @param message The message to log
     */
    void warning(String message);

    /**
     * Logs a message at the {@code informational} log level.
     *
     * @param message   The message to log
     * @param throwable An exception to log.
     */
    void warning(String message, Throwable throwable);

    /**
     * Logs a message at the {@code error} log level.
     *
     * @param message The message to log
     */
    void error(String message);

    /**
     * Logs a message at the {@code error} log level.
     *
     * @param message   The message to log
     * @param throwable An exception to log.
     */
    void error(String message, Throwable throwable);
}
