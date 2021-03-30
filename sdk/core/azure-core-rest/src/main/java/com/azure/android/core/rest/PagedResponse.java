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
 * @see com.azure.android.core.rest.Page
 * @see com.azure.android.core.rest.Response
 *
 * @param <T> The type of items in the page.
 */
public interface PagedResponse<T> extends Page<T>, Response<List<T>>, Closeable {
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
