package com.azure.android.storage.sample.kotlin.core.util.paging

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import androidx.paging.PagedList
import com.azure.android.core.credential.TokenRequestObservable

/**
 * The default implementation of [ViewModel] that provide data for a View UI that render
 * pageble result.
 *
 * @param R the type of the repository that provide [PaginationDescription]
 * @param T the type of items in the page
 * @param I the input describing common parameters for page fetching
 */
abstract class PaginationViewModel<R : PaginationDescriptionRepository<T, I>, T, I>(val repository: R) : ViewModel() {
    private val inputObservable: MutableLiveData<I> = MutableLiveData()

    private val paginationDescriptionObservable : LiveData<PaginationDescription<T>>
        = Transformations.map(inputObservable) { input: I -> repository.get(input) }

    private val pagedListObservable: LiveData<PagedList<T>>
        = Transformations.switchMap(paginationDescriptionObservable) { d: PaginationDescription<T> -> d.pagedListObservable }

    private val loadStateObservable : LiveData<PageLoadState>
        = Transformations.switchMap(paginationDescriptionObservable) { d: PaginationDescription<T> -> d.loadStateObservable }

    private val refreshStateObservable : LiveData<PageLoadState>
        = Transformations.switchMap(paginationDescriptionObservable) { d: PaginationDescription<T> -> d.refreshStateObservable }

    /**
     * Initiate retrieval of pages, once the PagedList based on the retrieved pages is initiated
     * it will be emitted by [PaginationViewModel.getPagedListObservable].
     *
     * @param input the data (e.g. REST call parameters) required to retrieve page
     * @return
     */
    fun list(input: I?): Boolean {
        return if (inputObservable.value === input) {
            false
        } else {
            inputObservable.value = input
            true
        }
    }

    /**
     * @return the LiveData observable that emits [PagedList]
     */
    fun getPagedListObservable(): LiveData<PagedList<T>> {
        return pagedListObservable
    }

    /**
     * @return the LiveData observable that emits page load status
     */
    fun getLoadStateObservable(): LiveData<PageLoadState> {
        return loadStateObservable
    }

    /**
     * @return the LiveData observable that emits refresh state when [refresh] is called
     */
    fun getRefreshStateObservable(): LiveData<PageLoadState> {
        return refreshStateObservable
    }

    /**
     * @return the token request observable that UI can Observe for request for access-token
     * from the repository.
     */
    fun getTokenRequestObservable(): TokenRequestObservable {
        return repository.tokenRequestObservable
    }

    /**
     * Invalidate the last PagedList emitted by [PaginationViewModel.getPagedListObservable]
     * and create a new PagedList by retrieving Pages from the beginning, the new PagedList
     * will be emitted by [getPagedListObservable].
     */
    fun refresh() {
        val description: PaginationDescription<T>? = paginationDescriptionObservable.value
        description?.refresh()
    }

    /**
     * If the last page load was failed then retry it.
     */
    fun retry() {
        val description: PaginationDescription<T>? = paginationDescriptionObservable.value
        description?.retry()
    }
}
