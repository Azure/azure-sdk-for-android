package com.azure.android.storage.blob.models;

import com.azure.android.core.util.CancellationToken;

public class GetBlobPropertiesOptions {
    private String snapshot;
    private Integer timeout;
    private String version;
    private String leaseId;
    private String requestId;
    private CpkInfo cpkInfo;
    private BlobRequestConditions blobRequestConditions;
    private CancellationToken cancellationToken;

    public GetBlobPropertiesOptions() {
    }

    public String getSnapshot() {
        return snapshot;
    }

    public GetBlobPropertiesOptions setSnapshot(String snapshot) {
        this.snapshot = snapshot;
        return this;
    }

    public Integer getTimeout() {
        return timeout;
    }

    public GetBlobPropertiesOptions setTimeout(Integer timeout) {
        this.timeout = timeout;
        return this;
    }

    public String getVersion() {
        return version;
    }

    public GetBlobPropertiesOptions setVersion(String version) {
        this.version = version;
        return this;
    }

    public String getRequestId() {
        return requestId;
    }

    public GetBlobPropertiesOptions setRequestId(String requestId) {
        this.requestId = requestId;
        return this;
    }

    public CpkInfo getCpkInfo() {
        return cpkInfo;
    }

    public GetBlobPropertiesOptions setCpkInfo(CpkInfo cpkInfo) {
        this.cpkInfo = cpkInfo;
        return this;
    }

    public BlobRequestConditions getBlobRequestConditions() {
        return this.blobRequestConditions;
    }

    public GetBlobPropertiesOptions setBlobRequestConditions(BlobRequestConditions blobRequestConditions) {
        this.blobRequestConditions = blobRequestConditions;
        return this;
    }

    public CancellationToken getCancellationToken() {
        return this.cancellationToken == null ? CancellationToken.NONE : cancellationToken;
    }

    public GetBlobPropertiesOptions setCancellationToken(CancellationToken cancellationToken) {
        this.cancellationToken = cancellationToken;
        return this;
    }
}
