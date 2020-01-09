package com.azure.android.core.provider;

/**
 * Interface for a provider that contains locale information.
 */
public interface LocaleInformationProvider {
    /**
     * Default system language.
     *
     * @return String containing the default system language.
     */
    String getDefaultSystemLanguage();

    /**
     * System region.
     *
     * @return String containing the system region.
     */
    String getSystemRegion();
}
