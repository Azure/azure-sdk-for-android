// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.internal.provider;

import android.os.Build;

import com.azure.android.core.provider.PlatformInformationProvider;

import static com.azure.android.core.util.CoreUtil.toTitleCase;

/**
 * Provider that contains platform information extracted from the {@link Build} class.
 */
public final class AndroidPlatformInformationProvider implements PlatformInformationProvider {
    @Override
    public String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;

        if (model.toLowerCase().startsWith(manufacturer.toLowerCase())) {
            return toTitleCase(model).toString();
        } else {
            return toTitleCase(manufacturer) + " " + model;
        }
    }

    @Override
    public int getOsVersion() {
        return Build.VERSION.SDK_INT;
    }
}
