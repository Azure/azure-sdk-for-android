package com.azure.android.storage.blob.models;

import java.util.ArrayList;
import java.util.List;

public class BlobsPage {
    private final List<BlobItem> items;
    private final String pageId;
    private final String nextPageId;

    public BlobsPage(List<BlobItem> items, String pageId, String nextPageId) {
        this.items = items == null ? new ArrayList<>() : items;
        this.pageId = pageId;
        this.nextPageId = nextPageId;
    }

    public List<BlobItem>  getItems() {
        return this.items;
    }

    public String getPageId() {
        return this.pageId;
    }

    public String getNextPageId() {
        return this.nextPageId;
    }
}
