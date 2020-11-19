// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.storage.blob.options;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.azure.android.core.annotation.Fluent;
import com.azure.android.core.util.CancellationToken;
import com.azure.android.storage.blob.models.ListBlobsOptions;

import java.util.Objects;

/**
 * Extended options that may be passed when getting properties of a container.
 */
@Fluent
public class ContainerListBlobsOptions {

    private final String containerName;
    private String pageId;
    private ListBlobsOptions listBlobsOptions;
    private Integer timeout;
    private CancellationToken cancellationToken;

    /**
     * @param containerName The container name.
     */
    public ContainerListBlobsOptions(@NonNull String containerName) {
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
     * @return Identifies the portion of the list to be returned.
     */
    @Nullable
    public String getPageId() {
        return pageId;
    }

    /**
     * @param pageId Identifies the portion of the list to be returned.
     * @return The updated options.
     */
    @NonNull
    public ContainerListBlobsOptions setPageId(@Nullable String pageId) {
        this.pageId = pageId;
        return this;
    }

    /**
     * @return {@link ListBlobsOptions}
     */
    @Nullable
    public ListBlobsOptions getListBlobsOptions() {
        return listBlobsOptions;
    }

    /**
     * @param listBlobsOptions {@link ListBlobsOptions}
     * @return The updated options.
     */
    @NonNull
    public ContainerListBlobsOptions setListBlobsOptions(@Nullable ListBlobsOptions listBlobsOptions) {
        this.listBlobsOptions = listBlobsOptions;
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
    public ContainerListBlobsOptions setTimeout(@Nullable Integer timeout) {
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
    public ContainerListBlobsOptions setCancellationToken(@Nullable CancellationToken cancellationToken) {
        this.cancellationToken = cancellationToken;
        return this;
    }
}
