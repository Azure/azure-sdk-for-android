package com.azure.android.core.util.paging;

public interface PagingCallback<T, P extends Page<T>> {
    void onSuccess(P page, String currentPageId, String nextPageId);

    void onFailure(Throwable throwable);
}
