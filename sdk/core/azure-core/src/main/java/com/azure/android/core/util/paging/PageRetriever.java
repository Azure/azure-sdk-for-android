package com.azure.android.core.util.paging;

public interface PageRetriever<T, P extends Page<T>> {
    void getPage(String pageId, int pageSize, PagingCallback<T, P> callback);

    // Not sure sync method should be removed.
    P getPage(String pageId, int pageSize);
}
