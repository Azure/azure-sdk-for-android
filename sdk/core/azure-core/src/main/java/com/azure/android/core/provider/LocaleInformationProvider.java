// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.provider;

import android.content.Context;

/**
 * Interface for a provider that contains locale information.
 */
public interface LocaleInformationProvider {
    /**
     * Creates a default {@link LocaleInformationProvider}.
     *
     * @param context Android {@link Context} object to extract data from.
     * @return A default {@link LocaleInformationProvider}.
     */
    static LocaleInformationProvider getDefault(Context context) {
        return new AndroidLocaleInformationProvider(context);
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
