// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.logging.implementation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.powermock.reflect.Whitebox.setInternalState;

import android.os.Build;

import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Build.VERSION.class)
public class DefaultLoggerTest {
    @Test
    public void processShortTagForOlderAndroidVersion() {
        setInternalState(Build.VERSION.class, "SDK_INT", 23);

        assertEquals("com.example.MyClass",
            DefaultLogger.forceValidLoggerName("com.example.MyClass"));
    }

    public void processLongTagForOlderAndroidVersion() {
        setInternalState(Build.VERSION.class, "SDK_INT", 23);

        assertEquals("c*.e*.l*.MyClass",
            DefaultLogger.forceValidLoggerName("com.example.logging.MyClass"));
    }

    @Test
    public void processLongTagWithLongClassNameForOlderAndroidVersion() {
        setInternalState(Build.VERSION.class, "SDK_INT", 23);

        assertEquals("c*.e*.l*.MyLongClassNa*",
            DefaultLogger.forceValidLoggerName("com.example.logging.MyLongClassName"));
    }

    @Test
    public void processLongTagWithSingleLettersInPackageForOlderAndroidVersion() {
        setInternalState(Build.VERSION.class, "SDK_INT", 23);

        assertEquals("c.e.l*.MyLongClassName",
            DefaultLogger.forceValidLoggerName("c.e.logging.MyLongClassName"));
    }

    @Test
    public void processLongTagWithSingleLettersInPackageAndLongClassNameForOlderAndroidVersion() {
        setInternalState(Build.VERSION.class, "SDK_INT", 23);

        assertEquals("c.e.l*.MyVeryLongClass*",
            DefaultLogger.forceValidLoggerName("c.e.logging.MyVeryLongClassName"));
    }

    @Test
    public void processLongTagWithoutPackageForOlderAndroidVersion() {
        setInternalState(Build.VERSION.class, "SDK_INT", 23);

        assertEquals("MyVeryLongClassNameWit*",
            DefaultLogger.forceValidLoggerName("MyVeryLongClassNameWithoutPackage"));
    }

    @Test
    public void processShortTagForNewerAndroidVersion() {
        setInternalState(Build.VERSION.class, "SDK_INT", 26);

        assertEquals("com.example.MyClass",
            DefaultLogger.forceValidLoggerName("com.example.MyClass"));
    }

    @Test
    public void processLongTagForNewerAndroidVersion() {
        setInternalState(Build.VERSION.class, "SDK_INT", 26);

        assertEquals("com.example.logging.MyClass",
            DefaultLogger.forceValidLoggerName("com.example.logging.MyClass"));
    }

    @Test
    public void processLongTagWithLongClassNameForNewerAndroidVersion() {
        setInternalState(Build.VERSION.class, "SDK_INT", 26);

        assertEquals("com.example.logging.MyLongClassName",
            DefaultLogger.forceValidLoggerName("com.example.logging.MyLongClassName"));
    }

    @Test
    public void processLongTagWithSingleLettersInPackageForNewerAndroidVersion() {
        setInternalState(Build.VERSION.class, "SDK_INT", 26);

        assertEquals("c.e.logging.MyLongClassName",
            DefaultLogger.forceValidLoggerName("c.e.logging.MyLongClassName"));
    }

    @Test
    public void processLongTagWithSingleLettersInPackageAndLongClassNameForNewerAndroidVersion() {
        setInternalState(Build.VERSION.class, "SDK_INT", 26);

        assertEquals("c.e.logging.MyVeryLongClassName",
            DefaultLogger.forceValidLoggerName("c.e.logging.MyVeryLongClassName"));
    }

    @Test
    public void processLongTagWithoutPackageForNewerAndroidVersion() {
        setInternalState(Build.VERSION.class, "SDK_INT", 26);

        assertEquals("MyVeryLongClassNameWithoutPackage",
            DefaultLogger.forceValidLoggerName("MyVeryLongClassNameWithoutPackage"));
    }
}
