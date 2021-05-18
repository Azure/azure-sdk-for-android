// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.rest;

/**
 * This class provides utility to iterate over {@link PagedResponse} and elements in {@link PagedResponse}
 * using {@link Iterable} interfaces.
 *
 * @param <T> The type of page elements contained in this {@link PagedIterable}.
 */
public class PagedIterable<T> extends ContinuablePagedIterable<String, T, PagedResponse<T>> {
    /**
     * Creates an instance of {@link PagedIterable}.
     *
     * @param pageRetriever The page retriever.
     */
    public PagedIterable(PageRetriever<String, PagedResponse<T>> pageRetriever) {
        super(pageRetriever, pageId -> pageId != null);
    }
}
