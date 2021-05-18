// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.rest;

/**
 * The type to check paging should continue using the given continuation token.
 *
 * @param <C> The continuation token type
 */
public interface PagingContinuationPredicate<C> {
    /**
     * Evaluates this predicate on the given continuation token.
     *
     * @param pageId The continuation token
     * @return true if the paging should continue, false otherwise.
     */
    boolean shouldContinue(C pageId);
}
