// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.rest.util.paging;

import com.azure.android.core.logging.ClientLogger;
import com.azure.android.core.util.Function;
import com.azure.android.core.util.Predicate;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * PagedIterable provides the ability to enumerate paginated REST responses of type
 * {@link PagedResponse} and individual elements in such pages using {@link Iterable} interface.
 * When processing the response by page each response will contain the elements in the page
 * as well as the REST response details such as status code and headers.
 *
 * @param <T> The type of page elements contained in this {@link PagedIterable}.
 */
public class PagedIterable<T> implements Iterable<T> {
    private final Function<String, PagedResponse<T>> pageRetriever;
    private final Predicate<String> continuationPredicate;
    private final ClientLogger logger;

    /**
     * Creates an instance of {@link PagedIterable}.
     *
     * @param pageRetriever The page retriever.
     * @param continuationPredicate A predicate which determines if paging should continue.
     * @param logger The logger to log.
     */
    public PagedIterable(Function<String, PagedResponse<T>> pageRetriever,
                         Predicate<String> continuationPredicate,
                         ClientLogger logger) {
        this.pageRetriever = pageRetriever;
        this.continuationPredicate = continuationPredicate;
        this.logger = logger;
    }

    @Override
    public Iterator<T> iterator() {
        return new PageItemIterator(byPage().iterator(), this.logger);
    }

    /**
     * Gets a {@link Iterable} of {@link PagedResponse} starting at the first page.
     *
     * @return A {@link Iterable} of {@link PagedResponse}.
     */
    public Iterable<PagedResponse<T>> byPage() {
        return new PagedResponseIterable(this.pageRetriever, null, this.continuationPredicate, this.logger);
    }

    /**
     * Gets a {@link Iterable} of {@link PagedResponse} beginning at the page identified by the given
     * token.
     *
     * @param startPageId A continuation token identifying the page to select.
     * @return A {@link Iterable} of {@link PagedResponse}.
     */
    public Iterable<PagedResponse<T>> byPage(String startPageId) {
        return new PagedResponseIterable(this.pageRetriever, startPageId, this.continuationPredicate, this.logger);
    }

    /**
     * Gets a {@link Iterable} of {@link T} beginning at the page identified by the given
     * token.
     *
     * @param startPageId A continuation token identifying the page to select.
     * @return A {@link Iterable} of {@link T}.
     */
    public Iterator<T> iterator(String startPageId) {
        return new PageItemIterator(byPage(startPageId).iterator(), this.logger);
    }


    /**
     * Retrieve a page with given id {@code pageId}. A {@code null} value for {@code pageId} indicate the initial page.
     *
     * @param pageId The id of the page to retrieve.
     * @return The page with given id.
     */
    public PagedResponse<T> getPage(String pageId) {
        return this.pageRetriever.call(pageId);
    }

    private static final class PagedResponseIterable<T>
        implements Iterable<T> {

        private final Function<String, PagedResponse<T>> pageRetriever;
        private String nextPageId;
        private final Predicate<String> continuationPredicate;
        private final ClientLogger logger;

        PagedResponseIterable(Function<String, PagedResponse<T>> pageRetriever,
                              String startPageId,
                              Predicate<String> continuationPredicate,
                              ClientLogger logger) {
            this.pageRetriever = pageRetriever;
            this.nextPageId = startPageId;
            this.continuationPredicate = continuationPredicate;
            this.logger = logger;
        }

        @Override
        public Iterator<T> iterator() {
            return new PagedResponseIterator(this.pageRetriever,
                this.nextPageId,
                this.continuationPredicate,
                this.logger);
        }

        private static final class PagedResponseIterator<T>
            implements Iterator<PagedResponse<T>> {
            private final Function<String, PagedResponse<T>> pageRetriever;
            private String nextPageId;
            private final Predicate<String> continuationPredicate;
            private final ClientLogger logger;
            private boolean isExhausted;

            PagedResponseIterator(Function<String, PagedResponse<T>> pageRetriever,
                                  String startPageId,
                                  Predicate<String> continuationPredicate,
                                  ClientLogger logger) {
                this.pageRetriever = pageRetriever;
                this.nextPageId = startPageId;
                this.continuationPredicate = continuationPredicate;
                this.logger = logger;
            }

            @Override
            public boolean hasNext() {
                return !this.isExhausted;
            }

            @Override
            public PagedResponse<T> next() {
                if (this.isExhausted) {
                    throw this.logger.logExceptionAsError(new NoSuchElementException());
                }
                final PagedResponse<T> response = this.pageRetriever.call(this.nextPageId);
                this.nextPageId = response.getContinuationToken();
                this.isExhausted = !this.continuationPredicate.test(this.nextPageId);
                return response;
            }
        }
    }

    private static final class PageItemIterator<T> implements Iterator<T> {
        private final Iterator<PagedResponse<T>> pagedResponseIterator;
        private final ClientLogger logger;
        private final Deque<T> queue = new ArrayDeque<>();
        private boolean isExhausted;

        PageItemIterator(Iterator<PagedResponse<T>> pagedResponseIterator, ClientLogger logger) {
            this.pagedResponseIterator = pagedResponseIterator;
            this.logger = logger;
        }

        @Override
        public boolean hasNext() {
            return !this.isExhausted;
        }

        @Override
        public T next() {
            if (this.isExhausted) {
                throw this.logger.logExceptionAsError(new NoSuchElementException());
            }
            if (this.queue.size() > 0) {
                return this.queue.pop();
            } else {
                if (this.pagedResponseIterator.hasNext()) {
                    final PagedResponse<T> response = this.pagedResponseIterator.next();
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