// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.logging.implementation;

import android.os.Build;
import android.util.Log;

import org.slf4j.helpers.MarkerIgnoringBase;
import org.slf4j.helpers.MessageFormatter;

import java.util.StringTokenizer;

/**
 * This is a fluent logger helper class that implements the logging using the Android
 * {@link Log} class and its methods.
 */
public final class DefaultLogger extends MarkerIgnoringBase {
    private static final long serialVersionUID = 1L;
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
        this.name = forceValidLoggerName(name);
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
     * Returns the short logger tag (up to {@value #TAG_MAX_LENGTH} characters) for the given logger name if the
     * devices API level is <= 25, otherwise, the tag is unchanged. Traditionally loggers are named by fully-qualified
     * Java classes; this method attempts to return a concise identifying part of such names.
     */
    static String forceValidLoggerName(String name) {
        if (name == null) {
            return null;
        } else {
            name = name.trim();
        }

        if (name.length() > TAG_MAX_LENGTH && Build.VERSION.SDK_INT <= 25) {
            final StringTokenizer st = new StringTokenizer(name, ".");

            if (st.hasMoreTokens()) { // Note that empty tokens are skipped, i.e., "aa..bb" has tokens "aa", "bb".
                final StringBuilder sb = new StringBuilder();
                String token;

                do {
                    token = st.nextToken();
                    if (token.length() == 1) { // Token of one character appended as is.
                        sb.append(token);
                        sb.append('.');
                    } else if (st.hasMoreTokens()) { // Truncate all but the last token.
                        sb.append(token.charAt(0));
                        sb.append("*.");
                    } else { // Last token (usually class name) appended as is.
                        sb.append(token);
                    }
                } while (st.hasMoreTokens());

                name = sb.toString();
            }

            // Either we had no useful dot location at all or name still too long.
            // Take leading part and append '*' to indicate that it was truncated.
            if (name.length() > TAG_MAX_LENGTH) {
                name = name.substring(0, TAG_MAX_LENGTH - 1) + '*';
            }
        }

        return name;
    }
}
