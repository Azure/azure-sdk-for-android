package com.azure.android.core.provider;

/**
 * Interface for a provider that contains application and platform information.
 */
public interface ApplicationInformationProvider {

    /**
     * Application ID.
     *
     * @return String containing the application ID.
     */
    String getApplicationId();

    /**
     * Application version.
     *
     * @return String containing the application version.
     */
    String getApplicationVersion();

    /**
     * Application target SDK version.
     *
     * @return int representing the application target SDK version.
     */
    int getTargetSdkVersion();
}
