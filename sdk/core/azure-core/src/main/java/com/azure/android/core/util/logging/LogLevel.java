// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.util.logging;

/**
 * Enum which represent logging levels used in Azure SDKs.
 */
public enum LogLevel {
    /**
     * Indicates that log level is at verbose level.
     */
    DEBUG(1),

    /**
     * Indicates that log level is at information level.
     */
    INFORMATIONAL(2),

    /**
     * Indicates that log level is at warning level.
     */
    WARNING(3),

    /**
     * Indicates that log level is at error level.
     */
    ERROR(4),

    /**
     * Indicates that no log level is set.
     */
    NOT_SET(5);

    private final int numericValue;

    LogLevel(int numericValue) {
        this.numericValue = numericValue;
    }

    /**
     * Converts the log level into a numeric representation used for comparisons.
     *
     * @return The numeric representation of the log level.
     */
    public int getLogLevel() {
        return numericValue;
    }
}
