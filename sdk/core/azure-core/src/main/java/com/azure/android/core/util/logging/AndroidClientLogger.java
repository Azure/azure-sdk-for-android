// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.util.logging;

import android.util.Log;

import androidx.annotation.IntDef;

import org.slf4j.LoggerFactory;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

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
 */
public class AndroidClientLogger implements ClientLogger {
    /**
     * This interface represents the logging levels used in Azure SDKs.
     */
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({DEBUG, INFO, WARNING, ERROR, NOT_SET})
    public @interface LogLevel {}

    /**
     * Indicates that log level is at the debug level.
     */
    public static final int DEBUG = 1;

    /**
     * Indicates that log level is at the informational level.
     */
    public static final int INFO = 2;

    /**
     * Indicates that log level is at the warning level.
     */
    public static final int WARNING = 3;

    /**
     * Indicates that log level is at the error level.
     */
    public static final int ERROR = 4;

    /**
     * Indicates that no log level is set.
     */
    public static final int NOT_SET = 5;

    private final String tag;
    private int logLevel;

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
        this.tag = tag;
    }

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

    public void debug(String message) {
        if (canLogAtLevel(DEBUG)) {
            Log.d(tag, message);
        }
    }

    public void debug(String message, Throwable throwable) {
        if (canLogAtLevel(DEBUG)) {
            Log.d(tag, message, throwable);
        }
    }

    public void info(String message) {
        if (canLogAtLevel(INFO)) {
            Log.i(tag, message);
        }
    }

    public void info(String message, Throwable throwable) {
        if (canLogAtLevel(INFO)) {
            Log.i(tag, message, throwable);
        }
    }

    public void warning(String message) {
        if (canLogAtLevel(WARNING)) {
            Log.w(tag, message);
        }
    }

    public void warning(String message, Throwable throwable) {
        if (canLogAtLevel(WARNING)) {
            Log.w(tag, message, throwable);
        }
    }

    public void error(String message) {
        if (canLogAtLevel(ERROR)) {
            Log.e(tag, message);
        }
    }

    public void error(String message, Throwable throwable) {
        if (canLogAtLevel(ERROR)) {
            Log.e(tag, message, throwable);
        }
    }

    /**
     * Determines if the logger supports logging at the given log level.
     *
     * @param logLevel Logging level to validate.
     * @return Flag indicating if the environment and logger are configured to support logging at the given log level.
     */
    private boolean canLogAtLevel(int logLevel) {
        // Do not log if logLevel is null or not set.
        if (logLevel == NOT_SET) {
            return false;
        }

        return (logLevel >= this.logLevel);
    }
}
