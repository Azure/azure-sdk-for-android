package com.azure.android.core.util.paging;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

public class PagedCollection<T, P extends Page<T>> implements Iterable<T> {
    private static final int DEFAULT_PAGE_SIZE = 25; // Is this a good default size?

    private PageRetriever<T, P> pageRetriever;
    private Integer pageSize;
    private List<T> items;
    private LinkedHashMap<String, P> pages;
    private String nextPageId;

    public PagedCollection(PageRetriever<T, P> pageRetriever) {
        this.pageSize = DEFAULT_PAGE_SIZE;
        this.pageRetriever = pageRetriever;
        items = new ArrayList<>();
        pages = new LinkedHashMap<>();
    }

    public PagedCollection(PageRetriever<T, P> pageRetriever, Integer pageSize) {
        this(pageRetriever);

        if (pageSize == null || pageSize <= 0) {
            throw new IllegalArgumentException("Page size must not be null and must be greater than zero.");
        }

        this.pageSize = pageSize;
    }

    public List<T> getItems() {
        return items;
    }

    public Collection<P> getPages() {
        return pages.values();
    }

    // A null page Id gets the first page from the service. It will get cached with key: null.
    public void getPage(String pageId, PagingCallback<T, P> callback) {
        P page = pages.get(pageId);

        if (page == null) {
            pageRetriever.getPage(pageId, pageSize, new PagingCallback<T, P>() {
                @Override
                public void onSuccess(P page, String currentPageId, String nextPageId) {
                    pages.put(pageId, page);
                    items.addAll(page.getItems());

                    PagedCollection.this.nextPageId = nextPageId; // If null, this is the last page.

                    callback.onSuccess(page, currentPageId, nextPageId);
                }

                @Override
                public void onFailure(Throwable throwable) {
                    callback.onFailure(throwable);
                }
            });
        } else {
            callback.onSuccess(page, page.getPageId(), page.getNextPageId());
            nextPageId = page.getNextPageId();
        }
    }

    @NonNull
    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {
            private Iterator<T> itemsIterator;

            @Override
            public boolean hasNext() {
                if (itemsIterator == null) {
                    loadItems();
                }

                if (itemsIterator.hasNext()) {
                    return true;
                } else {
                    if (nextPageId == null) {
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
                getPage(nextPageId, new PagingCallback<T, P>() {
                    @Override
                    public void onSuccess(P page, String currentPageId, String nextPageId) {
                        itemsIterator =
                            page.getItems() == null ? Collections.emptyIterator() : page.getItems().iterator();
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
