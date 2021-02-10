package com.azure.android.communication.chat.signaling;

public enum ConnectionState {
    Unknown(0),
    Connected(2),
    Disconnected(3),
    Switching(9);

    private final int value;

    ConnectionState(final int newValue) {
        value = newValue;
    }

    public int getValue() { return value; }
}
