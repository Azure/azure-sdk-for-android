// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.communication.chat.implementation.notifications.signaling;

/**
 * Connection states
 */
public enum ConnectionState {
    Unknown(0),
    Connected(2),
    Disconnected(3),
    Switching(9);

    private final int value;

    ConnectionState(final int newValue) {
        value = newValue;
    }

    /**
     * Get connection state value
     * @return an integer for connection state
     */
    public int getValue() {
        return value;
    }
}