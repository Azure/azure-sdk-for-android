// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.rest.cfextensions;

import com.azure.android.core.logging.ClientLogger;
import com.azure.android.core.rest.ContinuablePagedResponse;

import java.util.List;

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
     * Enumerate the {@link ContinuablePagedAsyncCollection} by applying the given function for individual elements
     * in all pages.
     *
     * All the elements will be enumerated as long as the function returns true and there is
     * no error while retrieving the page (e.g. auth error, network error).
     *
     * If the function returns false or throws an error then no more elements will be enumerated.
     *
     * The {@code CompletableFuture} returned will be completed successfully if the enumeration
     * completes normally (collection exhausted or function returns false).
     *
     * The {@code CompletableFuture} returned will be completed with error if there is an error
     * during the enumeration (element retrieval error or error thrown by function).
     *
     * @param onNext The function to be applied for each page element.
     * @return CompletableFuture to signal the enumeration completion.
     */
    public CompletableFuture<Void> forEach(Function<T, Boolean> onNext) {
        return this.forEach(null, onNext);
    }

    /**
     * Enumerate the {@link ContinuablePagedAsyncCollection} by applying the given function for each page element,
     * starting from the elements in the page with the given id {@code startPageId}.
     *
     * All the elements will be enumerated as long as the function returns true and there is
     * no error while retrieving the page (e.g. auth error, network error).
     *
     * If the function returns false or throws an error then no more elements will be enumerated.
     *
     * The {@code CompletableFuture} returned will be completed successfully if the enumeration
     * completes normally (collection exhausted or function returns false).
     *
     * The {@code CompletableFuture} returned will be completed with error if there is an error
     * during the enumeration (element retrieval error or error thrown by function).
     *
     * @param startPageId The id of the page to start the enumeration from.
     * @param onNext The function to be applied for each element.
     * @return CompletableFuture to signal the enumeration completion.
     */
    public CompletableFuture<Void> forEach(C startPageId, Function<T, Boolean> onNext) {
        return this.forEachPage(startPageId, response -> {
            final List<T> items = response.getElements();
            if (items != null) {
                for (T item : items) {
                    try {
                        final boolean shouldContinue = onNext.apply(item);
                        if (!shouldContinue) {
                            return false;
                        }
                    } catch (Exception ex) {
                        if (ex instanceof RuntimeException) {
                            // avoid double-wrapping for already unchecked exception
                            throw logger.logExceptionAsError((RuntimeException) ex);
                        } else {
                            // wrap checked exception in a unchecked runtime exception
                            throw logger.logExceptionAsError(new RuntimeException(ex));
                        }
                    }
                }
            }
            return true;
        });
    }

    /**
     * Enumerate the {@link ContinuablePagedAsyncCollection} by applying the given function for each page.
     *
     * All the pages will be enumerated as long as the function returns true and there is
     * no error while retrieving the page (e.g. auth error, network error).
     *
     * If the function returns false or throws an error then no more pages will be enumerated.
     *
     * The {@code CompletableFuture} returned will be completed successfully if the enumeration
     * completes normally (collection exhausted or function returns false).
     *
     * The {@code CompletableFuture} returned will be completed with error if there is an error
     * during the enumeration (page retrieval error or error thrown by function).
     *
     * @param onNext The function to be applied for each page.
     * @return CompletableFuture to signal the enumeration completion.
     */
    public CompletableFuture<Void> forEachPage(Function<P, Boolean> onNext) {
        return enumeratePages(null, onNext);
    }

    /**
     * Enumerate the {@link ContinuablePagedAsyncCollection} by applying the given function for each page,
     * starting from the page with the given id {@code startPageId}.
     *
     * All the pages will be enumerated as long as the function returns true and there is
     * no error while retrieving the page (e.g. auth error, network error).
     *
     * If the function returns false or throws an error then no more pages will be enumerated.
     *
     * The {@code CompletableFuture} returned will be completed successfully if the enumeration
     * completes normally (collection exhausted or function returns false).
     *
     * The {@code CompletableFuture} returned will be completed with error if there is an error
     * during the enumeration (page retrieval error or error thrown by function).
     *
     * @param startPageId The id of the page to start the enumeration from.
     * @param onNext The function to be applied for each page.
     * @return CompletableFuture to signal the enumeration completion.
     */
    public CompletableFuture<Void> forEachPage(C startPageId,
                                               Function<P, Boolean> onNext) {
        return enumeratePages(startPageId, onNext);
    }

    private CompletableFuture<Void> enumeratePages(C pageId,
                                                   Function<P, Boolean> onNext) {
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
                final boolean shouldContinue = onNext.apply(pagedResponse);
                if (!shouldContinue) {
                    return null;
                }
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
            return nextPageId != null
                ? this.enumeratePages(nextPageId, onNext) : CompletableFuture.completedFuture(null);
        });
    }
}
