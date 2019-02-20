package com.azure.data.integration

import android.support.test.runner.AndroidJUnit4
import com.azure.data.AzureData
import com.azure.data.integration.common.PartitionedCustomDocment
import org.awaitility.Awaitility.await
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

@RunWith(AndroidJUnit4::class)
class CustomDocumentPartitionedTests : DocumentTests<PartitionedCustomDocment>(PartitionedCustomDocment::class.java) {

    init {
        partitionKeyPath = "/testKey"
    }

    @Test
    fun deleteDocumentByIdAndPartitionKey() {

        val doc = createNewDocument()

        AzureData.deleteDocument(doc.id, doc.testKey, collectionId, databaseId) {
            dataResponse = it
        }

        await().until {
            dataResponse != null
        }

        assertDataResponseSuccess(dataResponse)
    }

    @Test
    fun deleteDocumentFromCollectionById() {

        val doc = createNewDocument()

        AzureData.deleteDocument(doc.id, doc.testKey, collection!!) {
            dataResponse = it
        }

        await().until {
            dataResponse != null
        }

        assertDataResponseSuccess(dataResponse)
    }
}