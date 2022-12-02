// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.communication.common;

import java.util.concurrent.Callable;

class MockTokenRefresher implements Callable<String> {
    private String tokenString;
    private Runnable onCallReturn;
    private int callCount;

    public void setToken(String tokenString) {
        this.tokenString = tokenString;
    }

    public void setOnCallReturn(Runnable onCallReturn) {
        this.onCallReturn = onCallReturn;
    }

    public int getCallCount() {
        return callCount;
    }

    @Override
    public String call() {
        this.incrementCallCount();

        if (this.onCallReturn != null) {
            this.onCallReturn.run();
        }

        return this.tokenString;
    }

    private synchronized void incrementCallCount() {
        this.callCount++;
    }
}