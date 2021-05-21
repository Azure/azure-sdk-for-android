// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.rest;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * This class provides utility to iterate over {@link ContinuablePagedResponse} and elements
 * in {@link ContinuablePagedResponse} using {@link Iterable} interfaces.
 *
 * @param <C> The type of the continuation token
 * @param <T> The type of page elements contained in this {@link ContinuablePagedIterable}.
 * @param <P> The type of pages contained in this {@link ContinuablePagedIterable}.
 */
public class ContinuablePagedIterable<C, T, P extends ContinuablePagedResponse<C, T>> implements Iterable<T> {
    private final Function<C, P> pageRetriever;
    private final PagingContinuationPredicate<C> continuationPredicate;

    /**
     * Creates an instance of {@link ContinuablePagedIterable}.
     *
     * @param pageRetriever The page retriever.
     * @param continuationPredicate A predicate which determines if paging should continue.
     */
    public ContinuablePagedIterable(Function<C, P> pageRetriever,
                                    PagingContinuationPredicate<C> continuationPredicate) {
        this.pageRetriever = pageRetriever;
        this.continuationPredicate = continuationPredicate;
    }

    @Override
    public Iterator<T> iterator() {
        return new PageItemIterator(byPage().iterator());
    }

    /**
     * Gets a {@link Iterable} of {@link ContinuablePagedResponse} starting at the first page.
     *
     * @return A {@link Iterable} of {@link ContinuablePagedResponse}.
     */
    public Iterable<P> byPage() {
        return new PagedResponseIterable(this.pageRetriever, null, this.continuationPredicate);
    }

    /**
     * Gets a {@link Iterable} of {@link ContinuablePagedResponse} beginning at the page identified by the given
     * token.
     *
     * @param startPageId A continuation token identifying the page to select.
     * @return A {@link Iterable} of {@link ContinuablePagedResponse}.
     */
    public Iterable<P> byPage(C startPageId) {
        return new PagedResponseIterable(this.pageRetriever, startPageId, this.continuationPredicate);
    }

    /**
     * Gets a {@link Iterable} of {@link T} beginning at the page identified by the given
     * token.
     *
     * @param startPageId A continuation token identifying the page to select.
     * @return A {@link Iterable} of {@link T}.
     */
    public Iterator<T> iterator(C startPageId) {
        return new PageItemIterator(byPage(startPageId).iterator());
    }

    private static final class PagedResponseIterable<C, T, P extends ContinuablePagedResponse<C, T>>
        implements Iterable<T> {

        private final Function<String, PagedResponse<T>> pageRetriever;
        private C nextPageId;
        private final PagingContinuationPredicate<C> continuationPredicate;

        PagedResponseIterable(Function<String, PagedResponse<T>> pageRetriever,
                              C startPageId,
                              PagingContinuationPredicate<C> continuationPredicate) {
            this.pageRetriever = pageRetriever;
            this.nextPageId = startPageId;
            this.continuationPredicate = continuationPredicate;
        }

        @Override
        public Iterator<T> iterator() {
            return new PagedResponseIterator(this.pageRetriever, this.nextPageId, this.continuationPredicate);
        }

        private static final class PagedResponseIterator<C, T, P extends ContinuablePagedResponse<C, T>>
            implements Iterator<P> {
            private final Function<C, P> pageRetriever;
            private C nextPageId;
            private final PagingContinuationPredicate<C> continuationPredicate;
            private boolean isExhausted;

            PagedResponseIterator(Function<C, P> pageRetriever,
                                  C startPageId,
                                  PagingContinuationPredicate<C> continuationPredicate) {
                this.pageRetriever = pageRetriever;
                this.nextPageId = startPageId;
                this.continuationPredicate = continuationPredicate;
            }

            @Override
            public boolean hasNext() {
                return !this.isExhausted;
            }

            @Override
            public P next() {
                if (this.isExhausted) {
                    throw new NoSuchElementException();
                }
                final P response = this.pageRetriever.get(this.nextPageId);
                this.nextPageId = response.getContinuationToken();
                this.isExhausted = this.continuationPredicate.shouldContinue(this.nextPageId);
                return response;
            }
        }
    }

    private static final class PageItemIterator<C, T, P extends ContinuablePagedResponse<C, T>> implements Iterator<T> {
        private final Iterator<P> pagedResponseIterator;
        private final Deque<T> queue = new ArrayDeque<>();
        private boolean isExhausted;

        PageItemIterator(Iterator<P> pagedResponseIterator) {
            this.pagedResponseIterator = pagedResponseIterator;
        }

        @Override
        public boolean hasNext() {
            return !this.isExhausted;
        }

        @Override
        public T next() {
            if (this.isExhausted) {
                throw new NoSuchElementException();
            }
            if (this.queue.size() > 0) {
                return this.queue.pop();
            } else {
                if (this.pagedResponseIterator.hasNext()) {
                    final P response = this.pagedResponseIterator.next();
                    this.queue.addAll(response.getValue());
                    this.isExhausted = response.getContinuationToken() == null;
                    if (this.queue.size() > 0) {
                        return this.queue.pop();
                    } else {
                        return this.next();
                    }
                } else {
                    throw new NoSuchElementException();
                }
            }
        }
    }
}
