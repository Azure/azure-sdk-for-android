package com.azure.android.core.provider.implementation;

import android.os.Build;

import com.azure.android.core.provider.PlatformInformationProvider;

/**
 * Provider that contains platform information extracted from the {@link Build} class.
 */
class AndroidPlatformInformationProvider implements PlatformInformationProvider {
    /**
     * {@inheritDoc}
     */
    @Override
    public String getManufacturer() {
        return Build.MANUFACTURER;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getModel() {
        return Build.MODEL;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getOsVersion() {
        return Build.VERSION.SDK_INT;
    }
}
