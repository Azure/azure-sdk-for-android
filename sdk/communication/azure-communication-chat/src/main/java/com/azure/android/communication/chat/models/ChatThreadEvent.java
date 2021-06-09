// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.communication.chat.models;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Chat thread event
 */
public abstract class ChatThreadEvent extends ChatEvent {
    /**
     * Version of the thread. This version is an epoch time in a numeric unsigned Int64 format:
     * `1593117207131`
     */
    @JsonProperty(value = "version")
    private String version;

    /**
     * Gets version of the thread. This version is an epoch time in a numeric unsigned Int64 format:
     * `1593117207131`.
     *
     * @return Value of Version of the thread. This version is an epoch time in a numeric unsigned Int64 format:
     * `1593117207131`.
     */
    public String getVersion() {
        return version;
    }
}
