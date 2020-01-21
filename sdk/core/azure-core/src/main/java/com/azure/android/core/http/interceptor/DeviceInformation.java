package com.azure.android.core.http.interceptor;

import android.content.Context;
import android.content.res.Configuration;

/**
 * Interface for an object that contains application, platform and locale information.
 */
public interface DeviceInformation {
    /**
     * Creates and returns a default {@link DeviceInformation} in the form of an {@link AndroidInformation} instance.
     *
     * @param configuration The application's context.
     * @param context       The system configuration.
     * @return An {@link AndroidInformation} instance.
     */
    static DeviceInformation getDefault(Configuration configuration, Context context) {
        return new AndroidInformation(configuration, context);
    }

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

    /**
     * Default system language.
     *
     * @return The default system language.
     */
    String getDefaultSystemLanguage();

    /**
     * System region.
     *
     * @return The system region.
     */
    String getSystemRegion();

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
