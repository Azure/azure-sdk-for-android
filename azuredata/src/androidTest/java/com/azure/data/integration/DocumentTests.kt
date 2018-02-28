package com.azure.data.integration

import android.support.test.runner.AndroidJUnit4
import com.azure.data.model.DictionaryDocument
import org.junit.runner.RunWith

/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

@RunWith(AndroidJUnit4::class)
class DocumentTests : DocumentTest<DictionaryDocument>(DictionaryDocument::class.java)