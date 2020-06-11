// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.provider;

import android.content.Context;

import com.azure.android.core.internal.provider.AndroidApplicationInformationProvider;

/**
 * Interface for a provider that contains application information.
 */
public interface ApplicationInformationProvider {
    /**
     * Creates a default {@link ApplicationInformationProvider}.
     *
     * @param context Android {@link Context} object to extract data from.
     * @return A default {@link ApplicationInformationProvider}.
     */
    static ApplicationInformationProvider getDefault(Context context) {
        return new AndroidApplicationInformationProvider(context);
    }

    /**
     * Gets the application ID.
     *
     * @return The application ID.
     */
    String getApplicationId();

    /**
     * Gets the application version.
     *
     * @return The application version.
     */
    String getApplicationVersion();

    /**
     * Gets the application target SDK version.
     *
     * @return The application's target SDK version.
     */
    int getTargetSdkVersion();
}
