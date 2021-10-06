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
            LogUtils.ensureValidLoggerName("com.example.MyClass"));
    }

    public void processLongTagForOlderAndroidVersion() {
        setInternalState(Build.VERSION.class, "SDK_INT", 23);

        assertEquals("c*.e*.l*.MyClass",
            LogUtils.ensureValidLoggerName("com.example.logging.MyClass"));
    }

    @Test
    public void processLongTagWithLongClassNameForOlderAndroidVersion() {
        setInternalState(Build.VERSION.class, "SDK_INT", 23);

        assertEquals("c*.e*.l*.MyLongClassNa*",
            LogUtils.ensureValidLoggerName("com.example.logging.MyLongClassName"));
    }

    @Test
    public void processLongTagWithSingleLettersInPackageForOlderAndroidVersion() {
        setInternalState(Build.VERSION.class, "SDK_INT", 23);

        assertEquals("c.e.l*.MyLongClassName",
            LogUtils.ensureValidLoggerName("c.e.logging.MyLongClassName"));
    }

    @Test
    public void processLongTagWithSingleLettersInPackageAndLongClassNameForOlderAndroidVersion() {
        setInternalState(Build.VERSION.class, "SDK_INT", 23);

        assertEquals("c.e.l*.MyVeryLongClass*",
            LogUtils.ensureValidLoggerName("c.e.logging.MyVeryLongClassName"));
    }

    @Test
    public void processLongTagWithoutPackageForOlderAndroidVersion() {
        setInternalState(Build.VERSION.class, "SDK_INT", 23);

        assertEquals("MyVeryLongClassNameWit*",
            LogUtils.ensureValidLoggerName("MyVeryLongClassNameWithoutPackage"));
    }

    @Test
    public void processShortTagForNewerAndroidVersion() {
        setInternalState(Build.VERSION.class, "SDK_INT", 26);

        assertEquals("com.example.MyClass",
            LogUtils.ensureValidLoggerName("com.example.MyClass"));
    }

    @Test
    public void processLongTagForNewerAndroidVersion() {
        setInternalState(Build.VERSION.class, "SDK_INT", 26);

        assertEquals("com.example.logging.MyClass",
            LogUtils.ensureValidLoggerName("com.example.logging.MyClass"));
    }

    @Test
    public void processLongTagWithLongClassNameForNewerAndroidVersion() {
        setInternalState(Build.VERSION.class, "SDK_INT", 26);

        assertEquals("com.example.logging.MyLongClassName",
            LogUtils.ensureValidLoggerName("com.example.logging.MyLongClassName"));
    }

    @Test
    public void processLongTagWithSingleLettersInPackageForNewerAndroidVersion() {
        setInternalState(Build.VERSION.class, "SDK_INT", 26);

        assertEquals("c.e.logging.MyLongClassName",
            LogUtils.ensureValidLoggerName("c.e.logging.MyLongClassName"));
    }

    @Test
    public void processLongTagWithSingleLettersInPackageAndLongClassNameForNewerAndroidVersion() {
        setInternalState(Build.VERSION.class, "SDK_INT", 26);

        assertEquals("c.e.logging.MyVeryLongClassName",
            LogUtils.ensureValidLoggerName("c.e.logging.MyVeryLongClassName"));
    }

    @Test
    public void processLongTagWithoutPackageForNewerAndroidVersion() {
        setInternalState(Build.VERSION.class, "SDK_INT", 26);

        assertEquals("MyVeryLongClassNameWithoutPackage",
            LogUtils.ensureValidLoggerName("MyVeryLongClassNameWithoutPackage"));
    }
}
