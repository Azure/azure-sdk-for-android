// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.http.options;

import androidx.annotation.NonNull;

import com.azure.android.core.http.ServiceClient;
import com.azure.android.core.util.logging.ClientLogger;

/**
 * Options for configuring a {@link ServiceClient}.
 */
public class ClientOptions {
    @NonNull
    private final String apiVersion;
    @NonNull
    private final ClientLogger logger;
    @NonNull
    private final TelemetryOptions telemetryOptions;
    @NonNull
    private final TransportOptions transportOptions;

    /**
     * Creates an instance of {@link ClientOptions} without {@link TelemetryOptions} and {@link TransportOptions}.
     *
     * @param apiVersion The API version of the service to invoke.
     * @param logger     The {@link ClientLogger} to be used by the {@link ServiceClient}.
     */
    public ClientOptions(@NonNull String apiVersion, @NonNull ClientLogger logger) {
        this(apiVersion, logger, new TelemetryOptions(false, null), new TransportOptions(null, null));
    }

    /**
     * Creates an instance of {@link ClientOptions}.
     *
     * @param apiVersion       The API version of the service to invoke.
     * @param logger           The {@link ClientLogger} to be used by the {@link ServiceClient}.
     * @param telemetryOptions Options for configuring telemetry sent by the {@link ServiceClient}.
     * @param transportOptions Options for configuring calls made by the {@link ServiceClient}.
     */
    public ClientOptions(@NonNull String apiVersion, @NonNull ClientLogger logger,
                         @NonNull TelemetryOptions telemetryOptions, @NonNull TransportOptions transportOptions) {
        this.apiVersion = apiVersion;
        this.logger = logger;
        this.telemetryOptions = telemetryOptions;
        this.transportOptions = transportOptions;
    }

    /**
     * Gets the service API version the {@link ServiceClient} will use when making calls.
     *
     * @return The service API version.
     */
    @NonNull
    public String getApiVersion() {
        return apiVersion;
    }

    /**
     * Gets the {@link ServiceClient client}'s logger.
     *
     * @return The {@link ClientLogger}.
     */
    @NonNull
    public ClientLogger getLogger() {
        return logger;
    }

    /**
     * Gets the {@link ServiceClient client}'s {@link TelemetryOptions}.
     *
     * @return The {@link TelemetryOptions}.
     */
    @NonNull
    public TelemetryOptions getTelemetryOptions() {
        return telemetryOptions;
    }

    /**
     * Gets the {@link ServiceClient client}'s {@link TransportOptions}.
     *
     * @return The {@link TransportOptions}.
     */
    @NonNull
    public TransportOptions getTransportOptions() {
        return transportOptions;
    }
}
