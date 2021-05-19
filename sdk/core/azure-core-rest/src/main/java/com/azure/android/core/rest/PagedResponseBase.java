// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.rest;

import com.azure.android.core.http.HttpHeaders;
import com.azure.android.core.http.HttpRequest;
import com.azure.android.core.util.paging.Page;

import java.util.Collections;
import java.util.List;

/**
 * Represents an HTTP response that contains a list of items deserialized
 * into a {@link Page}.
 *
 * @param <H> The HTTP response headers
 * @param <T> The type of items contained in the {@link Page}
 * @see com.azure.android.core.rest.PagedResponse
 */
public class PagedResponseBase<H, T> implements PagedResponse<T> {
    private final HttpRequest request;
    private final int statusCode;
    private final H deserializedHeaders;
    private final HttpHeaders headers;
    private final List<T> items;
    private final String continuationToken;

    /**
     * Creates a new instance of the PagedResponseBase type.
     *
     * @param request The HttpRequest that was sent to the service whose response resulted in this response.
     * @param statusCode The status code from the response.
     * @param headers The headers from the response.
     * @param page The page of content returned from the service within the response.
     * @param deserializedHeaders The headers, deserialized into an instance of type H.
     */
    public PagedResponseBase(HttpRequest request, int statusCode, HttpHeaders headers, Page<String, T> page,
                             H deserializedHeaders) {
        this(request, statusCode, headers, page.getElements(), page.getContinuationToken(), deserializedHeaders);
    }

    /**
     * Creates a new instance of the PagedResponseBase type.
     *
     * @param request The HttpRequest that was sent to the service whose response resulted in this response.
     * @param statusCode The status code from the response.
     * @param headers The headers from the response.
     * @param items The items returned from the service within the response.
     * @param continuationToken The continuation token returned from the service, to enable future requests to pick up
     *      from the same place in the paged iteration.
     * @param deserializedHeaders The headers, deserialized into an instance of type H.
     */
    public PagedResponseBase(HttpRequest request, int statusCode, HttpHeaders headers, List<T> items,
                             String continuationToken, H deserializedHeaders) {
        this.request = request;
        this.statusCode = statusCode;
        this.headers = headers;
        this.items = items;
        this.continuationToken = continuationToken;
        this.deserializedHeaders = deserializedHeaders;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<T> getElements() {
        return Collections.unmodifiableList(this.items);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getContinuationToken() {
        return this.continuationToken;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getStatusCode() {
        return this.statusCode;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HttpHeaders getHeaders() {
        return this.headers;
    }

    /**
     * @return the request which resulted in this paged response.
     */
    @Override
    public HttpRequest getRequest() {
        return this.request;
    }

    /**
     * Get the headers from the HTTP response, transformed into the header type H.
     *
     * @return an instance of header type H, containing the HTTP response headers.
     */
    public H getDeserializedHeaders() {
        return this.deserializedHeaders;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() {
    }
}
