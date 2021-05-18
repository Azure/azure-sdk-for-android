// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.rest.cfextensions;

import com.azure.android.core.logging.ClientLogger;
import com.azure.android.core.rest.Page;
import com.azure.android.core.rest.PagedResponse;
import com.azure.android.core.rest.PagedResponseBase;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

import java9.util.concurrent.CompletableFuture;
import java9.util.function.Function;

/**
 * Tests {@link PagedAsyncCollection}.
 */
public class PagedAsyncCollectionTests {
    private final ClientLogger logger = new ClientLogger(PagedAsyncCollectionTests.class);

    @Test
    public void canEnumerateAllPages() throws ExecutionException, InterruptedException {
        final StringPageRetriever pageRetriever = new StringPageRetriever(3, 5);
        final PagedAsyncCollection<String> collection = new PagedAsyncCollection<>(pageRetriever, this.logger);

        CompletableFuture<Void> completableFuture = collection.forEachPage(response -> {
            Assertions.assertNotNull(response);
            return true;
        });

        completableFuture.get();
        Assertions.assertEquals(5, pageRetriever.getCallCount());
    }

    @Test
    public void canStopPageEnumeration() throws ExecutionException, InterruptedException {
        final StringPageRetriever pageRetriever = new StringPageRetriever(3, 5);
        final PagedAsyncCollection<String> collection = new PagedAsyncCollection<>(pageRetriever, this.logger);

        CompletableFuture<Void> completableFuture = collection.forEachPage(response -> {
            Assertions.assertNotNull(response);
            if (response.getContinuationToken().equalsIgnoreCase("3")) {
                return false;
            }
            return true;
        });

        completableFuture.get();
        Assertions.assertEquals(3, pageRetriever.getCallCount());
    }

    @Test
    public void shouldPropagateUserException() {
        final StringPageRetriever pageRetriever = new StringPageRetriever(3, 5);
        final PagedAsyncCollection<String> collection = new PagedAsyncCollection<>(pageRetriever, this.logger);

        CompletableFuture<Void> completableFuture = collection.forEachPage(response -> {
            Assertions.assertNotNull(response);
            if (response.getContinuationToken().equalsIgnoreCase("3")) {
                throw new RuntimeException("user-error");
            }
            return true;
        });

        Throwable exception = Assertions.assertThrows(ExecutionException.class, () -> completableFuture.get());

        Assertions.assertNotNull(exception);
        Assertions.assertNotNull(exception.getCause());
        Assertions.assertTrue(exception.getCause() instanceof RuntimeException);
        Assertions.assertNotNull(exception.getCause().getMessage());
        Assertions.assertTrue(exception.getCause().getMessage().equalsIgnoreCase("user-error"));
        Assertions.assertEquals(3, pageRetriever.getCallCount());
    }

    @Test
    public void shouldPropagateSdkException() {
        final StringPageRetriever pageRetriever = new StringPageRetriever(3, 5, 3);
        final PagedAsyncCollection<String> collection = new PagedAsyncCollection<>(pageRetriever, this.logger);

        CompletableFuture<Void> completableFuture = collection.forEachPage(response -> {
            Assertions.assertNotNull(response);
            return true;
        });

        Throwable exception = Assertions.assertThrows(ExecutionException.class, () -> completableFuture.get());

        Assertions.assertNotNull(exception);
        Assertions.assertNotNull(exception.getCause());
        Assertions.assertTrue(exception.getCause() instanceof UncheckedIOException);
        Assertions.assertEquals(4, pageRetriever.getCallCount());
    }

    private static final class StringPageRetriever
        implements Function<String, CompletableFuture<PagedResponse<String>>> {
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
        public CompletableFuture<PagedResponse<String>> apply(String pageId) {
            this.getCallCount.getAndIncrement();
            if (pageId == null) {
                final String nextPageId = this.pageCount == 1 ? null : "1";
                PagedResponse<String> response = createPagedResponse(getStringElements(0), nextPageId);
                return CompletableFuture.completedFuture(response);
            } else {
                final int currentPageId = Integer.parseInt(pageId);
                if (currentPageId < this.pageCount) {
                    if (this.throwOnPageId != null && throwOnPageId == currentPageId) {
                        return CompletableFuture.failedFuture(
                            new UncheckedIOException(new IOException("IO error on page retrieval.")));
                    } else {
                        final String nextPageId = this.pageCount == currentPageId + 1
                            ? null : String.valueOf(currentPageId + 1);
                        PagedResponse<String> response = createPagedResponse(getStringElements(currentPageId), nextPageId);
                        return CompletableFuture.completedFuture(response);
                    }
                } else {
                    return CompletableFuture.failedFuture(
                        new IndexOutOfBoundsException("pageId is not within the limit."));
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
}
