// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.rest;

import com.azure.android.core.logging.ClientLogger;
import com.azure.android.core.util.paging.Page;
import com.azure.android.core.util.AsyncStreamHandler;
import com.azure.android.core.util.CancellationToken;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import java9.util.concurrent.CompletableFuture;
import java9.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * Tests {@link PagedAsyncStream}.
 */
public class PagedAsyncCollectionTests {
    private final ClientLogger logger = new ClientLogger(PagedAsyncCollectionTests.class);

    @Test
    public void canEnumerateAllPages() {
        final StringPageRetriever pageRetriever = new StringPageRetriever(3, 5);
        //
        final PagedAsyncStream<String> collection = new PagedAsyncStream<>(pageId -> {
            final CFBackedPageAsyncStream backingStream = new CFBackedPageAsyncStream<>(pageRetriever,
                continuationToken -> continuationToken != null,
                pageId,
                logger);
            return backingStream;
        }, this.logger);

        CountDownLatch latch = new CountDownLatch(1);
        CancellationToken token = collection.byPage().forEach(new AsyncStreamHandler<PagedResponse<String>>() {
            @Override
            public void onNext(PagedResponse<String> response) {
                Assertions.assertNotNull(response);
            }

            @Override
            public void onError(Throwable throwable) {
                latch.countDown();
            }

            @Override
            public void onComplete() {
                latch.countDown();
            }
        });

        awaitOnLatch(latch, "canEnumerateAllPages");
        Assertions.assertEquals(5, pageRetriever.getCallCount());
    }

    @Test
    public void canStopPageEnumeration() {
        final StringPageRetriever pageRetriever = new StringPageRetriever(3, 5);
        //
        final PagedAsyncStream<String> collection = new PagedAsyncStream<>(pageId -> {
            final CFBackedPageAsyncStream backingStream = new CFBackedPageAsyncStream<>(pageRetriever,
                continuationToken -> continuationToken != null,
                pageId,
                logger);
            return backingStream;
        }, this.logger);

        CountDownLatch latch = new CountDownLatch(1);
        Throwable [] error = new Throwable[1];
        collection.byPage().forEach(new AsyncStreamHandler<PagedResponse<String>>() {
            private CancellationToken token;
            @Override
            public void onInit(CancellationToken cancellationToken) {
                this.token = cancellationToken;
            }

            @Override
            public void onNext(PagedResponse<String> response) {
                Assertions.assertNotNull(response);
                if (response.getContinuationToken().equalsIgnoreCase("3")) {
                    token.cancel();
                }
            }

            @Override
            public void onError(Throwable throwable) {
                error[0] = throwable;
                latch.countDown();
            }

            @Override
            public void onComplete() {
                latch.countDown();
            }
        });

        awaitOnLatch(latch, "canStopPageEnumeration");
        Assertions.assertEquals(3, pageRetriever.getCallCount());
        Assertions.assertNotNull(error[0]);
        Assertions.assertTrue(error[0] instanceof CancellationException);
    }

    @Test
    public void shouldPropagateUserException() {
        final StringPageRetriever pageRetriever = new StringPageRetriever(3, 5);
        //
        final PagedAsyncStream<String> collection = new PagedAsyncStream<>(pageId -> {
            final CFBackedPageAsyncStream backingStream = new CFBackedPageAsyncStream<>(pageRetriever,
                continuationToken -> continuationToken != null,
                pageId,
                logger);
            return backingStream;
        }, this.logger);

        CountDownLatch latch = new CountDownLatch(1);
        Throwable [] error = new Throwable[1];
        collection.byPage().forEach(new AsyncStreamHandler<PagedResponse<String>>() {
            @Override
            public void onNext(PagedResponse<String> response) {
                Assertions.assertNotNull(response);
                if (response.getContinuationToken().equalsIgnoreCase("3")) {
                    throw new RuntimeException("user-error");
                }
            }

            @Override
            public void onError(Throwable throwable) {
                error[0] = throwable;
                latch.countDown();
            }

            @Override
            public void onComplete() {
                latch.countDown();
            }
        });

        awaitOnLatch(latch, "shouldPropagateUserException");

        Assertions.assertNotNull(error[0]);
        Assertions.assertNotNull(error[0].getCause());
        Assertions.assertTrue(error[0].getCause() instanceof RuntimeException);
        Assertions.assertNotNull(error[0].getCause().getMessage());
        Assertions.assertTrue(error[0].getCause().getMessage().equalsIgnoreCase("user-error"));
        Assertions.assertEquals(3, pageRetriever.getCallCount());
    }

    @Test
    public void shouldPropagateSdkException() {
        final StringPageRetriever pageRetriever = new StringPageRetriever(3, 5, 3);
        //
        final PagedAsyncStream<String> collection = new PagedAsyncStream<>(pageId -> {
            final CFBackedPageAsyncStream backingStream = new CFBackedPageAsyncStream<>(pageRetriever,
                continuationToken -> continuationToken != null,
                pageId,
                logger);
            return backingStream;
        }, this.logger);

        CountDownLatch latch = new CountDownLatch(1);
        Throwable [] error = new Throwable[1];
        collection.byPage().forEach(new AsyncStreamHandler<PagedResponse<String>>() {
            @Override
            public void onNext(PagedResponse<String> response) {
            }

            @Override
            public void onError(Throwable throwable) {
                error[0] = throwable;
                latch.countDown();
            }

            @Override
            public void onComplete() {
                latch.countDown();
            }
        });

        awaitOnLatch(latch, "shouldPropagateSdkException");

        Assertions.assertNotNull(error[0]);
        Assertions.assertNotNull(error[0].getCause());
        Assertions.assertTrue(error[0].getCause() instanceof UncheckedIOException);
        Assertions.assertEquals(4, pageRetriever.getCallCount());
    }

    private static void awaitOnLatch(CountDownLatch latch, String method) {
        try {
            latch.await(30, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            assertFalse(true, method + " didn't produce any result.");
        }
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
