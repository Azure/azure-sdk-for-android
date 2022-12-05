// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.credential;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class AzureKeyCredentialTests {

    @Test
    public void ensureKeyValidated() {
        assertThrows(NullPointerException.class, () -> new AzureKeyCredential(null));
    }
}