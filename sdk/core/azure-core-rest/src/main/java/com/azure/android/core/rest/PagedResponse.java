// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.android.core.rest;

/**
 * Response of a REST API that returns page.
 *
 * @see com.azure.android.core.rest.ContinuablePagedResponse
 *
 * @param <T> The type of items in the page.
 */
public interface PagedResponse<T> extends ContinuablePagedResponse<String, T> {
}
