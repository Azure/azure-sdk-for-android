package com.azure.android.core.provider;

/**
 * Interface for a provider that contains application information.
 */
public interface ApplicationInformationProvider {
    /**
     * Application ID.
     *
     * @return The application ID.
     */
    String getApplicationId();

    /**
     * Application version.
     *
     * @return The application version.
     */
    String getApplicationVersion();

    /**
     * Application target SDK version.
     *
     * @return The application's target SDK version.
     */
    int getTargetSdkVersion();
}
