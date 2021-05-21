// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.rest.util.paging;

import com.azure.android.core.logging.ClientLogger;
import com.azure.android.core.util.Function;
import com.azure.android.core.util.Predicate;
import com.azure.android.core.util.AsyncStream;
import com.azure.android.core.util.AsyncStreamHandler;
import com.azure.android.core.util.CancellationToken;

import java.util.concurrent.CancellationException;

import java9.util.concurrent.CompletableFuture;
import java9.util.concurrent.CompletionException;

final class CFBackedPageAsyncStream<T>
    implements AsyncStream<PagedResponse<T>> {
    private final Function<String, CompletableFuture<PagedResponse<T>>> pageRetriever;
    private final Predicate<String> continuationPredicate;
    private final String startPageId;
    private final ClientLogger logger;

    CFBackedPageAsyncStream(Function<String, CompletableFuture<PagedResponse<T>>> pageRetriever,
                            Predicate<String> continuationPredicate,
                            String startPageId,
                            ClientLogger logger) {
        this.pageRetriever = pageRetriever;
        this.continuationPredicate = continuationPredicate;
        this.startPageId = startPageId;
        this.logger = logger;
    }

    @Override
    public CancellationToken forEach(AsyncStreamHandler<PagedResponse<T>> handler) {
        final CancellationToken token = new CancellationToken();
        handler.onInit(token);
        if (token.isCancellationRequested()) {
            handler.onError(new CancellationException());
            return token;
        }

        final CompletableFuture<Void> completableFuture = this.enumeratePages(startPageId, token, handler);
        token.registerOnCancel(() -> {
            completableFuture.cancel(true);
        });
        completableFuture.whenCompleteAsync((ignored, throwable) -> {
            if (throwable != null) {
                if (throwable instanceof CompletionException && throwable.getCause() != null) {
                    // unwrap CF's CompletionException.
                    handler.onError(throwable.getCause());
                } else {
                    handler.onError(throwable);
                }
            } else {
                handler.onComplete();
            }
        });
        return token;
    }

    private CompletableFuture<Void> enumeratePages(String pageId,
                                                   CancellationToken token,
                                                   AsyncStreamHandler<PagedResponse<T>> handler) {
        return this.pageRetriever.call(pageId).handleAsync((pagedResponse, throwable) -> {
            if (throwable != null) {
                if (throwable instanceof RuntimeException) {
                    // avoid double-wrapping for already unchecked exception
                    throw logger.logExceptionAsError((RuntimeException) throwable);
                } else {
                    // wrap checked exception in a unchecked runtime exception
                    throw logger.logExceptionAsError(new RuntimeException(throwable));
                }
            }

            handler.onNext(pagedResponse);

            final String nextPageId = pagedResponse.getContinuationToken();
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
