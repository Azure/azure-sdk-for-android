// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.util.paging;

import java.util.List;

/**
 * Represents a page returned, this page may contain a reference to additional pages known as a continuation token.
 *
 * @param <C> Type of the continuation token.
 * @param <T> Type of the elements in the page.
 */
public interface ContinuablePage<C, T> {
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
