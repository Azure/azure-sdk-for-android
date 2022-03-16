// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.template;

import org.junit.Test;

public class DummyTest {

    @Test
    public void constructor() {
        final Dummy dummy = new Dummy();
    }

    @Test
    public void doNothing() {
        final Dummy dummy = new Dummy();
        dummy.doNothing();
    }
}
