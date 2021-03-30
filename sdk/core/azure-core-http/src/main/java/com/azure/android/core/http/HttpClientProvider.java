// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.http;

/**
 * An interface to be implemented by any azure-core plugin that wishes to provide an alternate
 * {@link com.azure.android.core.http.HttpClient} implementation.
 */
@FunctionalInterface
public interface HttpClientProvider {

    /**
     * Creates a new instance of the {@link com.azure.android.core.http.HttpClient} that this HttpClientProvider
     * is configured to create.
     *
     * @return A new {@link com.azure.android.core.http.HttpClient} instance, entirely unrelated to all other instances
     * that were created previously.
     */
    HttpClient createInstance();
}
