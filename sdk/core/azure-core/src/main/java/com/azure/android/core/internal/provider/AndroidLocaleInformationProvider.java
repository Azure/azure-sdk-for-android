// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.internal.provider;

import android.content.Context;

import androidx.annotation.NonNull;

import com.azure.android.core.provider.LocaleInformationProvider;

/**
 * Provider that contains system locale information extracted using given {@link Context} object.
 */
public final class AndroidLocaleInformationProvider implements LocaleInformationProvider {
    private final Context context;
    private String language;
    private String systemRegion;

    /**
     * Constructor that takes an application's {@link Context} to extract data from.
     *
     * @param context The application's context.
     */
    public AndroidLocaleInformationProvider(@NonNull Context context) {
        this.context = context;
    }

    @Override
    public String getDefaultSystemLanguage() {
        if (language == null) {
            // Using this instead of Configuration.getLocales() because it's not supported in anything less than
            // Android L24
            language = context.getResources().getConfiguration().locale.getLanguage();
        }

        return language;
    }

    @Override
    public String getSystemRegion() {
        if (systemRegion == null) {
            // Using this instead of Configuration.getLocales() because it's not supported in anything less than
            // Android L24
            systemRegion = context.getResources().getConfiguration().locale.getCountry();
        }

        return systemRegion;
    }
}
