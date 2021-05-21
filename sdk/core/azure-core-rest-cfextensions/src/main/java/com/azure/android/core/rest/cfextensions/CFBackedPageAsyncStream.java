// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.rest.cfextensions;

import com.azure.android.core.logging.ClientLogger;
import com.azure.android.core.rest.ContinuablePagedResponse;
import com.azure.android.core.util.AsyncStream;
import com.azure.android.core.util.AsyncStreamHandler;
import com.azure.android.core.util.CancellationToken;

import java.util.concurrent.CancellationException;

import java9.util.concurrent.CompletableFuture;
import java9.util.function.Function;
import java9.util.function.Predicate;

final class CFBackedPageAsyncStream<C, T, P extends ContinuablePagedResponse<C, T>>
    implements AsyncStream<P> {
    private final Function<C, CompletableFuture<P>> pageRetriever;
    private final Predicate<C> continuationPredicate;
    private final C startPageId;
    private final ClientLogger logger;

    CFBackedPageAsyncStream(Function<C, CompletableFuture<P>> pageRetriever,
                            Predicate<C> continuationPredicate,
                            C startPageId,
                            ClientLogger logger) {
        this.pageRetriever = pageRetriever;
        this.continuationPredicate = continuationPredicate;
        this.startPageId = startPageId;
        this.logger = logger;
    }

    @Override
    public CancellationToken forEach(AsyncStreamHandler<P> handler) {
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
        return this.pageRetriever.apply(pageId).handleAsync((pagedResponse, throwable) -> {
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
