// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.communication.common;

import com.azure.android.core.credential.AccessToken;

import java9.util.concurrent.CompletableFuture;

abstract class UserCredential {
    private boolean isDisposed = false;

    abstract CompletableFuture<AccessToken> getToken();

    void dispose() {
        this.isDisposed = true;
    }

    boolean isDisposed() {
        return this.isDisposed;
    }
}
