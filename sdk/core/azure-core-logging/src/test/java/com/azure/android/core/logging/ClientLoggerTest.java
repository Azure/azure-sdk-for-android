// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.logging;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

public class ClientLoggerTest {
    @Test
    public void canCreateClientLogger() {
        ClientLogger clientLogger = new ClientLogger(ClientLoggerTest.class);
        assertNotNull(clientLogger);
    }
}