package com.azure.android.core.rest.util.paging;

import com.azure.android.core.util.Function;
import com.azure.android.core.util.paging.Page;

import org.junit.jupiter.api.Assertions;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

final class StringPageRetriever
    implements Function<String, PagedResponse<String>> {
    private final AtomicInteger getCallCount = new AtomicInteger();
    private final int pageSize;
    private final int pageCount;
    private final Integer throwOnPageId;

    public StringPageRetriever(int pageSize, int pageCount) {
        this.pageSize = pageSize;
        this.pageCount = pageCount;
        this.throwOnPageId = null;
    }

    public StringPageRetriever(int pageSize, int pageCount, int throwOnPageId) {
        Assertions.assertTrue(pageSize > 0);
        this.pageSize = pageSize;
        Assertions.assertTrue(pageCount > 0);
        this.pageCount = pageCount;
        Assertions.assertTrue(throwOnPageId > 0);
        this.throwOnPageId = throwOnPageId;
    }

    public int getCallCount() {
        return this.getCallCount.get();
    }

    @Override
    public PagedResponse<String> call(String pageId) {
        this.getCallCount.getAndIncrement();
        if (pageId == null) {
            final String nextPageId = this.pageCount == 1 ? null : "1";
            PagedResponse<String> response = createPagedResponse(getStringElements(0), nextPageId);
            return response;
        } else {
            final int currentPageId = Integer.parseInt(pageId);
            if (currentPageId < this.pageCount) {
                if (this.throwOnPageId != null && throwOnPageId == currentPageId) {
                    throw new UncheckedIOException(new IOException("IO error on page retrieval."));
                } else {
                    final String nextPageId = this.pageCount == currentPageId + 1
                        ? null : String.valueOf(currentPageId + 1);
                    PagedResponse<String> response = createPagedResponse(getStringElements(currentPageId), nextPageId);
                    return response;
                }
            } else {
                throw new IndexOutOfBoundsException("pageId is not within the limit.");
            }
        }
    }

    private <T> PagedResponse<T> createPagedResponse(List<T> items, String continuationToken) {
        return new PagedResponseBase<Void, T>(null, 200, null, new Page<String, T>() {
            @Override
            public List<T> getElements() {
                return items;
            }

            @Override
            public String getContinuationToken() {
                return continuationToken;
            }
        }, null);
    }

    private List<String> getStringElements(Integer i) {
        List<String> elements = new ArrayList<>();
        final int start = i * this.pageSize;
        final int end = start + this.pageSize;

        for (int e = start; e < end; e++) {
            elements.add(String.valueOf(e));
        }
        return Collections.unmodifiableList(elements);
    }
}