package com.azure.android.core.util.paging;

import androidx.annotation.NonNull;

import java.util.Iterator;
import java.util.List;

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
            private boolean hasNext;
            private int currentItem;
            private List<T> items;
            private String itNextPageId;

            @Override
            public boolean hasNext() {
                // If there are no items or the iterator is on the last item, get a page from the service.
                if (items == null || currentItem == items.size() - 1) {
                    pageRetriever.getPage(itNextPageId, DEFAULT_PAGE_SIZE, new PagingCallback<T, P>() {
                        @Override
                        public void onSuccess(P page, String currentPageId, String nextPageId) {
                            items = page.getItems();
                            currentItem = 0;
                            itNextPageId = nextPageId;
                            hasNext = items != null;
                        }

                        @Override
                        public void onFailure(Throwable throwable) {
                            throw new RuntimeException(throwable);
                        }
                    });
                }

                return hasNext;
            }

            @Override
            public T next() {
                return items.get(currentItem++);
            }
        };
    }
}
