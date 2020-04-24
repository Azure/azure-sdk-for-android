package com.azure.android.storage.sample.core.util.paging;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.paging.PagedList;

import com.azure.android.storage.sample.core.util.tokenrequest.TokenRequestObservable;

import static androidx.lifecycle.Transformations.map;
import static androidx.lifecycle.Transformations.switchMap;

/**
 * The default implementation of {@link ViewModel} that provide data for a View UI that render
 * pageble result.
 *
 * @param <R> the type of the repository that provide {@link PaginationDescription}
 * @param <T> the type of items in the page
 * @param <I> the input describing common parameters for page fetching
 */
public abstract class PaginationViewModel<R extends PaginationDescriptionRepository<T, I>, T, I>
        extends ViewModel {
    private final R repository;
    private final MutableLiveData<I> inputObservable = new MutableLiveData<>();
    private final LiveData<PaginationDescription<T>> paginationDescriptionObservable;
    private final LiveData<PagedList<T>> pagedListObservable;
    private final LiveData<PageLoadState> loadStateObservable;
    private final LiveData<PageLoadState> refreshStateObservable;


    /**
     * Creates PaginationViewModel.
     *
     * @param repository the repository that provides {@link PaginationDescription} for the view model.
     */
    public PaginationViewModel(R repository) {
        this.repository = repository;
        this.paginationDescriptionObservable = map(this.inputObservable, (input) -> this.repository.get(input));
        this.pagedListObservable = switchMap(paginationDescriptionObservable, d -> d.getPagedListObservable());
        this.loadStateObservable = switchMap(paginationDescriptionObservable, d -> d.getLoadStateObservable());
        this.refreshStateObservable = switchMap(paginationDescriptionObservable, d -> d.getRefreshStateObservable());
    }

    /**
     * Initiate retrieval of pages, once the PagedList based on the retrieved pages is initiated
     * it will be emitted by {@link PaginationViewModel#getPagedListObservable()}.
     *
     * @param input the data (e.g. REST call parameters) required to retrieve page
     * @return
     */
    public boolean list(I input) {
        if (this.inputObservable.getValue() == input) {
            return false;
        } else {
            this.inputObservable.setValue(input);
            return true;
        }
    }

    /**
     * @return the LiveData observable that emits {@link PagedList}
     */
    public LiveData<PagedList<T>> getPagedListObservable() {
        return this.pagedListObservable;
    }

    /**
     * @return the LiveData observable that emits page load status
     */
    public LiveData<PageLoadState> getLoadStateObservable() {
        return this.loadStateObservable;
    }

    /**
     * @return the LiveData observable that emits refresh state when {@link this#refresh()} is called
     */
    public LiveData<PageLoadState> getRefreshStateObservable() {
        return this.refreshStateObservable;
    }

    /**
     * @return the token request observable that UI can Observe for request for access-token
     * from the repository.
     */
    public TokenRequestObservable getTokenRequestObservable() {
        return this.repository.getTokenRequestObservable();
    }

    /**
     * Invalidate the last PagedList emitted by {@link PaginationViewModel#getPagedListObservable()}
     * and create a new PagedList by retrieving Pages from the beginning, the new PagedList
     * will be emitted by {@link this#getPagedListObservable()}.
     */
    public void refresh() {
        PaginationDescription<T> description = this.paginationDescriptionObservable.getValue();
        if (description != null) {
            description.refresh();
        }
    }

    /**
     * If the last page load was failed then retry it.
     */
    public void retry() {
        PaginationDescription<T> description = this.paginationDescriptionObservable.getValue();
        if (description != null) {
            description.retry();
        }
    }

    /**
     * @return get the repository backing the page data for this view
     */
    public R getRepository() {
        return this.repository;
    }
}
