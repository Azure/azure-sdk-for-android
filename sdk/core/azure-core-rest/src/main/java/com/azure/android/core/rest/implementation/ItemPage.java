// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.rest.implementation;

import com.azure.android.core.rest.Page;
import com.azure.core.serde.SerdePropertyAlias;

import java.util.Collections;
import java.util.List;

/**
 * Base class that is able to deserialize a Page JSON response. The JSON formats that it understands are:
 * {
 *      "nextLink": "",
 *      "value": [{ serialized(T) }, ... ]
 * }
 * or
 * {
 *      "nextPageLink": "",
 *      "items": [{ serialized(T) }, ... ]
 * }
 * or any other cases where the property names of that type are swapped
 * @param <T> The type of the object stored within the {@link ItemPage} instance
 */
public class ItemPage<T> implements Page<T> {
    @SerdePropertyAlias({"items", "value"})
    private List<T> items;

    @SerdePropertyAlias({"nextLink", "nextPageLink"})
    private String continuationToken;

    @Override
    public List<T> getElements() {
        return Collections.unmodifiableList(items);
    }

    @Override
    public String getContinuationToken() {
        return continuationToken;
    }
}
