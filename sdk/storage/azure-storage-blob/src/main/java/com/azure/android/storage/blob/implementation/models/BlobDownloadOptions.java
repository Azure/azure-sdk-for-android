package com.azure.android.storage.blob.implementation.models;

import com.azure.android.core.util.CancellationToken;
import com.azure.android.storage.blob.models.CpkInfo;

import org.threeten.bp.OffsetDateTime;

public class BlobDownloadOptions {
    private String snapshot;
    private Integer timeout;
    private String range;
    private Boolean rangeGetContentMd5;
    private Boolean rangeGetContentCrc64;
    private String leaseId;
    private OffsetDateTime ifModifiedSince;
    private OffsetDateTime ifUnmodifiedSince;
    private String ifMatch;
    private String ifNoneMatch;
    private String version;
    private String requestId;
    private CpkInfo cpkInfo;
    private CancellationToken cancellationToken;

    public String getSnapshot() {
        return snapshot;
    }

    public BlobDownloadOptions setSnapshot(String snapshot) {
        this.snapshot = snapshot;
        return this;
    }

    public Integer getTimeout() {
        return timeout;
    }

    public BlobDownloadOptions setTimeout(Integer timeout) {
        this.timeout = timeout;
        return this;
    }

    public String getRange() {
        return range;
    }

    public BlobDownloadOptions setRange(String range) {
        this.range = range;
        return this;
    }

    public String getLeaseId() {
        return leaseId;
    }

    public BlobDownloadOptions setLeaseId(String leaseId) {
        this.leaseId = leaseId;
        return this;
    }

    public Boolean getRangeGetContentMd5() {
        return rangeGetContentMd5;
    }

    public BlobDownloadOptions setRangeGetContentMd5(Boolean rangeGetContentMd5) {
        this.rangeGetContentMd5 = rangeGetContentMd5;
        return this;
    }

    public Boolean getRangeGetContentCrc64() {
        return rangeGetContentCrc64;
    }

    public BlobDownloadOptions setRangeGetContentCrc64(Boolean rangeGetContentCrc64) {
        this.rangeGetContentCrc64 = rangeGetContentCrc64;
        return this;
    }

    public OffsetDateTime getIfModifiedSince() {
        return ifModifiedSince;
    }

    public BlobDownloadOptions setIfModifiedSince(OffsetDateTime ifModifiedSince) {
        this.ifModifiedSince = ifModifiedSince;
        return this;
    }

    public OffsetDateTime getIfUnmodifiedSince() {
        return ifUnmodifiedSince;
    }

    public BlobDownloadOptions setIfUnmodifiedSince(OffsetDateTime ifUnmodifiedSince) {
        this.ifUnmodifiedSince = ifUnmodifiedSince;
        return this;
    }

    public String getIfMatch() {
        return ifMatch;
    }

    public BlobDownloadOptions setIfMatch(String ifMatch) {
        this.ifMatch = ifMatch;
        return this;
    }

    public String getIfNoneMatch() {
        return ifNoneMatch;
    }

    public BlobDownloadOptions setIfNoneMatch(String ifNoneMatch) {
        this.ifNoneMatch = ifNoneMatch;
        return this;
    }

    public String getVersion() {
        return version;
    }

    public BlobDownloadOptions setVersion(String version) {
        this.version = version;
        return this;
    }

    public String getRequestId() {
        return requestId;
    }

    public BlobDownloadOptions setRequestId(String requestId) {
        this.requestId = requestId;
        return this;
    }

    public CpkInfo getCpkInfo() {
        return cpkInfo;
    }

    public BlobDownloadOptions setCpkInfo(CpkInfo cpkInfo) {
        this.cpkInfo = cpkInfo;
        return this;
    }

    public CancellationToken getCancellationToken() {
        return this.cancellationToken == null ? CancellationToken.NONE : cancellationToken;
    }

    public BlobDownloadOptions setCancellationToken(CancellationToken cancellationToken) {
        this.cancellationToken = cancellationToken;
        return this;
    }
}
