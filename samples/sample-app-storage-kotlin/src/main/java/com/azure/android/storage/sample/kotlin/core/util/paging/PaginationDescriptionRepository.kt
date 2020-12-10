package com.azure.android.storage.sample.kotlin.core.util.paging

import com.azure.android.core.credential.TokenRequestObservable

/**
 * A repository that provide [PaginationDescription].
 *
 * @param T the type of items in the page
 * @param I the input type
 */
interface PaginationDescriptionRepository<T, I> {
    /**
     * Creates [PaginationDescription] for a paginated API.
     *
     * @param parameter the parameters
     * @return the pagination description
     */
    operator fun get(parameter: I): PaginationDescription<T>

    /**
     * @return the token request observable that UI can Observe for access-token
     * requests from the repository.
     */
    val tokenRequestObservable: TokenRequestObservable
}

