package com.azure.android.storage.blob.implementation.models;

import com.azure.android.core.util.CancellationToken;
import com.azure.android.storage.blob.models.ListBlobsIncludeItem;

import java.util.List;

public class ListBlobFlatSegmentOptions {
    private String marker;
    private String prefix;
    private Integer maxResults;
    private List<ListBlobsIncludeItem> include;
    private Integer timeout;
    private String requestId;
    private CancellationToken cancellationToken;

    public ListBlobFlatSegmentOptions() {
    }

    public String getMarker() {
        return marker;
    }

    public ListBlobFlatSegmentOptions setMarker(String marker) {
        this.marker = marker;
        return this;
    }

    public String getPrefix() {
        return prefix;
    }

    public ListBlobFlatSegmentOptions setPrefix(String prefix) {
        this.prefix = prefix;
        return this;
    }

    public Integer getMaxResults() {
        return maxResults;
    }

    public ListBlobFlatSegmentOptions setMaxResults(Integer maxResults) {
        this.maxResults = maxResults;
        return this;
    }

    public List<ListBlobsIncludeItem> getInclude() {
        return include;
    }

    public ListBlobFlatSegmentOptions setInclude(List<ListBlobsIncludeItem> include) {
        this.include = include;
        return this;
    }

    public Integer getTimeout() {
        return timeout;
    }

    public ListBlobFlatSegmentOptions setTimeout(Integer timeout) {
        this.timeout = timeout;
        return this;
    }

    public String getRequestId() {
        return requestId;
    }

    public ListBlobFlatSegmentOptions setRequestId(String requestId) {
        this.requestId = requestId;
        return this;
    }

    public CancellationToken getCancellationToken() {
        return this.cancellationToken == null ? CancellationToken.NONE : cancellationToken;
    }

    public ListBlobFlatSegmentOptions setCancellationToken(CancellationToken cancellationToken) {
        this.cancellationToken = cancellationToken;
        return this;
    }
}
