package com.azure.android.core.provider.implementation;

import android.content.Context;
import android.content.pm.PackageManager;

import androidx.annotation.NonNull;

import com.azure.android.core.provider.ApplicationInformationProvider;

/**
 * Provider that contains application and platform information extracted from a {@link Context} object.
 */
public class AndroidApplicationInformationProvider implements ApplicationInformationProvider {
    private final Context context;
    private String applicationId;
    private String applicationVersion;
    private String systemRegion;
    private int targetSdkVersion;

    public AndroidApplicationInformationProvider(@NonNull Context context) {
        this.context = context;
        applicationId = null;
        applicationVersion = null;
        targetSdkVersion = -1;
    }

    @Override
    public String getApplicationId() {
        if (applicationId == null) {
            applicationId = context.getPackageName();
        }

        return applicationId;
    }

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

    @Override
    public int getTargetSdkVersion() {
        if (targetSdkVersion == -1) {
            targetSdkVersion = context.getApplicationInfo().targetSdkVersion;
        }

        return targetSdkVersion;
    }
}
