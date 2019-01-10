package com.azure.data.integration

import android.support.test.runner.AndroidJUnit4
import org.junit.runner.RunWith

/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

@RunWith(AndroidJUnit4::class)
class CustomDocumentTests : DocumentTest<CustomDocument>(CustomDocument::class.java)