// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.rest.util.paging;

import com.azure.android.core.logging.ClientLogger;
import com.azure.android.core.util.AsyncStreamHandler;
import com.azure.android.core.util.CancellationToken;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * Tests {@link PagedAsyncStream}.
 */
public class PagedAsyncStreamTests {
    private final ClientLogger logger = new ClientLogger(PagedAsyncStreamTests.class);

    @Test
    public void canEnumerateAllPages() {
        final CFStringPageRetriever pageRetriever = new CFStringPageRetriever(3, 5);
        //
        final PagedAsyncStream<String> asyncStream = new PagedAsyncStream<>(pageId -> {
            final CFBackedPageAsyncStream backingStream = new CFBackedPageAsyncStream<>(pageRetriever,
                continuationToken -> continuationToken != null,
                pageId,
                logger);
            return backingStream;
        }, this.logger);

        CountDownLatch latch = new CountDownLatch(1);
        asyncStream.byPage().forEach(new AsyncStreamHandler<PagedResponse<String>>() {
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
    public void canEnumerateAllElements() {
        final CFStringPageRetriever pageRetriever = new CFStringPageRetriever(3, 5);
        //
        final PagedAsyncStream<String> asyncStream = new PagedAsyncStream<>(pageId -> {
            final CFBackedPageAsyncStream backingStream = new CFBackedPageAsyncStream<>(pageRetriever,
                continuationToken -> continuationToken != null,
                pageId,
                logger);
            return backingStream;
        }, this.logger);

        CountDownLatch latch = new CountDownLatch(1);
        asyncStream.forEach(new AsyncStreamHandler<String>() {
            @Override
            public void onNext(String response) {
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

        awaitOnLatch(latch, "canEnumerateAllElements");
        Assertions.assertEquals(5, pageRetriever.getCallCount());
    }

    @Test
    public void canStopPageEnumeration() {
        final CFStringPageRetriever pageRetriever = new CFStringPageRetriever(3, 5);
        //
        final PagedAsyncStream<String> asyncStream = new PagedAsyncStream<>(pageId -> {
            final CFBackedPageAsyncStream backingStream = new CFBackedPageAsyncStream<>(pageRetriever,
                continuationToken -> continuationToken != null,
                pageId,
                logger);
            return backingStream;
        }, this.logger);

        CountDownLatch latch = new CountDownLatch(1);
        Throwable [] error = new Throwable[1];
        asyncStream.byPage().forEach(new AsyncStreamHandler<PagedResponse<String>>() {
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
        Assertions.assertTrue(error[0] instanceof RuntimeException);
        if (error[0].getCause() != null) {
            // To investigate: It seems when running test using "gradle from commandline"
            // getCause() returns null but not-null from an app or editor.
            Assertions.assertTrue(error[0].getCause() instanceof CancellationException);
        }
    }

    @Test
    public void canStopElementEnumeration() {
        final CFStringPageRetriever pageRetriever = new CFStringPageRetriever(3, 5);
        //
        final PagedAsyncStream<String> asyncStream = new PagedAsyncStream<>(pageId -> {
            final CFBackedPageAsyncStream backingStream = new CFBackedPageAsyncStream<>(pageRetriever,
                continuationToken -> continuationToken != null,
                pageId,
                logger);
            return backingStream;
        }, this.logger);

        CountDownLatch latch = new CountDownLatch(1);
        Throwable [] error = new Throwable[1];
        asyncStream.forEach(new AsyncStreamHandler<String>() {
            private CancellationToken token;
            @Override
            public void onInit(CancellationToken cancellationToken) {
                this.token = cancellationToken;
            }

            @Override
            public void onNext(String element) {
                Assertions.assertNotNull(element);
                if (element.equalsIgnoreCase("8")) { // "8" is an element in page 3.
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

        awaitOnLatch(latch, "canStopElementEnumeration");
        Assertions.assertEquals(3, pageRetriever.getCallCount());
        Assertions.assertNotNull(error[0]);
        Assertions.assertTrue(error[0] instanceof RuntimeException);
        if (error[0].getCause() != null) {
            // To investigate: It seems when running test using "gradle from commandline"
            // getCause() returns null but not-null from an app or editor.
            Assertions.assertTrue(error[0].getCause() instanceof CancellationException);
        }
    }

    @Test
    public void shouldPropagateUserExceptionWhenIteratePages() {
        final CFStringPageRetriever pageRetriever = new CFStringPageRetriever(3, 5);
        //
        final PagedAsyncStream<String> asyncStream = new PagedAsyncStream<>(pageId -> {
            final CFBackedPageAsyncStream backingStream = new CFBackedPageAsyncStream<>(pageRetriever,
                continuationToken -> continuationToken != null,
                pageId,
                logger);
            return backingStream;
        }, this.logger);

        CountDownLatch latch = new CountDownLatch(1);
        Throwable [] error = new Throwable[1];
        asyncStream.byPage().forEach(new AsyncStreamHandler<PagedResponse<String>>() {
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

        awaitOnLatch(latch, "shouldPropagateUserExceptionWhenIteratePages");

        Assertions.assertNotNull(error[0]);
        Assertions.assertTrue(error[0] instanceof RuntimeException);
        Assertions.assertNotNull(error[0].getMessage());
        Assertions.assertTrue(error[0].getMessage().equalsIgnoreCase("user-error"));
        Assertions.assertEquals(3, pageRetriever.getCallCount());
    }

    @Test
    public void shouldPropagateUserExceptionWhenIterateElements() {
        final CFStringPageRetriever pageRetriever = new CFStringPageRetriever(3, 5);
        //
        final PagedAsyncStream<String> asyncStream = new PagedAsyncStream<>(pageId -> {
            final CFBackedPageAsyncStream backingStream = new CFBackedPageAsyncStream<>(pageRetriever,
                continuationToken -> continuationToken != null,
                pageId,
                logger);
            return backingStream;
        }, this.logger);

        CountDownLatch latch = new CountDownLatch(1);
        Throwable [] error = new Throwable[1];
        asyncStream.forEach(new AsyncStreamHandler<String>() {
            @Override
            public void onNext(String element) {
                Assertions.assertNotNull(element);
                if (element.equalsIgnoreCase("8")) { // "8" is an element in page 3
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

        awaitOnLatch(latch, "shouldPropagateUserExceptionWhenIterateElements");

        Assertions.assertNotNull(error[0]);
        Assertions.assertTrue(error[0] instanceof RuntimeException);
        Assertions.assertNotNull(error[0].getMessage());
        Assertions.assertTrue(error[0].getMessage().equalsIgnoreCase("user-error"));
        Assertions.assertEquals(3, pageRetriever.getCallCount());
    }

    @Test
    public void shouldPropagateSdkExceptionWhenIteratePages() {
        final CFStringPageRetriever pageRetriever = new CFStringPageRetriever(3, 5, 3);
        //
        final PagedAsyncStream<String> asyncStream = new PagedAsyncStream<>(pageId -> {
            final CFBackedPageAsyncStream backingStream = new CFBackedPageAsyncStream<>(pageRetriever,
                continuationToken -> continuationToken != null,
                pageId,
                logger);
            return backingStream;
        }, this.logger);

        CountDownLatch latch = new CountDownLatch(1);
        Throwable [] error = new Throwable[1];
        asyncStream.byPage().forEach(new AsyncStreamHandler<PagedResponse<String>>() {
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

        awaitOnLatch(latch, "shouldPropagateSdkExceptionWhenIteratePages");

        Assertions.assertNotNull(error[0]);
        Assertions.assertTrue(error[0] instanceof UncheckedIOException);
        Assertions.assertNotNull(error[0].getCause());
        Assertions.assertTrue(error[0].getCause() instanceof IOException);
        Assertions.assertEquals(4, pageRetriever.getCallCount());
    }

    @Test
    public void shouldPropagateSdkExceptionWhenIterateElements() {
        final CFStringPageRetriever pageRetriever = new CFStringPageRetriever(3, 5, 3);
        //
        final PagedAsyncStream<String> asyncStream = new PagedAsyncStream<>(pageId -> {
            final CFBackedPageAsyncStream backingStream = new CFBackedPageAsyncStream<>(pageRetriever,
                continuationToken -> continuationToken != null,
                pageId,
                logger);
            return backingStream;
        }, this.logger);

        CountDownLatch latch = new CountDownLatch(1);
        Throwable [] error = new Throwable[1];
        asyncStream.forEach(new AsyncStreamHandler<String>() {
            @Override
            public void onNext(String element) {
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

        awaitOnLatch(latch, "shouldPropagateSdkExceptionWhenIterateElements");

        Assertions.assertNotNull(error[0]);
        Assertions.assertTrue(error[0] instanceof UncheckedIOException);
        Assertions.assertNotNull(error[0].getCause());
        Assertions.assertTrue(error[0].getCause() instanceof IOException);
        Assertions.assertEquals(4, pageRetriever.getCallCount());
    }

    @Test
    public void shouldFetchSinglePage() {
        final CFStringPageRetriever pageRetriever = new CFStringPageRetriever(3, 5);
        //
        final PagedAsyncStream<String> asyncStream = new PagedAsyncStream<>(pageId -> {
            final CFBackedPageAsyncStream backingStream = new CFBackedPageAsyncStream<>(pageRetriever,
                continuationToken -> continuationToken != null,
                pageId,
                logger);
            return backingStream;
        }, this.logger);

        CountDownLatch latch = new CountDownLatch(1);
        //
        // when requesting a random page with id, there should be only one call to retriever.
        asyncStream.getPage("3", (response, throwable) -> {
            try {
                Assertions.assertNotNull(response);
            } finally {
                latch.countDown();
            }
        });

        awaitOnLatch(latch, "shouldFetchSinglePage");
        Assertions.assertEquals(1, pageRetriever.getCallCount());
    }

    private static void awaitOnLatch(CountDownLatch latch, String method) {
        try {
            latch.await(30, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            assertFalse(true, method + " didn't produce any result.");
        }
    }
}