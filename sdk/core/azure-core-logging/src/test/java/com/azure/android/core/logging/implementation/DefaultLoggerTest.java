// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.logging.implementation;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.azure.android.core.logging.implementation.DefaultLogger;

import org.junit.jupiter.api.Test;

public class DefaultLoggerTest {
    @Test
    public void processShortTag() {
        assertEquals("com.example.MyClass", DefaultLogger.loggerNameToTag("com.example.MyClass"));
    }

    @Test
    public void processLongTag() {
        assertEquals("c*.e*.l*.MyClass", DefaultLogger.loggerNameToTag("com.example.logging.MyClass"));
    }

    @Test
    public void processLongTagWithLongClassName() {
        assertEquals("c*.e*.l*.MyLongClassNa*", DefaultLogger.loggerNameToTag("com.example.logging.MyLongClassName"));
    }

    @Test
    public void processLongTagWithSingleLettersInPackage() {
        assertEquals("c.e.l*.MyLongClassName", DefaultLogger.loggerNameToTag("c.e.logging.MyLongClassName"));
    }

    @Test
    public void processLongTagWithSingleLettersInPackageAndLongClassName() {
        assertEquals("c.e.l*.MyVeryLongClass*", DefaultLogger.loggerNameToTag("c.e.logging.MyVeryLongClassName"));
    }

    @Test
    public void processLongTagWithoutPackage() {
        assertEquals("MyVeryLongClassNameWit*", DefaultLogger.loggerNameToTag("MyVeryLongClassNameWithoutPackage"));
    }
}
