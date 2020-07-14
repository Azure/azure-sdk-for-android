package com.azure.android.storage.blob.implementation.models;

import com.azure.android.core.util.CancellationToken;
import com.azure.android.storage.blob.models.DeleteSnapshotsOptionType;

import org.threeten.bp.OffsetDateTime;

public class BlobDeleteOptions {
    private String snapshot;
    private Integer timeout;
    private String version;
    private String leaseId;
    private DeleteSnapshotsOptionType deleteSnapshots;
    private OffsetDateTime ifModifiedSince;
    private OffsetDateTime ifUnmodifiedSince;
    private String ifMatch;
    private String ifNoneMatch;
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

    public String getLeaseId() {
        return leaseId;
    }

    public BlobDeleteOptions setLeaseId(String leaseId) {
        this.leaseId = leaseId;
        return this;
    }

    public DeleteSnapshotsOptionType getDeleteSnapshots() {
        return deleteSnapshots;
    }

    public BlobDeleteOptions setDeleteSnapshots(DeleteSnapshotsOptionType deleteSnapshots) {
        this.deleteSnapshots = deleteSnapshots;
        return this;
    }

    public OffsetDateTime getIfModifiedSince() {
        return ifModifiedSince;
    }

    public BlobDeleteOptions setIfModifiedSince(OffsetDateTime ifModifiedSince) {
        this.ifModifiedSince = ifModifiedSince;
        return this;
    }

    public OffsetDateTime getIfUnmodifiedSince() {
        return ifUnmodifiedSince;
    }

    public BlobDeleteOptions setIfUnmodifiedSince(OffsetDateTime ifUnmodifiedSince) {
        this.ifUnmodifiedSince = ifUnmodifiedSince;
        return this;
    }

    public String getIfMatch() {
        return ifMatch;
    }

    public BlobDeleteOptions setIfMatch(String ifMatch) {
        this.ifMatch = ifMatch;
        return this;
    }

    public String getIfNoneMatch() {
        return ifNoneMatch;
    }

    public BlobDeleteOptions setIfNoneMatch(String ifNoneMatch) {
        this.ifNoneMatch = ifNoneMatch;
        return this;
    }

    public String getRequestId() {
        return requestId;
    }

    public BlobDeleteOptions setRequestId(String requestId) {
        this.requestId = requestId;
        return this;
    }

    public CancellationToken getCancellationToken() {
        return cancellationToken;
    }

    public BlobDeleteOptions setCancellationToken(CancellationToken cancellationToken) {
        this.cancellationToken = cancellationToken;
        return this;
    }
}
