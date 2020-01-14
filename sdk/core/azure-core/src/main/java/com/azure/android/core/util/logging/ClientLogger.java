// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.util.logging;

public interface ClientLogger {
    /**
     * Logs a message that at the {@code debug} log level.
     *
     * <p><strong>Code samples</strong></p>
     *
     * <p>Logging a message at the debug log level.</p>
     * <p>
     * {@codesnippet com.azure.core.util.logging.clientLogger.info}
     *
     * @param message The message to log
     */
    void debug(String message);

    /**
     * Logs a message that at the {@code debug} log level.
     *
     * <p><strong>Code samples</strong></p>
     *
     * <p>Logging a message at the debug log level.</p>
     * <p>
     * {@codesnippet com.azure.core.util.logging.clientLogger.info}
     *
     * @param message   The message to log
     * @param throwable An exception to log.
     */
    void debug(String message, Throwable throwable);

    /**
     * Logs a message that at the {@code informational} log level.
     *
     * <p><strong>Code samples</strong></p>
     *
     * <p>Logging a message at the informational log level.</p>
     *
     * {@codesnippet com.azure.core.util.logging.clientLogger.info}
     *
     * @param message The message to log
     */
    void info(String message);

    /**
     * Logs a message that at the {@code informational} log level.
     *
     * <p><strong>Code samples</strong></p>
     *
     * <p>Logging a message at the informational log level.</p>
     * <p>
     * {@codesnippet com.azure.core.util.logging.clientLogger.info}
     *
     * @param message   The message to log
     * @param throwable An exception to log.
     */
    void info(String message, Throwable throwable);

    /**
     * Logs a message that at the {@code warning} log level.
     *
     * <p><strong>Code samples</strong></p>
     *
     * <p>Logging a message at the warning log level.</p>
     * <p>
     * {@codesnippet com.azure.core.util.logging.clientLogger.info}
     *
     * @param message The message to log
     */
    void warning(String message);

    /**
     * Logs a message that at the {@code informational} log level.
     *
     * <p><strong>Code samples</strong></p>
     *
     * <p>Logging a message at the informational log level.</p>
     * <p>
     * {@codesnippet com.azure.core.util.logging.clientLogger.info}
     *
     * @param message   The message to log
     * @param throwable An exception to log.
     */
    void warning(String message, Throwable throwable);

    /**
     * Logs a message that at the {@code error} log level.
     *
     * <p><strong>Code samples</strong></p>
     *
     * <p>Logging a message at the error log level.</p>
     * <p>
     * {@codesnippet com.azure.core.util.logging.clientLogger.info}
     *
     * @param message The message to log
     */
    void error(String message);

    /**
     * Logs a message that at the {@code error} log level.
     *
     * <p><strong>Code samples</strong></p>
     *
     * <p>Logging a message at the error log level.</p>
     * <p>
     * {@codesnippet com.azure.core.util.logging.clientLogger.info}
     *
     * @param message   The message to log
     * @param throwable An exception to log.
     */
    void error(String message, Throwable throwable);
}
