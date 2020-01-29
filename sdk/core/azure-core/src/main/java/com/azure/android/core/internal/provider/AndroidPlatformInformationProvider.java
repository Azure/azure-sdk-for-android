// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.internal.provider;

import android.os.Build;

import com.azure.android.core.provider.PlatformInformationProvider;

/**
 * Provider that contains platform information extracted from the {@link Build} class.
 */
public class AndroidPlatformInformationProvider implements PlatformInformationProvider {
    @Override
    public String getManufacturer() {
        return Build.MANUFACTURER;
    }

    @Override
    public String getModel() {
        return Build.MODEL;
    }

    @Override
    public int getOsVersion() {
        return Build.VERSION.SDK_INT;
    }
}
