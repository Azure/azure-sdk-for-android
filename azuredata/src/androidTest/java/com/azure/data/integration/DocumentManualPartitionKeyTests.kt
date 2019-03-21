package com.azure.data.integration

import android.support.test.runner.AndroidJUnit4
import com.azure.data.AzureData
import com.azure.data.integration.common.CustomDocument
import com.azure.data.service.Response
import org.awaitility.Awaitility.await
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

class PartitionedDoc(id: String? = null) : CustomDocument(id) {

    // partitioned doc WITHOUT annotation - use case for when you don't own the model to annotate the partition key property
    var testKey = "PartitionKeyValue"
}

@RunWith(AndroidJUnit4::class)
class DocumentManualPartitionKeyTests : DocumentTestsBase<PartitionedDoc>("DocumentManualPartitionKeyTests", PartitionedDoc::class.java) {

    init {
        partitionKeyPath = "/testKey"
    }

    // must match the value extracted from the document (server validates this)
    private val partitionKeyValue = "PartitionKeyValue"

    @Test
    fun createDocumentWithManualPartitionKey() {

        createNewDocument(partitionKey = partitionKeyValue)
    }

    @Test
    fun createOrUpdateDocumentWithManualPartitionKey() {

        val doc = createNewDocument(partitionKey = partitionKeyValue)

        //change something
        doc.customNumber = customNumberValue + 1

        var docResponse: Response<PartitionedDoc>? = null

        AzureData.createOrUpdateDocument(doc, partitionKeyValue, collectionId, databaseId) {
            docResponse = it
        }

        await().until { docResponse != null }

        assertResourceResponseSuccess(docResponse)
        Assert.assertEquals(createdResourceId, docResponse?.resource?.id)

        val updatedDoc = docResponse!!.resource!!

        Assert.assertNotNull(updatedDoc.customString)
        Assert.assertNotNull(updatedDoc.customNumber)
        Assert.assertEquals(customNumberValue + 1, updatedDoc.customNumber)
    }
}