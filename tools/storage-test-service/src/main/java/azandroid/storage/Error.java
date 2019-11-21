// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package azandroid.storage;

public class Error {
    private final String error;

    public Error(String error) {
        this.error = error;
    }

    public String getError() {
        return this.error;
    }
}
