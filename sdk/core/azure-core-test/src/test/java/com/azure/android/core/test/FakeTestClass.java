// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.test;

import com.azure.android.core.logging.ClientLogger;
import com.azure.android.core.test.annotation.DoNotRecord;

import java.lang.reflect.Method;

/**
 * This is a dummy class used to mock scenarios for {@link TestContextManager}.
 */
public final class FakeTestClass {
    public static final Method METHOD_WITHOUT_DONOTRECORD = getTestMethod("testWithoutDoNotRecord");
    public static final Method DONOTRECORD_FALSE_SKIPINPLAYBACK = getTestMethod("testWithDoNotRecordRunInPlayback");
    public static final Method DONOTRECORD_SKIPINPLAYBACK = getTestMethod("testWithDoNotRecordSkipInPlayback");

    public void testWithoutDoNotRecord() {
    }

    @DoNotRecord
    public void testWithDoNotRecordRunInPlayback() {

    }

    @DoNotRecord(skipInPlayback = true)
    public void testWithDoNotRecordSkipInPlayback() {
    }

    private static Method getTestMethod(String methodName) {
        try {
            return FakeTestClass.class.getMethod(methodName);
        } catch (NoSuchMethodException e) {
            throw new ClientLogger(FakeTestClass.class).logExceptionAsWarning(new RuntimeException(e));
        }
    }
}
