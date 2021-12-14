// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.logging;

import com.azure.android.core.logging.implementation.DefaultLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.NOPLogger;

import java.util.Arrays;

import static com.azure.android.core.logging.implementation.LogUtils.removeNewLinesFromLogMessage;

/**
 * This is a fluent logger helper class that wraps a pluggable {@link Logger}.
 *
 * <p>This logger logs formattable messages that use {@code {}} as the placeholder. When a {@link Throwable throwable}
 * is the last argument of the format varargs and the logger is enabled for
 * {@link ClientLogger#verbose(String, Object...) verbose}, the stack trace for the throwable is logged.</p>
 *
 * <p><strong>Log level hierarchy</strong></p>
 * <ol>
 * <li>{@link ClientLogger#error(String, Object...) Error}</li>
 * <li>{@link ClientLogger#warning(String, Object...) Warning}</li>
 * <li>{@link ClientLogger#info(String, Object...) Info}</li>
 * <li>{@link ClientLogger#verbose(String, Object...) Verbose}</li>
 * </ol>
 */
public class ClientLogger {
    private static final String LINE_SEPARATOR;
    private final Logger logger;

    static {
        final String lineSeparator = System.getProperty("line.separator");
        if (lineSeparator ==  null || lineSeparator.length() == 0) {
            LINE_SEPARATOR = lineSeparator;
        } else {
            LINE_SEPARATOR = "\n";
        }
    }

    /**
     * Retrieves a logger for the passed class using the {@link LoggerFactory}.
     *
     * @param clazz Class creating the logger.
     */
    public ClientLogger(Class<?> clazz) {
        this(clazz.getName());
    }

    /**
     * Retrieves a logger for the passed class name using the {@link LoggerFactory}.
     *
     * @param className Class name creating the logger.
     * @throws RuntimeException it is an error.
     */
    public ClientLogger(String className) {
        Logger initLogger = LoggerFactory.getLogger(className);
        logger = initLogger instanceof NOPLogger ? new DefaultLogger(className) : initLogger;
    }

    /**
     * Logs a message at {@code verbose} log level.
     *
     * @param message The message to log.
     */
    public void verbose(String message) {
        if (logger.isDebugEnabled()) {
            logger.debug(removeNewLinesFromLogMessage(message));
        }
    }

    /**
     * Logs a formattable message that uses {@code {}} as the placeholder at {@code verbose} log level.
     *
     * @param format The formattable message to log.
     * @param args Arguments for the message. If an exception is being logged, the last argument should be the
     *     {@link Throwable}.
     */
    public void verbose(String format, Object... args) {
        if (logger.isDebugEnabled()) {
            performLogging(LogLevel.VERBOSE, false, format, args);
        }
    }

    /**
     * Logs a message at {@code info} log level.
     *
     * @param message The message to log.
     */
    public void info(String message) {
        if (logger.isInfoEnabled()) {
            logger.info(removeNewLinesFromLogMessage(message));
        }
    }

    /**
     * Logs a formattable message that uses {@code {}} as the placeholder at {@code informational} log level.
     *
     * @param format The formattable message to log
     * @param args Arguments for the message. If an exception is being logged, the last argument should be the
     *     {@link Throwable}.
     */
    public void info(String format, Object... args) {
        if (logger.isInfoEnabled()) {
            performLogging(LogLevel.INFORMATIONAL, false, format, args);
        }
    }

    /**
     * Logs a message at {@code warning} log level.
     *
     * @param message The message to log.
     */
    public void warning(String message) {
        if (logger.isWarnEnabled()) {
            logger.warn(removeNewLinesFromLogMessage(message));
        }
    }

    /**
     * Logs a formattable message that uses {@code {}} as the placeholder at {@code warning} log level.
     *
     * @param format The formattable message to log.
     * @param args Arguments for the message. If an exception is being logged, the last argument should be the
     *     {@link Throwable}.
     */
    public void warning(String format, Object... args) {
        if (logger.isWarnEnabled()) {
            performLogging(LogLevel.WARNING, false, format, args);
        }
    }

    /**
     * Logs a message at {@code error} log level.
     *
     * @param message The message to log.
     */
    public void error(String message) {
        if (logger.isErrorEnabled()) {
            logger.error(removeNewLinesFromLogMessage(message));
        }
    }

    /**
     * Logs a formattable message that uses {@code {}} as the placeholder at {@code error} log level.
     *
     * @param format The formattable message to log.
     * @param args Arguments for the message. If an exception is being logged, the last argument should be the
     *     {@link Throwable}.
     */
    public void error(String format, Object... args) {
        if (logger.isErrorEnabled()) {
            performLogging(LogLevel.ERROR, false, format, args);
        }
    }

    /**
     * Logs the {@link RuntimeException} at the warning level and returns it to be thrown.
     * <p>
     * This API covers the cases where a runtime exception type needs to be thrown and logged. If a {@link Throwable} is
     * being logged use {@link #logThrowableAsWarning(Throwable)} instead.
     *
     * @param runtimeException RuntimeException to be logged and returned.
     * @return The passed {@link RuntimeException}.
     * @throws NullPointerException If {@code runtimeException} is {@code null}.
     */
    public RuntimeException logExceptionAsWarning(RuntimeException runtimeException) {
        requireNonNull(runtimeException, "'runtimeException' cannot be null.");

        return logThrowableAsWarning(runtimeException);
    }

    /**
     * Logs the {@link Throwable} at the warning level and returns it to be thrown.
     * <p>
     * This API covers the cases where a checked exception type needs to be thrown and logged. If a {@link
     * RuntimeException} is being logged use {@link #logExceptionAsWarning(RuntimeException)} instead.
     *
     * @param throwable Throwable to be logged and returned.
     * @param <T> Type of the Throwable being logged.
     * @return The passed {@link Throwable}.
     * @throws NullPointerException If {@code throwable} is {@code null}.
     */
    public <T extends Throwable> T logThrowableAsWarning(T throwable) {
        requireNonNull(throwable, "'throwable' cannot be null.");
        if (!logger.isWarnEnabled()) {
            return throwable;
        }

        performLogging(LogLevel.WARNING, true, throwable.getMessage(), throwable);
        return throwable;
    }

    /**
     * Logs the {@link RuntimeException} at the error level and returns it to be thrown.
     * <p>
     * This API covers the cases where a runtime exception type needs to be thrown and logged. If a {@link Throwable} is
     * being logged use {@link #logThrowableAsError(Throwable)} instead.
     *
     * @param runtimeException RuntimeException to be logged and returned.
     * @return The passed {@code RuntimeException}.
     * @throws NullPointerException If {@code runtimeException} is {@code null}.
     */
    public RuntimeException logExceptionAsError(RuntimeException runtimeException) {
        requireNonNull(runtimeException, "'runtimeException' cannot be null.");

        return logThrowableAsError(runtimeException);
    }

    /**
     * Logs the {@link Throwable} at the error level and returns it to be thrown.
     * <p>
     * This API covers the cases where a checked exception type needs to be thrown and logged. If a {@link
     * RuntimeException} is being logged use {@link #logExceptionAsError(RuntimeException)} instead.
     *
     * @param throwable Throwable to be logged and returned.
     * @param <T> Type of the Throwable being logged.
     * @return The passed {@link Throwable}.
     * @throws NullPointerException If {@code throwable} is {@code null}.
     */
    public <T extends Throwable> T logThrowableAsError(T throwable) {
        requireNonNull(throwable, "'throwable' cannot be null.");
        if (!logger.isErrorEnabled()) {
            return throwable;
        }

        performLogging(LogLevel.ERROR, true, throwable.getMessage(), throwable);
        return throwable;
    }

    /*
     * Performs the logging.
     *
     * @param format formattable message.
     * @param args Arguments for the message, if an exception is being logged last argument is the throwable.
     */
    private void performLogging(LogLevel logLevel, boolean isExceptionLogging, String format, Object... args) {
        // If the logging level is less granular than verbose remove the potential throwable from the args.
        String throwableMessage = "";
        if (doesArgsHaveThrowable(args)) {
            // If we are logging an exception the format string is already the exception message, don't append it.
            if (!isExceptionLogging) {
                Object throwable = args[args.length - 1];

                // This is true from before but is needed to appease SpotBugs.
                if (throwable instanceof Throwable) {
                    throwableMessage = ((Throwable) throwable).getMessage();
                }
            }

            /*
             * Environment is logging at a level higher than verbose, strip out the throwable as it would log its
             * stack trace which is only expected when logging at a verbose level.
             */
            if (!logger.isDebugEnabled()) {
                args = removeThrowable(args);
            }
        }

        format = removeNewLinesFromLogMessage(format);

        switch (logLevel) {
            case VERBOSE:
                logger.debug(format, args);
                break;
            case INFORMATIONAL:
                logger.info(format, args);
                break;
            case WARNING:
                if (throwableMessage != null && throwableMessage.length() != 0) {
                    format += LINE_SEPARATOR + throwableMessage;
                }
                logger.warn(format, args);
                break;
            case ERROR:
                if (throwableMessage != null && throwableMessage.length() != 0) {
                    format += LINE_SEPARATOR + throwableMessage;
                }
                logger.error(format, args);
                break;
            default:
                // Don't do anything, this state shouldn't be possible.
                break;
        }

    }

    /**
     * Determines if the app or environment logger support logging at the given log level.
     *
     * @param logLevel Logging level for the log message.
     * @return Flag indicating if the environment and logger are configured to support logging at the given log level.
     */
    public boolean canLogAtLevel(LogLevel logLevel) {
        if (logLevel == null) {
            return false;
        }
        switch (logLevel) {
            case VERBOSE:
                return logger.isDebugEnabled();
            case INFORMATIONAL:
                return logger.isInfoEnabled();
            case WARNING:
                return logger.isWarnEnabled();
            case ERROR:
                return logger.isErrorEnabled();
            default:
                return false;
        }
    }

    /**
     * Determines if the arguments contains a throwable that would be logged, SLF4J logs a throwable if it is the last
     * element in the argument list.
     *
     * @param args The arguments passed to format the log message.
     * @return True if the last element is a throwable, false otherwise.
     */
    private boolean doesArgsHaveThrowable(Object... args) {
        if (args.length == 0) {
            return false;
        }

        return args[args.length - 1] instanceof Throwable;
    }

    /**
     * Removes the last element from the arguments as it is a throwable.
     *
     * @param args The arguments passed to format the log message.
     * @return The arguments with the last element removed.
     */
    private Object[] removeThrowable(Object... args) {
        return Arrays.copyOf(args, args.length - 1);
    }

    /**
     * If the {@code obj} is null then throw a {@link NullPointerException} with the provided {@code message}.
     *
     * @param obj The object to check.
     * @param message The message for the NullPointerException if it's thrown.
     * @param <T> The type of {@code obj}.
     * @return The {@code obj} if not null
     */
    private static <T> T requireNonNull(T obj, String message) {
        if (obj == null) {
            throw new NullPointerException(message);
        } else {
            return obj;
        }
    }
}
