// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.logging.implementation;

import android.util.Log;

import org.slf4j.helpers.MarkerIgnoringBase;
import org.slf4j.helpers.MessageFormatter;

/**
 * This is a fluent logger helper class that implements the logging using the Android
 * {@link Log} class and its methods.
 */
public final class DefaultLogger extends MarkerIgnoringBase {
    private static final long serialVersionUID = 1L;

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
        this.name = LogUtils.ensureValidLoggerName(name);
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
    public void error(final String msg, final Throwable t) {
        Log.e(name, msg, t);
    }

    private String format(final String format, final Object arg1, final Object arg2) {
        return MessageFormatter.format(format, arg1, arg2).getMessage();
    }

    private String format(final String format, final Object[] args) {
        return MessageFormatter.arrayFormat(format, args).getMessage();
    }
}