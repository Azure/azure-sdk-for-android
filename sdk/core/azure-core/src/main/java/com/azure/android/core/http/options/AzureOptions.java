package com.azure.android.core.http.options;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.azure.android.core.http.ServiceClient;
import com.azure.android.core.util.CancellationToken;

public class AzureOptions {
    @Nullable
    String clientRequestId;
    @NonNull
    CancellationToken cancellationToken;

    /**
     * Options for a service operation to be carried out by a {@link ServiceClient}.
     *
     * @param clientRequestId   A client-generated, opaque value with 1KB character limit that is recorded in analytics
     *                          logs. Highly recommended for correlating client-side activities with requests received
     *                          by the server.
     * @param cancellationToken A token used to make a best-effort attempt at canceling a request.
     */
    public AzureOptions(@Nullable String clientRequestId, CancellationToken cancellationToken) {
        this.clientRequestId = clientRequestId;
        this.cancellationToken = cancellationToken == null ? CancellationToken.NONE : cancellationToken;
    }

    /**
     * Get the service operation's unique request ID.
     *
     * @return The request ID.
     */
    @Nullable
    public String getClientRequestId() {
        return clientRequestId;
    }

    /**
     * Get the {@link CancellationToken} associated to the service operation.
     *
     * @return The {@link CancellationToken}.
     */
    @NonNull
    public CancellationToken getCancellationToken() {
        return cancellationToken;
    }
}
