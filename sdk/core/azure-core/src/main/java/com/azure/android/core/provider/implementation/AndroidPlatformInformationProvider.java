package com.azure.android.core.provider.implementation;

import android.os.Build;

import com.azure.android.core.provider.PlatformInformationProvider;

/**
 * Provider that contains system information extracted from the {@link Build} class.
 */
class AndroidPlatformInformationProvider implements PlatformInformationProvider {
    @Override
    public String getManufacturer() {
        return Build.MANUFACTURER;
    }

    @Override
    public String getModel() {
        return Build.MODEL;
    }

    @Override
    public int getTargetSdkVersion() {
        return Build.VERSION.SDK_INT;
    }
}
