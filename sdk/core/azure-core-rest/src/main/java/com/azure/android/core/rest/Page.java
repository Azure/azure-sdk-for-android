// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.rest;

import java.util.List;

/**
 * Represents a paginated REST response from the service.
 *
 * @param <C> The type of the continuation token.
 * @param <T> Type of items in the page response.
 */
public interface Page<C, T> {
    /**
     * Gets an {@link List} of elements in the page.
     *
     * @return A {@link List} containing the elements in the page.
     */
    List<T> getElements();

    /**
     * Gets the reference to the next page.
     *
     * @return The next page reference or {@code null} if there isn't a next page.
     */
    C getContinuationToken();
}
