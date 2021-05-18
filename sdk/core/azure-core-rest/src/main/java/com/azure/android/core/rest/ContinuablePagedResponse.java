// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.rest;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Response of a REST API that returns page.
 *
 * @see com.azure.android.core.rest.ContinuablePagedResponse
 *
 * @param <C> The type of the continuation token.
 * @param <T> The type of items in the page.
 */
public interface ContinuablePagedResponse<C, T> extends Page<C, T>, Response<List<T>>, Closeable {
    /**
     * Returns the items in the page.
     *
     * @return The items in the page.
     */
    default List<T> getValue() {
        List<T> elements = this.getElements();
        return elements == null ? new ArrayList<>() : Collections.unmodifiableList(elements);
    }
}
