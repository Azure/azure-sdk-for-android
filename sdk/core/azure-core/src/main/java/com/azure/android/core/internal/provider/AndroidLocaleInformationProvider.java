// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.internal.provider;

import android.content.res.Configuration;

import androidx.annotation.NonNull;

import com.azure.android.core.provider.LocaleInformationProvider;

/**
 * Provider that contains system locale information extracted using given {@link Configuration} object. The former
 * can be obtained by calling {@code Resources.getSystem().getConfiguration()}.
 */
public class AndroidLocaleInformationProvider implements LocaleInformationProvider {
    private final Configuration configuration;
    private String language;
    private String systemRegion;

    /**
     * Constructor that takes a system {@link Configuration} to extract data from.
     *
     * @param configuration The system configuration.
     */
    public AndroidLocaleInformationProvider(@NonNull Configuration configuration) {
        this.configuration = configuration;
    }

    @Override
    public String getDefaultSystemLanguage() {
        if (language == null) {
            // Using this instead of Configuration.getLocales() because it's not supported in anything less than
            // Android L24
            language = configuration.locale.getLanguage();
        }

        return language;
    }

    @Override
    public String getSystemRegion() {
        if (systemRegion == null) {
            // Using this instead of Configuration.getLocales() because it's not supported in anything less than
            // Android L24
            systemRegion = configuration.locale.getCountry();
        }

        return systemRegion;
    }
}
