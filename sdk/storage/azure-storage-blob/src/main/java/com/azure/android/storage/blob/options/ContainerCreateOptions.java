package com.azure.android.storage.blob.options;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.azure.android.core.annotation.Fluent;
import com.azure.android.core.util.CancellationToken;
import com.azure.android.storage.blob.models.PublicAccessType;

import java.util.Map;
import java.util.Objects;

/**
 * Extended options that may be passed when creating a container.
 */
@Fluent
public class ContainerCreateOptions {

    private final String containerName;
    private Map<String, String> metadata;
    private PublicAccessType publicAccessType;
    private Integer timeout;
    private CancellationToken cancellationToken;

    /**
     * @param containerName The container name.
     */
    public ContainerCreateOptions(@NonNull String containerName) {
        Objects.requireNonNull(containerName);
        this.containerName  = containerName;
    }

    /**
     * @return The container name.
     */
    @NonNull
    public String getContainerName() {
        return containerName;
    }

    /**
     * @return The metadata to associate with the container.
     */
    @Nullable
    public Map<String, String> getMetadata() {
        return metadata;
    }

    /**
     * @param metadata  The metadata to associate with the container.
     * @return The updated options.
     */
    @NonNull
    public ContainerCreateOptions setMetadata(@Nullable Map<String, String> metadata) {
        this.metadata = metadata;
        return this;
    }

    /**
     * @return  Specifies how the data in this container is available to the public.
     */
    @Nullable
    public PublicAccessType getPublicAccessType() {
        return publicAccessType;
    }

    /**
     * @param publicAccessType Specifies how the data in this container is available to the public.
     * Pass null for no public access.
     * @return The updated options.
     */
    @NonNull
    public ContainerCreateOptions setPublicAccessType(@Nullable PublicAccessType publicAccessType) {
        this.publicAccessType = publicAccessType;
        return this;
    }

    /**
     * @return The timeout parameter expressed in seconds.
     */
    @Nullable
    public Integer getTimeout() {
        return timeout;
    }

    /**
     * @param timeout The timeout parameter expressed in seconds. For more information, see
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/setting-timeouts-for-blob-service-operations">Setting Timeouts for Blob Service Operations</a>.
     * @return The updated options.
     */
    @NonNull
    public ContainerCreateOptions setTimeout(@Nullable Integer timeout) {
        this.timeout = timeout;
        return this;
    }

    /**
     * @return The token to request cancellation.
     */
    @Nullable
    public CancellationToken getCancellationToken() {
        return cancellationToken;
    }

    /**
     * @param cancellationToken The token to request cancellation.
     * @return The updated options.
     */
    @NonNull
    public ContainerCreateOptions setCancellationToken(@Nullable CancellationToken cancellationToken) {
        this.cancellationToken = cancellationToken;
        return this;
    }

}
