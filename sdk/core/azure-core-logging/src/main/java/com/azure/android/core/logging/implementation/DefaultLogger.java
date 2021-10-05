// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.logging.implementation;

import android.os.Build;
import android.util.Log;

import org.slf4j.helpers.MarkerIgnoringBase;
import org.slf4j.helpers.MessageFormatter;

/**
 * This is a fluent logger helper class that implements the logging using the Android
 * {@link Log} class and its methods.
 */
public final class DefaultLogger extends MarkerIgnoringBase {
    private static final long serialVersionUID = 1L;
    private static final String ANONYMOUS_TAG = "null";
    private static final int TAG_MAX_LENGTH = 23;

    /**
     * Construct DefaultLogger for the given class.
     *
     * @param clazz Class creating the logger.
     */
    public DefaultLogger(Class<?> clazz) {
        this(clazz.getName());
    }

    /**
     * Construct DefaultLogger.
     *
     * @param name The tag name.
     */
    public DefaultLogger(final String name) {
        this.name = loggerNameToTag(name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isTraceEnabled() {
        return Log.isLoggable(name, Log.VERBOSE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void trace(final String msg) {
        Log.v(name, msg);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void trace(final String format, final Object param1) {
        Log.v(name, format(format, param1, null));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void trace(final String format, final Object param1, final Object param2) {
        Log.v(name, format(format, param1, param2));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void trace(final String format, final Object[] argArray) {
        Log.v(name, format(format, argArray));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void trace(final String msg, final Throwable t) {
        Log.v(name, msg, t);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isDebugEnabled() {
        return Log.isLoggable(name, Log.DEBUG);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void debug(final String msg) {
        Log.d(name, msg);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void debug(final String format, final Object arg1) {
        Log.d(name, format(format, arg1, null));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void debug(final String format, final Object param1, final Object param2) {
        Log.d(name, format(format, param1, param2));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void debug(final String format, final Object[] argArray) {
        Log.d(name, format(format, argArray));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void debug(final String msg, final Throwable t) {
        Log.d(name, msg, t);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isInfoEnabled() {
        return Log.isLoggable(name, Log.INFO);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void info(final String msg) {
        Log.i(name, msg);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void info(final String format, final Object arg) {
        Log.i(name, format(format, arg, null));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void info(final String format, final Object arg1, final Object arg2) {
        Log.i(name, format(format, arg1, arg2));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void info(final String format, final Object[] argArray) {
        Log.i(name, format(format, argArray));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void info(final String msg, final Throwable t) {
        Log.i(name, msg, t);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isWarnEnabled() {
        return Log.isLoggable(name, Log.WARN);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void warn(final String msg) {
        Log.w(name, msg);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void warn(final String format, final Object arg) {
        Log.w(name, format(format, arg, null));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void warn(final String format, final Object arg1, final Object arg2) {
        Log.w(name, format(format, arg1, arg2));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void warn(final String format, final Object[] argArray) {
        Log.w(name, format(format, argArray));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void warn(final String msg, final Throwable t) {
        Log.w(name, msg, t);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isErrorEnabled() {
        return Log.isLoggable(name, Log.ERROR);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void error(final String msg) {
        Log.e(name, msg);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void error(final String format, final Object arg) {
        Log.e(name, format(format, arg, null));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void error(final String format, final Object arg1, final Object arg2) {
        Log.e(name, format(format, arg1, arg2));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void error(final String format, final Object[] argArray) {
        Log.e(name, format(format, argArray));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void error(final String msg, final Throwable t)  {
        Log.e(name, msg, t);
    }

    private String format(final String format, final Object arg1, final Object arg2) {
        return MessageFormatter.format(format, arg1, arg2).getMessage();
    }

    private String format(final String format, final Object[] args) {
        return MessageFormatter.arrayFormat(format, args).getMessage();
    }

    /**
     * Tag names cannot be longer than {@value #TAG_MAX_LENGTH} characters on Android API levels <= 25.
     *
     * <p>Returns the short logger tag (up to {@value #TAG_MAX_LENGTH} characters) for the given logger name if the
     * devices API level is <= 25, otherwise, the tag is unchanged. Traditionally loggers are named by fully-qualified
     * Java classes; this method attempts to return a concise identifying part of such names.</p>
     */
    private static String loggerNameToTag(String loggerName) {
        // Anonymous logger.
        if (loggerName == null) {
            return ANONYMOUS_TAG;
        }

        int length = loggerName.length();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O || length <= TAG_MAX_LENGTH) {
            return loggerName;
        }

        int tagLength = 0;
        int lastTokenIndex = 0;
        int lastPeriodIndex;
        StringBuilder tagName = new StringBuilder(TAG_MAX_LENGTH + 3);

        while ((lastPeriodIndex = loggerName.indexOf('.', lastTokenIndex)) != -1) {
            tagName.append(loggerName.charAt(lastTokenIndex));

            // Token of one character appended as is otherwise truncate it to one character.
            int tokenLength = lastPeriodIndex - lastTokenIndex;

            if (tokenLength > 1) {
                tagName.append('*');
            }

            tagName.append('.');
            lastTokenIndex = lastPeriodIndex + 1;

            // Check if name is already too long
            tagLength = tagName.length();

            if (tagLength > TAG_MAX_LENGTH) {
                return getSimpleName(loggerName);
            }
        }

        // Either we had no useful dot location at all or last token would exceed TAG_MAX_LENGTH
        int tokenLength = length - lastTokenIndex;

        if (tagLength == 0 || (tagLength + tokenLength) > TAG_MAX_LENGTH) {
            return getSimpleName(loggerName);
        }

        // Last token (usually class name) appended as is.
        tagName.append(loggerName, lastTokenIndex, length);

        return tagName.toString();
    }

    private static String getSimpleName(String loggerName) {
        // Take leading part and append '*' to indicate that it was truncated.
        int length = loggerName.length();
        int lastPeriodIndex = loggerName.lastIndexOf('.');

        return lastPeriodIndex != -1
            && length - (lastPeriodIndex + 1) <= TAG_MAX_LENGTH
                ? loggerName.substring(lastPeriodIndex + 1)
                : '*' + loggerName.substring(length - TAG_MAX_LENGTH + 1);
    }
}
