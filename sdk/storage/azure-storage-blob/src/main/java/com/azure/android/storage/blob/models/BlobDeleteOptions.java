package com.azure.android.storage.blob.models;

import com.azure.android.core.util.CancellationToken;

public class BlobDeleteOptions {
    private String snapshot;
    private Integer timeout;
    private String version;
    private DeleteSnapshotsOptionType deleteSnapshots;
    private BlobRequestConditions blobRequestConditions;
    private String requestId;
    private CancellationToken cancellationToken;

    public BlobDeleteOptions() {
    }

    public String getSnapshot() {
        return snapshot;
    }

    public BlobDeleteOptions setSnapshot(String snapshot) {
        this.snapshot = snapshot;
        return this;
    }

    public Integer getTimeout() {
        return timeout;
    }

    public BlobDeleteOptions setTimeout(Integer timeout) {
        this.timeout = timeout;
        return this;
    }

    public String getVersion() {
        return version;
    }

    public BlobDeleteOptions setVersion(String version) {
        this.version = version;
        return this;
    }

    public DeleteSnapshotsOptionType getDeleteSnapshots() {
        return deleteSnapshots;
    }

    public BlobDeleteOptions setDeleteSnapshots(DeleteSnapshotsOptionType deleteSnapshots) {
        this.deleteSnapshots = deleteSnapshots;
        return this;
    }

    public String getRequestId() {
        return requestId;
    }

    public BlobDeleteOptions setRequestId(String requestId) {
        this.requestId = requestId;
        return this;
    }

    public BlobRequestConditions getBlobRequestConditions() {
        return this.blobRequestConditions;
    }

    public BlobDeleteOptions setBlobRequestConditions(BlobRequestConditions blobRequestConditions) {
        this.blobRequestConditions = blobRequestConditions;
        return this;
    }

    public CancellationToken getCancellationToken() {
        return this.cancellationToken == null ? CancellationToken.NONE : cancellationToken;
    }

    public BlobDeleteOptions setCancellationToken(CancellationToken cancellationToken) {
        this.cancellationToken = cancellationToken;
        return this;
    }
}
