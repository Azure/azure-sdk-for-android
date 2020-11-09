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

    public ContainerCreateOptions(@NonNull String containerName) {
        Objects.requireNonNull(containerName);
        this.containerName  = containerName;
    }

    @NonNull
    public String getContainerName() {
        return containerName;
    }

    @Nullable
    public Map<String, String> getMetadata() {
        return metadata;
    }

    @NonNull
    public ContainerCreateOptions setMetadata(@Nullable Map<String, String> metadata) {
        this.metadata = metadata;
        return this;
    }

    @Nullable
    public PublicAccessType getPublicAccessType() {
        return publicAccessType;
    }

    @NonNull
    public ContainerCreateOptions setPublicAccessType(@Nullable PublicAccessType publicAccessType) {
        this.publicAccessType = publicAccessType;
        return this;
    }

    @Nullable
    public Integer getTimeout() {
        return timeout;
    }

    @NonNull
    public ContainerCreateOptions setTimeout(@Nullable Integer timeout) {
        this.timeout = timeout;
        return this;
    }

    @Nullable
    public CancellationToken getCancellationToken() {
        return cancellationToken;
    }

    @NonNull
    public ContainerCreateOptions setCancellationToken(@Nullable CancellationToken cancellationToken) {
        this.cancellationToken = cancellationToken;
        return this;
    }

}
