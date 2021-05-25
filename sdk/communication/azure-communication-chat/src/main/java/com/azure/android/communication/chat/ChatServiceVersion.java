// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.communication.chat;

import com.azure.android.core.util.ServiceVersion;

/**
 * The versions of Chat Service supported by this client library.
 */
public enum ChatServiceVersion implements ServiceVersion {
    V2021_03_07("2021-03-07");

    private final String version;

    ChatServiceVersion(String version) {
        this.version = version;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getVersion() {
        return this.version;
    }

    /**
     * Gets the latest service version supported by this client library.
     *
     * @return The latest {@link ChatServiceVersion}
     */
    public static ChatServiceVersion getLatest() {
        return V2021_03_07;
    }
}
