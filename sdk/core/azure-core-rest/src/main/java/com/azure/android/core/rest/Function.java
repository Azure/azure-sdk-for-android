// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.rest;

/**
 * The type handles retrieving pages.
 *
 * @param <C> Type of the continuation token.
 * @param <P> the page type
 */
@FunctionalInterface
public interface Function<C, P> {
    /**
     * Retrieves one or more pages starting from the page identified by the given continuation token.
     *
     * @param pageId Token identifying which page to retrieve, passing {@code null} indicates to retrieve
     * the first page.
     * @return page identified by the given id.
     */
    P get(C pageId);
}
