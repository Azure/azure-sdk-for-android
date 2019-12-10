// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.http.interceptor;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Build;
import android.telephony.TelephonyManager;

import androidx.annotation.NonNull;

import com.azure.android.core.BuildConfig;
import com.azure.android.core.util.CoreUtils;

import java.io.IOException;
import java.util.Locale;

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
    private static final String DEFAULT_USER_AGENT_HEADER = "azsdk-java";

    // From the design guidelines, the default user agent header format is:
    // azsdk-java-<client_lib>/<sdk_version> <platform_info> [<platform_info>] (<library_info>|<application_info>)
    // <user_locale_info>
    private static final String USER_AGENT_FORMAT = DEFAULT_USER_AGENT_HEADER + "-%s/%s [%s] (%s) %s";

    // From the design guidelines, the application id user agent header format is:
    // AzCopy/10.0.4-Preview azsdk-java-<client_lib>/<sdk_version> (<platform_info>) [<library_info>|<application_info>]
    // <user_locale_info>
    private static final String APPLICATION_ID_USER_AGENT_FORMAT = "%s " + USER_AGENT_FORMAT;

    // From the design guidelines, the platform info format is: <os version>
    private static final String PLATFORM_INFO_FORMAT = "%s-%s";

    // TODO: Determine the guidelines for this format
    private static final String LIBRARY_INFO_FORMAT = "%s:%s (%s)";

    // TODO: Determine the guidelines for this format
    private static final String APPLICATION_INFO_FORMAT = "%s:%s, %s";

    // TODO: Determine the guidelines for this format
    private static final String USER_LOCALE_INFO_FORMAT = "%s_%s";

    private final String userAgent;

    // TODO: Create a layer of abstraction so we don't pass the Context directly to this class
    private final Context context;

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
        context = null;

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
        context = null;
        userAgent = String.format(USER_AGENT_FORMAT, sdkName, sdkVersion, getPlatformInfo(), getApplicationInfo(null), getUserLocaleInfo(null));
    }


    /**
     * Creates a UserAgentInterceptor with the {@code sdkName} and {@code sdkVersion} in the User-Agent header value.
     *
     * @param applicationId User specified application Id.
     * @param sdkName       Name of the client library.
     * @param sdkVersion    Version of the client library.
     */
    public UserAgentInterceptor(String applicationId, String sdkName, String sdkVersion) {
        context = null;

        if (applicationId == null) {
            userAgent = String.format(USER_AGENT_FORMAT, sdkName, sdkVersion, getPlatformInfo(), getApplicationInfo(null), getUserLocaleInfo(null));
        } else {
            userAgent = String.format(APPLICATION_ID_USER_AGENT_FORMAT, applicationId, sdkName, sdkVersion,
                getPlatformInfo(), getApplicationInfo(null), getUserLocaleInfo(null));
        }
    }

    /**
     * Creates a UserAgentInterceptor with the {@code sdkName} and {@code sdkVersion} in the User-Agent header value.
     *
     * @param applicationId User specified application Id.
     * @param sdkName       Name of the client library.
     * @param sdkVersion    Version of the client library.
     */
    public UserAgentInterceptor(Context context, String applicationId, String sdkName, String sdkVersion) {
        this.context = context;

        if (applicationId == null) {
            userAgent = String.format(USER_AGENT_FORMAT, sdkName, sdkVersion, getPlatformInfo(), getApplicationInfo(context), getUserLocaleInfo(context));
        } else {
            userAgent = String.format(APPLICATION_ID_USER_AGENT_FORMAT, applicationId, sdkName, sdkVersion,
                getPlatformInfo(), getApplicationInfo(context), getUserLocaleInfo(context));
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

    private static String getPlatformInfo() {
        String deviceName = getDeviceName();
        int osVersion = Build.VERSION.SDK_INT;

        return String.format(PLATFORM_INFO_FORMAT, deviceName, osVersion);
    }

    private static String getLibraryInfo() {
        String libraryName = BuildConfig.LIBRARY_PACKAGE_NAME;
        String libraryVersionName = BuildConfig.VERSION_NAME;
        int libraryVersionCode = BuildConfig.VERSION_CODE;

        return String.format(LIBRARY_INFO_FORMAT, libraryName, libraryVersionCode, libraryVersionName);
    }

    //TODO: Figure if we can get the minSdkVersion programatically and if we can get data such as what is included in
    // BuildConfig. Determine if we can also get the build type (debug, release, etc).
    private static String getApplicationInfo(Context context) {
        String targetSdkVersion = "";
        String applicationId = "";
        String applicationVersion = "";

        if (context != null) {
            targetSdkVersion = Integer.toString(context.getApplicationInfo().targetSdkVersion);
            applicationId = context.getPackageName();

            try {
                applicationVersion = context.getPackageManager().getPackageInfo(applicationId, 0).versionName;
            } catch (PackageManager.NameNotFoundException e) {
                // TODO: Determine if we need to use the ClientLogger here
                e.printStackTrace();
            }
        }

        return String.format(APPLICATION_INFO_FORMAT, applicationId, applicationVersion, targetSdkVersion);
    }

    private static String getUserLocaleInfo(Context context) {
        String region = null;

        if (context != null) {
            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            assert telephonyManager != null;
            final String simCountry = telephonyManager.getSimCountryIso();

            if (simCountry != null && simCountry.length() == 2) {
                // SIM country code is available
                region = simCountry.toLowerCase(Locale.US);
            } else if (telephonyManager.getPhoneType() != TelephonyManager.PHONE_TYPE_CDMA) {
                // Device is not 3G (would be unreliable)
                String networkCountry = telephonyManager.getNetworkCountryIso();

                if (networkCountry != null && networkCountry.length() == 2) {
                    // Network country code is available
                    region = networkCountry.toLowerCase(Locale.US);
                }
            }
        }

        if (CoreUtils.isNullOrEmpty(region)) {
            region = "N/A";
        }

        // Using this instead of Configuration.getLocales() because it's not supported in anything less than Android L24
        String language = Resources.getSystem().getConfiguration().locale.getLanguage();

        return String.format(USER_LOCALE_INFO_FORMAT, language, region);
    }

    private static String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;

        if (model.toLowerCase().startsWith(manufacturer.toLowerCase())) {
            return capitalize(model);
        } else {
            return capitalize(manufacturer) + " " + model;
        }
    }

    private static String capitalize(String str) {
        if (CoreUtils.isNullOrEmpty(str)) {
            return str;
        }

        char[] arr = str.toCharArray();
        boolean capitalizeNext = true;
        StringBuilder stringBuilder = new StringBuilder();

        for (char c : arr) {
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
