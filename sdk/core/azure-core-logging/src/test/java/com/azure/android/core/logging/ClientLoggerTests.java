// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.logging;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ClientLoggerTests {

    @Test
    public void canCreateClientLogger() {
        ClientLogger clientLogger = new ClientLogger(ClientLoggerTests.class);
        assertNotNull(clientLogger);
    }
}
