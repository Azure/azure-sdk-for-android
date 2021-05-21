// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.rest.cfextensions;

import com.azure.android.core.logging.ClientLogger;
import com.azure.android.core.rest.ContinuablePagedResponse;
import com.azure.android.core.util.AsyncStream;
import com.azure.android.core.util.AsyncStreamHandler;
import com.azure.android.core.util.CancellationToken;
import com.azure.android.core.util.paging.PagedAsyncStreamCore;

import java.util.List;

import java9.util.function.Function;

/**
 * ContinuablePagedAsyncCollection provides the ability to operate on paginated REST responses of type
 * {@link ContinuablePagedResponse}
 * and individual elements in such pages. When processing the response by page each response will contain the
 * elements in the page as well as the REST response details such as status code and headers.
 *
 * @param <C> The continuation token type.
 * @param <T> The type of element in the page.
 * @param <P> The type of the page.
 */
public class ContinuablePagedAsyncStream<C, T, P extends ContinuablePagedResponse<C, T>>
    implements PagedAsyncStreamCore<C, T, P> {
    private final Function<C, AsyncStream<P>> streamRetriever;
    private final ClientLogger logger;

    /**
     * Creates an instance of {@link ContinuablePagedAsyncStream}.
     *
     * @param streamRetriever The function to retrieve page stream backing this stream.
     * @param logger The logger to log.
     */
    public ContinuablePagedAsyncStream(Function<C, AsyncStream<P>> streamRetriever,
                                       ClientLogger logger) {
        this.streamRetriever = streamRetriever;
        this.logger = logger;
    }

    /**
     * Enumerate the {@link ContinuablePagedAsyncStream} by signaling each page element
     * from all pages to the {@code handler.onNext}.
     *
     * All the elements will be enumerated as long as there is no cancellation requested and
     * there is no error while retrieving the page (e.g. auth error, network error).
     *
     * The {@code CancellationToken} returned can be used to cancel the enumeration
     *
     * @param handler The enumeration handler.
     * @return CancellationToken to signal the enumeration cancel.
     */
    @Override
    public CancellationToken forEach(AsyncStreamHandler<T> handler) {
        return new PageElementAsyncStream<>(this.byPage(), this.logger).forEach(handler);
    }

    @Override
    public AsyncStream<P> byPage() {
        return this.streamRetriever.apply(null);
    }

    @Override
    public AsyncStream<P> byPage(C startPageId) {
        return this.streamRetriever.apply(startPageId);
    }

    @Override
    public AsyncStream<T> from(C startPageId) {
        return new PageElementAsyncStream<>(this.byPage(startPageId), this.logger);
    }

    // AsyncStream that produce elements by flattening pages in another AsyncStream.

    private static final class PageElementAsyncStream<C, T, P extends ContinuablePagedResponse<C, T>>
        implements AsyncStream<T> {
        private final AsyncStream<P> pageStream;
        private final ClientLogger logger;

        PageElementAsyncStream(AsyncStream<P> pageStream, ClientLogger logger) {
            this.pageStream = pageStream;
            this.logger = logger;
        }

        @Override
        public CancellationToken forEach(AsyncStreamHandler<T> handler) {
            return this.pageStream.forEach(new AsyncStreamHandler<P>() {
                private CancellationToken token;

                @Override
                public void onInit(CancellationToken cancellationToken) {
                    this.token = cancellationToken;
                    handler.onInit(cancellationToken);
                }

                @Override
                public void onNext(P response) {
                    final List<T> items = response.getElements();
                    if (items != null) {
                        for (T item : items) {
                            if (token != null && token.isCancellationRequested()) {
                                return;
                            }
                            try {
                                handler.onNext(item);
                            } catch (Exception ex) {
                                throw logger.logExceptionAsError(new RuntimeException(ex));
                            }
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
