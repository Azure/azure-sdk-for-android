// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.http.interceptor;

import androidx.annotation.NonNull;

import com.azure.android.core.provider.ApplicationInformationProvider;
import com.azure.android.core.provider.LocaleInformationProvider;
import com.azure.android.core.provider.PlatformInformationProvider;
import com.azure.android.core.util.CoreUtils;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Pipeline interceptor that adds "User-Agent" header to a request.
 * <p>
 * The format for the "User-Agent" string is outlined in
 * <a href="https://azure.github.io/azure-sdk/general_azurecore.html#telemetry-policy">Azure Core: Telemetry policy</a>.
 */
public class UserAgentInterceptor implements Interceptor {
    private static final String DEFAULT_USER_AGENT_HEADER = "azsdk-android";

    // From the design guidelines, the default user agent header format is:
    // azsdk-android-<sdk_name>/<sdk_version> (<platform_info>; application_info>; <user_locale_info>
    private static final String USER_AGENT_FORMAT = DEFAULT_USER_AGENT_HEADER + "-%s/%s (%s; %s; %s)";

    // From the design guidelines, the application ID user agent header format is:
    // [<application_id>] azsdk-android-<client_lib>/<sdk_version> (<platform_info>; <application_info>;
    // <user_locale_info>
    private static final String APPLICATION_ID_USER_AGENT_FORMAT = "[%s] " + USER_AGENT_FORMAT;

    // From the design guidelines, the platform info user agent header format is:
    // <device_name> - <os_version>
    private static final String PLATFORM_INFO_FORMAT = "%s - %s";

    // From the design guidelines, the application info user agent header format is:
    // <application_name>:<application_version> -> <target_sdk_version>
    private static final String APPLICATION_INFO_FORMAT = "%s:%s -> %s";

    // From the design guidelines, the user locale info user agent header format is:
    // <user_language>_<user_region>
    private static final String USER_LOCALE_INFO_FORMAT = "%s_%s";

    private final String userAgent;

    /**
     * Creates a {@link UserAgentInterceptor} with a default user agent string.
     */
    public UserAgentInterceptor() {
        this(null);
    }

    /**
     * Creates a UserAgentInterceptor with {@code userAgent} as the header value. If {@code userAgent} is {@code null},
     * then the default user agent value is used.
     *
     * @param userAgent The user agent string to add to request headers.
     */
    public UserAgentInterceptor(String userAgent) {
        if (userAgent != null) {
            this.userAgent = userAgent;
        } else {
            this.userAgent = DEFAULT_USER_AGENT_HEADER;
        }
    }

    /**
     * Creates a UserAgentInterceptor with the {@code sdkName} and {@code sdkVersion} in the User-Agent header value.
     * <p>
     *
     * @param sdkName       Name of the client library.
     * @param sdkVersion    Version of the client library.
     */
    public UserAgentInterceptor(String sdkName, String sdkVersion) {
        userAgent = String.format(USER_AGENT_FORMAT, sdkName, sdkVersion, getPlatformInfo(null),
            getApplicationInfo(null), getLocaleInfo(null));
    }


    /**
     * Creates a UserAgentInterceptor with the {@code sdkName} and {@code sdkVersion} in the User-Agent header value.
     *
     * @param applicationId User specified application ID.
     * @param sdkName       Name of the client library.
     * @param sdkVersion    Version of the client library.
     */
    public UserAgentInterceptor(String applicationId, String sdkName, String sdkVersion) {
        if (applicationId == null) {
            userAgent = String.format(USER_AGENT_FORMAT, sdkName, sdkVersion, getPlatformInfo(null),
                getApplicationInfo(null), getLocaleInfo(null));
        } else {
            userAgent = String.format(APPLICATION_ID_USER_AGENT_FORMAT, applicationId, sdkName, sdkVersion,
                getPlatformInfo(null), getApplicationInfo(null), getLocaleInfo(null));
        }
    }

    /**
     * Creates a UserAgentInterceptor with the {@code sdkName} and {@code sdkVersion} in the User-Agent header value.
     *
     * @param applicationInformationProvider Provider that contains application information.
     * @param localeInformationProvider      Provider that contains system locale information.
     * @param applicationId                  User specified application ID.
     * @param sdkName                        Name of the client library.
     * @param sdkVersion                     Version of the client library.
     */
    public UserAgentInterceptor(ApplicationInformationProvider applicationInformationProvider, LocaleInformationProvider localeInformationProvider,
                                PlatformInformationProvider platformInformationProvider, String applicationId,
                                String sdkName, String sdkVersion) {
        if (applicationId == null) {
            userAgent = String.format(USER_AGENT_FORMAT, sdkName, sdkVersion,
                getPlatformInfo(platformInformationProvider), getApplicationInfo(applicationInformationProvider),
                getLocaleInfo(localeInformationProvider));
        } else {
            userAgent = String.format(APPLICATION_ID_USER_AGENT_FORMAT, applicationId, sdkName, sdkVersion,
                getPlatformInfo(platformInformationProvider), getApplicationInfo(applicationInformationProvider),
                getLocaleInfo(localeInformationProvider));
        }
    }

    /**
     * Updates the "User-Agent" header with the value supplied in the interceptor.
     * <p>
     * When the User-Agent header already has a value and it differs from the value used to create this interceptor the
     * User-Agent header is updated by prepending the value in this interceptor.
     * {@inheritDoc}
     */
    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        Request request = chain.request();
        String header = request.header("User-Agent");

        if (header == null || header.contains(DEFAULT_USER_AGENT_HEADER)) {
            header = userAgent;
        } else {
            header = userAgent + " " + header;
        }

        return chain.proceed(request
            .newBuilder()
            .addHeader("User-Agent", header)
            .build());
    }

    /**
     * Retrieves operating system information.
     *
     * @return String containing the device name (maker and model) and OS version.
     */
    private static String getPlatformInfo(PlatformInformationProvider platformInformationProvider) {
        String deviceName = "";
        int osVersion = -1;

        if (platformInformationProvider != null) {
            String manufacturer = platformInformationProvider.getManufacturer();
            String model = platformInformationProvider.getModel();

            if (model.toLowerCase().startsWith(manufacturer.toLowerCase())) {
                deviceName = capitalize(model);
            } else {
                deviceName = capitalize(manufacturer) + " " + model;
            }

            osVersion = platformInformationProvider.getTargetSdkVersion();
        }

        return String.format(PLATFORM_INFO_FORMAT, deviceName, osVersion);
    }

    // TODO: Figure out how to pass the SDK info instead of using Azure Core library BuildConfig data.
    /*private static String getLibraryInfo() {
        String libraryName = BuildConfig.LIBRARY_PACKAGE_NAME;
        String libraryVersionName = BuildConfig.VERSION_NAME;
        int libraryVersionCode = BuildConfig.VERSION_CODE;

        return String.format(LIBRARY_INFO_FORMAT, libraryName, libraryVersionCode, libraryVersionName);
    }*/

    /**
     * Retrieves application information.
     *
     * @return String containing the application's ID, version and target SDK version.
     */
    private static String getApplicationInfo(ApplicationInformationProvider applicationInformationProvider) {
        // TODO: Figure if we can get the minSdkVersion programatically and if we can get data such as what is included in
        // BuildConfig. Determine if we can also get the build type (debug, release, etc).
        String applicationId = "";
        String applicationVersion = "";
        String targetSdkVersion = "";

        if (applicationInformationProvider != null) {
            applicationId = applicationInformationProvider.getApplicationId();
            applicationVersion = applicationInformationProvider.getApplicationVersion();
            targetSdkVersion = Integer.toString(applicationInformationProvider.getTargetSdkVersion());
        }

        return String.format(APPLICATION_INFO_FORMAT, applicationId, applicationVersion, targetSdkVersion);
    }

    /**
     * Retrieves system locale information.
     *
     * @return String containing the system language and region.
     */
    private static String getLocaleInfo(LocaleInformationProvider localeInformationProvider) {
        String region = "";
        String language = "";

        if (localeInformationProvider != null) {
            language = localeInformationProvider.getDefaultSystemLanguage();
            region = localeInformationProvider.getSystemRegion();
        }

        return String.format(USER_LOCALE_INFO_FORMAT, language, region);
    }

    /**
     * Capitalizes a given string
     *
     * @param string String to capitalize.
     * @return String where the first letter of each word is capitalized.
     */
    private static String capitalize(String string) {
        if (CoreUtils.isNullOrEmpty(string)) {
            return string;
        }

        char[] charArray = string.toCharArray();
        boolean capitalizeNext = true;
        StringBuilder stringBuilder = new StringBuilder();

        for (char c : charArray) {
            if (capitalizeNext && Character.isLetter(c)) {
                stringBuilder.append(Character.toUpperCase(c));
                capitalizeNext = false;

                continue;
            } else if (Character.isWhitespace(c)) {
                capitalizeNext = true;
            }

            stringBuilder.append(c);
        }

        return stringBuilder.toString();
    }
}
