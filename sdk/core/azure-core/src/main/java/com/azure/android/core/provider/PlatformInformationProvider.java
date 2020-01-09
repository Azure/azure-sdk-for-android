package com.azure.android.core.provider;

/**
 * Interface for a provider that contains system information.
 */
public interface PlatformInformationProvider {
    /**
     * Device manufacturer.
     *
     * @return String containing the device manufacturer.
     */
    String getManufacturer();

    /**
     * Device model.
     *
     * @return String containing the device model.
     */
    String getModel();

    /**
     * Application target SDK version.
     *
     * @return Integer representing the application's target SDK version.
     */
    int getTargetSdkVersion();
}
