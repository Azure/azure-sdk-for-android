// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.http.interceptor;

import com.azure.android.core.http.HttpHeader;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * The log configuration options for HTTP messages.
 */
public class LogOptions {
    private static final List<String> DEFAULT_HEADERS_WHITELIST = Arrays.asList(
        HttpHeader.ACCEPT,
        HttpHeader.CACHE_CONTROL,
        HttpHeader.CLIENT_REQUEST_ID,
        HttpHeader.CONNECTION,
        HttpHeader.CONTENT_LENGTH,
        HttpHeader.CONTENT_TYPE,
        HttpHeader.DATE,
        HttpHeader.ETAG,
        HttpHeader.EXPIRES,
        HttpHeader.IF_MATCH,
        HttpHeader.IF_MODIFIED_SINCE,
        HttpHeader.IF_NONE_MATCH,
        HttpHeader.IF_UNMODIFIED_SINCE,
        HttpHeader.LAST_MODIFIED,
        HttpHeader.PRAGMA,
        HttpHeader.RETURN_CLIENT_REQUEST_ID,
        HttpHeader.REQUEST_ID,
        HttpHeader.RETRY_AFTER,
        HttpHeader.SERVER,
        HttpHeader.TRACEPARENT,
        HttpHeader.TRANSFER_ENCODING,
        HttpHeader.USER_AGENT
    );

    private Set<String> allowedHeaderNames;
    private Set<String> allowedQueryParamNames;

    /**
     * Creates a new instance which includes the default headers to whitelist.
     */
    public LogOptions() {
        allowedHeaderNames = new HashSet<>(DEFAULT_HEADERS_WHITELIST);
        allowedQueryParamNames = new HashSet<>();
    }

    /**
     * Gets the whitelisted headers that should be logged.
     *
     * @return The list of whitelisted headers.
     */
    public Set<String> getAllowedHeaderNames() {
        return allowedHeaderNames;
    }

    /**
     * Sets the given whitelisted headers that should be logged.
     * <p>
     * This method sets the provided header names to be the whitelisted header names which will be logged for all HTTP
     * requests and responses, overwriting any previously configured headers, including the default set. Additionally,
     * users can use {@link LogOptions#addAllowedHeaderName(String)} or {@link LogOptions#getAllowedHeaderNames()} to
     * add or remove more headers names to the existing set of allowed header names.
     *
     * @param allowedHeaderNames The list of whitelisted header names from the user.
     * @return The updated HttpLogOptions object.
     */
    public LogOptions setAllowedHeaderNames(final Set<String> allowedHeaderNames) {
        this.allowedHeaderNames = allowedHeaderNames == null ? new HashSet<>() : allowedHeaderNames;

        return this;
    }

    /**
     * Sets the given whitelisted header to the default header set that should be logged.
     *
     * @param allowedHeaderName The whitelisted header name from the user.
     * @return The updated {@link LogOptions} object.
     * @throws NullPointerException If {@code allowedHeaderName} is {@code null}.
     */
    public LogOptions addAllowedHeaderName(final String allowedHeaderName) {
        Objects.requireNonNull(allowedHeaderName);
        this.allowedHeaderNames.add(allowedHeaderName);

        return this;
    }

    /**
     * Gets the whitelisted query parameters.
     *
     * @return The list of whitelisted query parameters.
     */
    public Set<String> getAllowedQueryParamNames() {
        return allowedQueryParamNames;
    }

    /**
     * Sets the given whitelisted query params to be displayed in the logging info.
     *
     * @param allowedQueryParamNames The list of whitelisted query params from the user.
     * @return The updated HttpLogOptions object.
     */
    public LogOptions setAllowedQueryParamNames(final Set<String> allowedQueryParamNames) {
        this.allowedQueryParamNames = allowedQueryParamNames == null ? new HashSet<>() : allowedQueryParamNames;

        return this;
    }

    /**
     * Sets the given whitelisted query param that should be logged.
     *
     * @param allowedQueryParamName The whitelisted query param name from the user.
     * @return The updated {@link LogOptions} object.
     * @throws NullPointerException If {@code allowedQueryParamName} is {@code null}.
     */
    public LogOptions addAllowedQueryParamName(final String allowedQueryParamName) {
        this.allowedQueryParamNames.add(allowedQueryParamName);

        return this;
    }
}
