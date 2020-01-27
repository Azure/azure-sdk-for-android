package com.azure.android.core.provider;

/**
 * Interface for a provider that contains locale information.
 */
public interface LocaleInformationProvider {
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
