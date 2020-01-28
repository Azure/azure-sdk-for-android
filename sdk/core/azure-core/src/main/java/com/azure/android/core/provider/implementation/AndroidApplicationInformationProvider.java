package com.azure.android.core.provider.implementation;

import android.content.Context;
import android.content.pm.PackageManager;

import androidx.annotation.NonNull;

import com.azure.android.core.provider.ApplicationInformationProvider;

/**
 * Provider that contains application information extracted from a application's {@link Context} object.
 */
public class AndroidApplicationInformationProvider implements ApplicationInformationProvider {
    private final Context context;
    private String applicationId;
    private String applicationVersion;
    private int targetSdkVersion;

    /**
     * Constructor that takes an application's {@link Context} to extract data from.
     *
     * @param context The application's context.
     */
    public AndroidApplicationInformationProvider(@NonNull Context context) {
        this.context = context;
        applicationId = null;
        applicationVersion = null;
        targetSdkVersion = -1;
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
    public int getTargetSdkVersion() {
        if (targetSdkVersion == -1) {
            targetSdkVersion = context.getApplicationInfo().targetSdkVersion;
        }

        return targetSdkVersion;
    }
}
