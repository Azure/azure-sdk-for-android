// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.rest.util.paging;

import com.azure.android.core.logging.ClientLogger;
import com.azure.android.core.util.BiConsumer;
import com.azure.android.core.util.Function;
import com.azure.android.core.util.AsyncStream;
import com.azure.android.core.util.AsyncStreamHandler;
import com.azure.android.core.util.CancellationToken;
import com.azure.android.core.util.paging.PagedAsyncStreamCore;

import java.util.List;

/**
 * PagedAsyncStream provides the ability to asynchronously enumerate paginated REST responses of type
 * {@link PagedResponse} and individual elements in such pages. When processing the response by page
 * each response will contain the elements in the page as well as the REST response details such as
 * status code and headers.
 *
 * @param <T> The type of element in the page.
 */
public final class PagedAsyncStream<T> implements PagedAsyncStreamCore<String, T, PagedResponse<T>> {
    private final Function<String, AsyncStream<PagedResponse<T>>> streamRetriever;
    private final ClientLogger logger;

    /**
     * Creates an instance of {@link PagedAsyncStream}. The constructor takes a {@code streamRetriever}.
     * The {@code pageRetriever} returns {@code AsyncStream} of pages when invoked with the id of the page
     * to retrieve.
     *
     * @param streamRetriever The function to retrieve source AsyncStream to back this stream.
     * @param logger The logger to log.
     */
    public PagedAsyncStream(Function<String, AsyncStream<PagedResponse<T>>> streamRetriever,
                            ClientLogger logger) {
        this.streamRetriever = streamRetriever;
        this.logger = logger;
    }

    /**
     * Enumerate the {@link PagedAsyncStream} by signaling each page element
     * across all pages to the {@code handler.onNext}.
     *
     * All the elements will be enumerated as long as there is no cancellation requested
     * and there is no error while retrieving the page (e.g. auth error, network error).
     *
     * The {@code CancellationToken} returned can be used to cancel the enumeration
     *
     * @param handler The enumeration handler.
     * @return CancellationToken to signal the enumeration cancel.
     */
    @Override
    public CancellationToken forEach(AsyncStreamHandler<T> handler) {
        return new PageElementAsyncStream<>(this.byPage()).forEach(handler);
    }

    /**
     * {@inheritdoc}
     */
    @Override
    public AsyncStream<PagedResponse<T>> byPage() {
        return this.streamRetriever.call(null);
    }

    /**
     * {@inheritdoc}
     */
    @Override
    public AsyncStream<PagedResponse<T>> byPage(String startPageId) {
        return this.streamRetriever.call(startPageId);
    }

    /**
     * {@inheritdoc}
     */
    @Override
    public AsyncStream<T> from(String startPageId) {
        return new PageElementAsyncStream<>(this.byPage(startPageId));
    }

    /**
     * Retrieve a page with given id {@code pageId}. A {@code null} value for {@code pageId} indicate the initial page.
     *
     * @param pageId The id of the page to retrieve.
     * @param pageHandler The handler to signal the retrieved page or any error during the page retrieval.
     */
    public void getPage(String pageId, BiConsumer<PagedResponse<T>, Throwable> pageHandler) {
        //
        // "getPage()" by default is built on "forEach". In almost all REST services
        // each iteration of "forEach" result in one network call, hence this default
        // impl of breaking the iteration after receiving first element is good enough.
        //
        // For any service which involves pre-fetching multiple pages(like EH), the SDK is expected
        // to directly implement 'com.azure.android.core.util.paging.PagedAsyncStreamCore' contract
        // and SDK may decide to have or opt-out (for perf reasons) the getPage().
        //
        this.byPage(pageId).forEach(new AsyncStreamHandler<PagedResponse<T>>() {
            private CancellationToken cancellationToken;
            private volatile boolean gotPage;

            @Override
            public void onInit(CancellationToken cancellationToken) {
                this.cancellationToken = cancellationToken;
            }

            @Override
            public void onNext(PagedResponse<T> page) {
                this.gotPage = true;
                if (this.cancellationToken != null) {
                    // "break" the async-loop once first page is received.
                    this.cancellationToken.cancel();
                }
                pageHandler.accept(page, null);
            }

            @Override
            public void onError(Throwable throwable) {
                if (!this.gotPage) {
                    // Propagate any Exception other than the CancelledException
                    // from self-cancellation (in onNext).
                    logger.logThrowableAsError(throwable);
                    pageHandler.accept(null, throwable);
                }
            }
        });
    }

    // AsyncStream that produce items by flattening elements in pages in a source AsyncStream.
    //
    private static final class PageElementAsyncStream<T>
        implements AsyncStream<T> {
        private final AsyncStream<PagedResponse<T>> pageSourceStream;

        PageElementAsyncStream(AsyncStream<PagedResponse<T>> pageSourceStream) {
            this.pageSourceStream = pageSourceStream;
        }

        @Override
        public CancellationToken forEach(AsyncStreamHandler<T> handler) {
            return this.pageSourceStream.forEach(new AsyncStreamHandler<PagedResponse<T>>() {
                private CancellationToken token;

                @Override
                public void onInit(CancellationToken cancellationToken) {
                    this.token = cancellationToken;
                    handler.onInit(cancellationToken);
                }

                @Override
                public void onNext(PagedResponse<T> response) {
                    final List<T> items = response.getElements();
                    if (items != null) {
                        for (T item : items) {
                            if (token != null && token.isCancellationRequested()) {
                                return;
                            }
                            handler.onNext(item);
                        }
                    }
                }

                @Override
                public void onError(Throwable throwable) {
                    handler.onError(throwable);
                }

                @Override
                public void onComplete() {
                    handler.onComplete();
                }
            });
        }
    }
}
