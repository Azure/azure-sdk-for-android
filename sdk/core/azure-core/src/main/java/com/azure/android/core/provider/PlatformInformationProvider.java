// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.provider;

import com.azure.android.core.internal.provider.AndroidPlatformInformationProvider;

/**
 * Interface for a provider that contains platform information.
 */
public interface PlatformInformationProvider {
    /**
     * Creates a default {@link PlatformInformationProvider}.
     *
     * @return A default {@link PlatformInformationProvider}.
     */
    static PlatformInformationProvider getDefault() {
        return new AndroidPlatformInformationProvider();
    }

    /**
     * Device name.
     *
     * @return The device name.
     */
    String getDeviceName();

    /**
     * OS version.
     *
     * @return The device's OS version.
     */
    int getOsVersion();
}
