package com.azure.android.storage.sample.core.util.paging;

import com.azure.android.core.credential.TokenRequestObservable;

/**
 * A repository that provide {@link PaginationDescription}.
 *
 * @param <T> the type of items in the page
 * @param <I> the input type
 */
public interface PaginationDescriptionRepository<T, I> {
    /**
     * Creates {@link PaginationDescription} for a paginated API.
     *
     * @param parameter the parameters
     * @return the pagination description
     */
    PaginationDescription<T> get(I parameter);

    /**
     * @return the token request observable that UI can Observe for access-token
     * requests from the repository.
     */
    TokenRequestObservable getTokenRequestObservable();
}
