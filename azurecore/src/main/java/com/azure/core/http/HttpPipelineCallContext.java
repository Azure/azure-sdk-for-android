// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http;

import com.azure.core.util.Context;
import com.azure.core.util.Maybe;

import java.util.Objects;

/**
 * Type representing context local to a single http request and it's response.
 */
public final class HttpPipelineCallContext {
    private Context data;
    private HttpRequest httpRequest;

    /**
     * Package private ctr.
     *
     * Creates HttpPipelineCallContext.
     */
    public HttpPipelineCallContext() {
        this(Context.NONE);
    }

    /**
     * Package private ctr.
     *
     * Creates HttpPipelineCallContext.
     *
     * @param data the data to associate with this context
     *
     * @throws IllegalArgumentException if there are multiple policies with same name
     */
    public HttpPipelineCallContext(Context data) {
        Objects.requireNonNull(data);
        this.data = data;
    }

    /**
     * Stores a key-value data in the context.
     *
     * @param key the key
     * @param value the value
     */
    public void setData(String key, Object value) {
        this.data = this.data.addData(key, value);
    }

    /**
     * Gets a value with the given key stored in the context.
     *
     * @param key the key
     * @return the value
     */
    public Maybe<Object> getData(String key) {
        return this.data.getData(key);
    }

    /**
     * Get the http request.
     *
     * @return the request.
     */
    public HttpRequest httpRequest() {
        return this.httpRequest;
    }

    /**
     * Sets the http request object in the context.
     *
     * @param request request object
     * @return HttpPipelineCallContext
     */
    public HttpPipelineCallContext httpRequest(HttpRequest request) {
        this.httpRequest = request;
        return this;
    }
}
