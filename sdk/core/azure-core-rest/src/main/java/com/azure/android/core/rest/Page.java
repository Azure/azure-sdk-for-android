// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.rest;

import com.azure.android.core.micro.util.paging.ContinuablePage;

/**
 * Represents a paginated REST response from the service.
 *
 * @param <T> Type of items in the page response.
 */
public interface Page<T> extends ContinuablePage<String, T> {
}
