// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.communication.common;

import java.util.concurrent.Future;

import com.azure.android.core.credential.AccessToken;

abstract class UserCredential {
    private boolean isDisposed = false;

    abstract Future<AccessToken> getToken();

    void dispose() {
        this.isDisposed = true;
    }

    boolean isDisposed() {
        return this.isDisposed;
    }
}
