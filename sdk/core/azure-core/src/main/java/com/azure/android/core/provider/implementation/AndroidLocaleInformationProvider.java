package com.azure.android.core.provider.implementation;

import android.content.Context;
import android.content.res.Configuration;
import android.telephony.TelephonyManager;

import androidx.annotation.NonNull;

import com.azure.android.core.provider.LocaleInformationProvider;

import java.util.Locale;

/**
 * Provider that contains system locale information extracted using a {@link Configuration} and a {@link Context}
 * object. The former can be obtained by calling {@code Resources.getSystem().getConfiguration()}.
 */
public class AndroidLocaleInformationProvider implements LocaleInformationProvider {
    private final Configuration configuration;
    private final Context context;
    private String language;
    private String systemRegion;

    public AndroidLocaleInformationProvider(@NonNull Configuration configuration, @NonNull Context context) {
        this.configuration = configuration;
        this.context = context;
    }

    @Override
    public String getDefaultSystemLanguage() {
        if (language == null) {
            // Using this instead of Configuration.getLocales() because it's not supported in anything less than Android L24
            language = configuration.locale.getLanguage();
        }

        return language;
    }

    @Override
    public String getSystemRegion() {
        if (systemRegion == null) {
            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            assert telephonyManager != null;
            final String simCountry = telephonyManager.getSimCountryIso();

            if (simCountry != null && simCountry.length() == 2) {
                // SIM country code is available
                systemRegion = simCountry.toLowerCase(Locale.US);
            } else if (telephonyManager.getPhoneType() != TelephonyManager.PHONE_TYPE_CDMA) {
                // Device is not 3G (would be unreliable)
                String networkCountry = telephonyManager.getNetworkCountryIso();

                if (networkCountry != null && networkCountry.length() == 2) {
                    // Network country code is available
                    systemRegion = networkCountry.toLowerCase(Locale.US);
                }
            }
        }

        return systemRegion;
    }
}
