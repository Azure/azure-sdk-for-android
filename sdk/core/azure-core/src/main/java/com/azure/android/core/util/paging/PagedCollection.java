package com.azure.android.core.util.paging;

import androidx.annotation.NonNull;

import java.util.Collections;
import java.util.Iterator;

public class PagedCollection<T, P extends Page<T>> implements Iterable<T> {
    private static final int DEFAULT_PAGE_SIZE = 25; // Is this a good size?

    PageRetriever<T, P> pageRetriever;

    public PagedCollection(PageRetriever<T, P> pageRetriever) {
        this.pageRetriever = pageRetriever;
    }

    public void nextPage(String pageId, int pageSize, PagingCallback<T, P> callback) {
        pageRetriever.getPage(pageId, pageSize, callback);
    }

    // Not sure sync method should be removed.
    public P nextPage(String pageId, int pageSize) {
        return pageRetriever.getPage(pageId, pageSize);
    }

    @NonNull
    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {
            private Iterator<T> itemsIterator;
            private String itNextPageId;

            @Override
            public boolean hasNext() {
                if (itemsIterator == null) {
                    loadItems();
                }

                if (itemsIterator.hasNext()) {
                    return true;
                } else {
                    if (itNextPageId == null) {
                        return false;
                    } else {
                        loadItems();

                        return itemsIterator.hasNext();
                    }
                }
            }

            @Override
            public T next() {
                return itemsIterator.next();
            }

            private void loadItems() {
                pageRetriever.getPage(itNextPageId, DEFAULT_PAGE_SIZE, new PagingCallback<T, P>() {
                    @Override
                    public void onSuccess(P page, String currentPageId, String nextPageId) {
                        itemsIterator =
                            page.getItems() == null ? Collections.emptyIterator() : page.getItems().iterator();
                        itNextPageId = nextPageId; // If null, this is the last page.
                    }

                    @Override
                    public void onFailure(Throwable throwable) {
                        throw new RuntimeException(throwable);
                    }
                });
            }
        };
    }
}
