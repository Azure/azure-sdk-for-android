// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.rest.cfextensions;

import com.azure.android.core.logging.ClientLogger;
import com.azure.android.core.rest.ContinuablePagedResponse;
import com.azure.android.core.util.CancellationToken;

import java.util.List;
import java.util.concurrent.CancellationException;

import java9.util.concurrent.CompletableFuture;
import java9.util.function.Function;
import java9.util.function.Predicate;

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
public class ContinuablePagedAsyncCollection<C, T, P extends ContinuablePagedResponse<C, T>> {
    private final Function<C, CompletableFuture<P>> pageRetriever;
    private final Predicate<C> continuationPredicate;
    private final ClientLogger logger;

    /**
     * Creates an instance of {@link ContinuablePagedAsyncCollection}. The constructor takes a {@code pageRetriever}.
     * The {@code pageRetriever} returns a page of {@code T} when invoked with the id of the page to retrieve.
     *
     * @param pageRetriever The function to retrieve pages.
     * @param continuationPredicate The predicate to check whether to continue the paging.
     * @param logger The logger to log.
     */
    public ContinuablePagedAsyncCollection(Function<C, CompletableFuture<P>> pageRetriever,
                                           Predicate<C> continuationPredicate,
                                           ClientLogger logger) {
        this.pageRetriever = pageRetriever;
        this.continuationPredicate = continuationPredicate;
        this.logger = logger;
    }

    /**
     * Retrieve a page with given id {@code pageId}. A {@code null} value for {@code pageId} indicate the initial page.
     *
     * @param pageId The id of the page.
     * @return A CompletableFuture with the page retrieved or any error during the page retrieval.
     */
    public CompletableFuture<P> getPage(C pageId) {
        try {
            return this.pageRetriever.apply(pageId);
        } catch (Exception ex) {
            return CompletableFuture.failedFuture(ex);
        }
    }

    /**
     * Enumerate the {@link ContinuablePagedAsyncCollection} by signaling each page element
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
    public CancellationToken forEach(AsyncStreamHandler<T> handler) {
        return this.forEach(null, handler);
    }

    /**
     * Enumerate the {@link ContinuablePagedAsyncCollection} by signaling each page element to
     * the {@code handler.onNext}, starting from the elements in the page with the given
     * id {@code startPageId}.
     *
     * All the elements will be enumerated as long as there is no cancellation requested and
     * there is no error while retrieving the page (e.g. auth error, network error).
     *
     * The {@code CancellationToken} returned can be used to cancel the enumeration
     *
     * @param startPageId The id of the page to start the enumeration from.
     * @param handler The enumeration handler.
     * @return CancellationToken to signal the enumeration cancel.
     */
    public CancellationToken forEach(C startPageId, AsyncStreamHandler<T> handler) {
        return this.forEachPage(startPageId, new AsyncStreamHandler<P>() {
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

    /**
     * Enumerate the {@link ContinuablePagedAsyncCollection} by signaling each page to
     * the {@code handler.onNext}.
     *
     * All the pages will be enumerated as long as there is no cancellation requested and
     * there is no error while retrieving the page (e.g. auth error, network error).
     *
     * The {@code CancellationToken} returned can be used to cancel the enumeration
     *
     * @param handler The enumeration handler.
     * @return CancellationToken to signal the enumeration cancel.
     */
    public CancellationToken forEachPage(AsyncStreamHandler<P> handler) {
        return this.forEachPage(null, handler);
    }

    /**
     * Enumerate the {@link ContinuablePagedAsyncCollection} by signaling each page to
     * the {@code handler.onNext}, starting from the page with the given id {@code startPageId}.
     *
     * All the pages will be enumerated as long as there is no cancellation requested and
     * there is no error while retrieving the page (e.g. auth error, network error).
     *
     * The {@code CancellationToken} returned can be used to cancel the enumeration
     *
     * @param startPageId The id of the page to start the enumeration from.
     * @param handler The enumeration handler.
     * @return CancellationToken to signal the enumeration cancel.
     */
    public CancellationToken forEachPage(C startPageId,
                                         AsyncStreamHandler<P> handler) {
        final CancellationToken token = new CancellationToken();
        handler.onInit(token);
        if (token.isCancellationRequested()) {
            handler.onError(new CancellationException());
            return token;
        }

        final CompletableFuture<Void> completableFuture = enumeratePages(startPageId, token, handler);
        token.registerOnCancel(() -> {
            completableFuture.cancel(true);
        });
        completableFuture.whenCompleteAsync((ignored, throwable) -> {
            if (throwable != null) {
                handler.onError(throwable);
            } else {
                handler.onComplete();
            }
        });
        return token;
    }

    private CompletableFuture<Void> enumeratePages(C pageId,
                                                   CancellationToken token,
                                                   AsyncStreamHandler<P> handler) {
        return this.getPage(pageId).handleAsync((pagedResponse, throwable) -> {
            if (throwable != null) {
                if (throwable instanceof RuntimeException) {
                    // avoid double-wrapping for already unchecked exception
                    throw logger.logExceptionAsError((RuntimeException) throwable);
                } else {
                    // wrap checked exception in a unchecked runtime exception
                    throw logger.logExceptionAsError(new RuntimeException(throwable));
                }
            }

            try {
                handler.onNext(pagedResponse);
            } catch (Exception ex) {
                if (ex instanceof RuntimeException) {
                    // avoid double-wrapping for already unchecked exception
                    throw logger.logExceptionAsError((RuntimeException) ex);
                } else {
                    // wrap checked exception in a unchecked runtime exception
                    throw logger.logExceptionAsError(new RuntimeException(ex));
                }
            }

            final C nextPageId = pagedResponse.getContinuationToken();
            return continuationPredicate.test(nextPageId) ? nextPageId : null;

        }).thenCompose(nextPageId -> {
            if (token.isCancellationRequested()) {
                return CompletableFuture.failedFuture(new CancellationException());
            } else {
                return nextPageId != null
                    ? this.enumeratePages(nextPageId, token, handler)
                    : CompletableFuture.completedFuture(null);
            }
        });
    }
}
