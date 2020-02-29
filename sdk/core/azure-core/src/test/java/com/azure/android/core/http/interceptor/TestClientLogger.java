package com.azure.android.core.http.interceptor;

import com.azure.android.core.util.logging.ClientLogger;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;

import static com.azure.android.core.http.interceptor.TestUtils.getStackTraceString;

class TestClientLogger implements ClientLogger {
    private final List<AbstractMap.SimpleEntry<Integer, String>> logs = new ArrayList<>();
    private int logLevel;

    List<AbstractMap.SimpleEntry<Integer, String>> getLogs() {
        return logs;
    }

    void clearLogs() {
        logs.clear();
    }

    @Override
    public int getLogLevel() {
        return logLevel;
    }

    @Override
    public void setLogLevel(int logLevel) {
        this.logLevel = logLevel;
    }

    @Override
    public void debug(String message) {
        logs.add(new AbstractMap.SimpleEntry<>(LOG_LEVEL_DEBUG, message));
    }

    @Override
    public void debug(String message, Throwable throwable) {
        logs.add(new AbstractMap.SimpleEntry<>(LOG_LEVEL_DEBUG, message + getStackTraceString(throwable)));
    }

    @Override
    public void info(String message) {
        logs.add(new AbstractMap.SimpleEntry<>(LOG_LEVEL_INFO, message));
    }

    @Override
    public void info(String message, Throwable throwable) {
        logs.add(new AbstractMap.SimpleEntry<>(LOG_LEVEL_INFO, message + getStackTraceString(throwable)));
    }

    @Override
    public void warning(String message) {
        logs.add(new AbstractMap.SimpleEntry<>(LOG_LEVEL_WARNING, message));
    }

    @Override
    public void warning(String message, Throwable throwable) {
        logs.add(new AbstractMap.SimpleEntry<>(LOG_LEVEL_WARNING, message + getStackTraceString(throwable)));
    }

    @Override
    public void error(String message) {
        logs.add(new AbstractMap.SimpleEntry<>(LOG_LEVEL_ERROR, message));
    }

    @Override
    public void error(String message, Throwable throwable) {
        logs.add(new AbstractMap.SimpleEntry<>(LOG_LEVEL_ERROR, message + getStackTraceString(throwable)));
    }
}
