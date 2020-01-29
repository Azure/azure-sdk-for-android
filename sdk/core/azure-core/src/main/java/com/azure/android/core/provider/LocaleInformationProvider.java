// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.provider;

import android.content.res.Configuration;

import com.azure.android.core.internal.provider.AndroidLocaleInformationProvider;

/**
 * Interface for a provider that contains locale information.
 */
public interface LocaleInformationProvider {
    /**
     * Creates a default {@link LocaleInformationProvider}.
     *
     * @param configuration Android {@link Configuration} object to extract data from.
     * @return A default {@link LocaleInformationProvider}.
     */
    static LocaleInformationProvider getDefault(Configuration configuration) {
        return new AndroidLocaleInformationProvider(configuration);
    }

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
}
