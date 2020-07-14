package com.azure.android.storage.blob.implementation.models;

import com.azure.android.core.util.CancellationToken;
import com.azure.android.storage.blob.models.AccessTier;
import com.azure.android.storage.blob.models.BlobHttpHeaders;
import com.azure.android.storage.blob.models.CpkInfo;

import org.threeten.bp.OffsetDateTime;

import java.util.Map;

public class CommitBlockListOptions {
    private byte[] transactionalContentMD5;
    private byte[] transactionalContentCrc64;
    private Integer timeout;
    private BlobHttpHeaders blobHttpHeaders;
    private Map<String, String> metadata;
    private String leaseId;
    private String ifMatch;
    private String ifNoneMatch;
    private OffsetDateTime ifModifiedSince;
    private OffsetDateTime ifUnmodifiedSince;
    private String requestId;
    private CpkInfo cpkInfo;
    private AccessTier tier;
    private CancellationToken cancellationToken;

    public CommitBlockListOptions() {
    }

    public byte[] getTransactionalContentMD5() {
        return transactionalContentMD5;
    }

    public CommitBlockListOptions setTransactionalContentMD5(byte[] transactionalContentMD5) {
        this.transactionalContentMD5 = transactionalContentMD5;
        return this;
    }

    public byte[] getTransactionalContentCrc64() {
        return transactionalContentCrc64;
    }

    public CommitBlockListOptions setTransactionalContentCrc64(byte[] transactionalContentCrc64) {
        this.transactionalContentCrc64 = transactionalContentCrc64;
        return this;
    }

    public Integer getTimeout() {
        return timeout;
    }

    public CommitBlockListOptions setTimeout(Integer timeout) {
        this.timeout = timeout;
        return this;
    }

    public BlobHttpHeaders getBlobHttpHeaders() {
        return blobHttpHeaders;
    }

    public CommitBlockListOptions setBlobHttpHeaders(BlobHttpHeaders blobHttpHeaders) {
        this.blobHttpHeaders = blobHttpHeaders;
        return this;
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }

    public CommitBlockListOptions setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
        return this;
    }

    public String getLeaseId() {
        return leaseId;
    }

    public CommitBlockListOptions setLeaseId(String leaseId) {
        this.leaseId = leaseId;
        return this;
    }

    public String getIfMatch() {
        return ifMatch;
    }

    public CommitBlockListOptions setIfMatch(String ifMatch) {
        this.ifMatch = ifMatch;
        return this;
    }

    public String getIfNoneMatch() {
        return ifNoneMatch;
    }

    public CommitBlockListOptions setIfNoneMatch(String ifNoneMatch) {
        this.ifNoneMatch = ifNoneMatch;
        return this;
    }

    public OffsetDateTime getIfModifiedSince() {
        return ifModifiedSince;
    }

    public CommitBlockListOptions setIfModifiedSince(OffsetDateTime ifModifiedSince) {
        this.ifModifiedSince = ifModifiedSince;
        return this;
    }

    public OffsetDateTime getIfUnmodifiedSince() {
        return ifUnmodifiedSince;
    }

    public CommitBlockListOptions setIfUnmodifiedSince(OffsetDateTime ifUnmodifiedSince) {
        this.ifUnmodifiedSince = ifUnmodifiedSince;
        return this;
    }

    public String getRequestId() {
        return requestId;
    }

    public CommitBlockListOptions setRequestId(String requestId) {
        this.requestId = requestId;
        return this;
    }

    public CpkInfo getCpkInfo() {
        return cpkInfo;
    }

    public CommitBlockListOptions setCpkInfo(CpkInfo cpkInfo) {
        this.cpkInfo = cpkInfo;
        return this;
    }

    public AccessTier getTier() {
        return tier;
    }

    public CommitBlockListOptions setTier(AccessTier tier) {
        this.tier = tier;
        return this;
    }

    public CancellationToken getCancellationToken() {
        return this.cancellationToken == null ? CancellationToken.NONE : cancellationToken;
    }

    public CommitBlockListOptions setCancellationToken(CancellationToken cancellationToken) {
        this.cancellationToken = cancellationToken;
        return this;
    }
}
