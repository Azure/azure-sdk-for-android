// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.util.paging;

import com.azure.android.core.util.AsyncStream;
import com.azure.android.core.util.BiConsumer;

/**
 * A sequence of elements in pages that can be enumerated asynchronously.
 *
 * @param <C> The continuation token type.
 * @param <T> The type of element in the page.
 * @param <P> The type of the page.
 */
public interface PagedAsyncStreamCore<C, T, P extends Page<C, T>> extends AsyncStream<T> {
    /**
     * Gets {@link AsyncStream} that enables enumerating the pages asynchronously.
     *
     * @return The {@link AsyncStream} of pages.
     */
    AsyncStream<P> byPage();
    /**
     * Gets {@link AsyncStream} that enables enumerating the pages asynchronously,
     * starting from the page with the given id {@code startPageId}.
     *
     * @param startPageId The id of the page to start the enumeration from.
     * @return The {@link AsyncStream} of pages.
     */
    AsyncStream<P> byPage(C startPageId);
    /**
     * Gets {@link AsyncStream} that enables enumerating the elements of pages asynchronously,
     * starting from the page with the given id {@code startPageId}.
     *
     * @param startPageId The id of the page to start the enumeration from.
     * @return The {@link AsyncStream} of page elements.
     */
    AsyncStream<T> from(C startPageId);
    /**
     * Retrieve a page with given id {@code pageId}. A {@code null} value for {@code pageId} indicate the initial page.
     *
     * @param pageId The id of the page to retrieve.
     * @param resultHandler The handler to signal the retrieved page or any error during the page retrieval.
     */
    void getPage(String pageId, BiConsumer<P, Throwable> resultHandler);
}