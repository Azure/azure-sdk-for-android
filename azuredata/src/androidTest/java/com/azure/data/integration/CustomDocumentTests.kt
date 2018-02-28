package com.azure.data.integration

import android.support.test.runner.AndroidJUnit4
import com.azure.data.model.Document
import com.azure.data.model.User
import org.junit.runner.RunWith
import java.util.*

/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

@RunWith(AndroidJUnit4::class)
class CustomDocumentTests : DocumentTest<CustomDocument>(CustomDocument::class.java)