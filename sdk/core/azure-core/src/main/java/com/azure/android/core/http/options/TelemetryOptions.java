// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.http.options;

import androidx.annotation.Nullable;

import com.azure.android.core.http.ServiceClient;

/**
 * Options for configuring telemetry sent by a {@link ServiceClient}.
 */
public class TelemetryOptions {
    private final boolean telemetryDisabled;
    @Nullable
    private final String applicationId;

    /**
     * Creates an instance of {@link TelemetryOptions}.
     *
     * @param telemetryDisabled Whether platform information will be omitted from the user agent string sent by the
     *                          {@link ServiceClient}.
     * @param applicationId     An optional user-specified application ID included in the user agent string sent by the
     *                          {@link ServiceClient}.
     */
    public TelemetryOptions(boolean telemetryDisabled, @Nullable String applicationId) {
        this.telemetryDisabled = telemetryDisabled;
        this.applicationId = applicationId;
    }

    /**
     * Gets a flag indicating if telemetry is disabled for calls made with the {@link ServiceClient}.
     *
     * @return {@code true} if telemetry is disabled.
     */
    public boolean isTelemetryDisabled() {
        return telemetryDisabled;
    }

    /**
     * Gets the application ID used in calls made by the {@link ServiceClient}.
     *
     * @return The application ID.
     */
    @Nullable
    public String getApplicationId() {
        return applicationId;
    }
}
