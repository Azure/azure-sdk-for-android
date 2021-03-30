// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.test.implementation;

import com.azure.android.core.http.HttpClient;
import com.azure.android.core.test.http.MockHttpClient;

public class RestProxyWithMockTests extends RestProxyTests {
    @Override
    protected int getWireMockPort() {
        return 80;
    }

    @Override
    protected HttpClient createHttpClient() {
        return new MockHttpClient();
    }
}
