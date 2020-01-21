package com.azure.android.core.http.interceptor;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Build;

/**
 * Provider that contains device and application information extracted from {@link Configuration} and {@link Context}
 * objects, as well as the {@link Build} class.
 */
final class AndroidInformation implements DeviceInformation {
    private final Configuration configuration;
    private final Context context;

    private int targetSdkVersion;
    private String applicationId;
    private String applicationVersion;
    private String language;
    private String systemRegion;

    /**
     * Constructor that takes an application's {@link Context} and a system's {@link Configuration} object to extract
     * data from.
     *
     * @param configuration The application's context.
     * @param context       The system configuration.
     */
    AndroidInformation(Configuration configuration, Context context) {
        this.configuration = configuration;
        this.context = context;
        targetSdkVersion = -1;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getTargetSdkVersion() {
        if (targetSdkVersion == -1) {
            targetSdkVersion = context.getApplicationInfo().targetSdkVersion;
        }

        return targetSdkVersion;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getApplicationId() {
        if (applicationId == null) {
            applicationId = context.getPackageName();
        }

        return applicationId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getApplicationVersion() {
        if (applicationVersion == null) {
            try {
                applicationVersion = context.getPackageManager().getPackageInfo(getApplicationId(), 0).versionName;
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }

        return applicationVersion;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDefaultSystemLanguage() {
        if (language == null) {
            // Using this instead of Configuration.getLocales() because it's not supported in anything less than
            // Android L24
            language = configuration.locale.getLanguage();
        }

        return language;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getSystemRegion() {
        if (systemRegion == null) {
            // Using this instead of Configuration.getLocales() because it's not supported in anything less than
            // Android L24
            systemRegion = configuration.locale.getCountry();
        }

        return systemRegion;
    }

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
