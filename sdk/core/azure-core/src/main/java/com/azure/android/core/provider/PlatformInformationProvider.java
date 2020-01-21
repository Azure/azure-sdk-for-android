package com.azure.android.core.provider;

/**
 * Interface for a provider that contains platform information.
 */
public interface PlatformInformationProvider {
    /**
     * Device manufacturer.
     *
     * @return The device manufacturer.
     */
    String getManufacturer();

    /**
     * Device model.
     *
     * @return The device model.
     */
    String getModel();

    /**
     * OS version.
     *
     * @return The device's OS version.
     */
    int getOsVersion();
}
