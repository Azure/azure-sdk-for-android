// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.rest;

import com.azure.android.core.logging.ClientLogger;
import com.azure.android.core.util.AsyncStream;

/**
 * PagedAsyncCollection provides the ability to operate on paginated REST responses of type {@link PagedResponse}
 * and individual elements in such pages. When processing the response by page each response will contain the
 * elements in the page as well as the REST response details such as status code and headers.
 *
 * @param <T> The type of element in the page.
 */
public final class PagedAsyncStream<T> extends ContinuablePagedAsyncStream<String, T, PagedResponse<T>> {
    /**
     * Creates an instance of {@link PagedAsyncStream}. The constructor takes a {@code streamRetriever}.
     * The {@code pageRetriever} returns {@code AsyncStream} of pages when invoked with the id of the page
     * to retrieve.
     *
     * @param streamRetriever The function to retrieve page stream backing this stream.
     * @param logger The logger to log.
     */
    public PagedAsyncStream(Function<String, AsyncStream<PagedResponse<T>>> streamRetriever,
                            ClientLogger logger) {
        super(streamRetriever, logger);
    }
}
