package com.azure.data.integration

import android.support.test.runner.AndroidJUnit4
import com.azure.data.AzureData
import com.azure.data.findDocument
import com.azure.data.integration.common.CustomDocument
import com.azure.data.integration.common.DocumentTest
import com.azure.data.integration.common.PartitionedCustomDocment
import com.azure.data.model.Query
import com.azure.data.queryDocuments
import org.awaitility.Awaitility.await
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

@RunWith(AndroidJUnit4::class)
class QueryTests : DocumentTest<PartitionedCustomDocment>(PartitionedCustomDocment::class.java) {

    init {
        partitionKeyPath = "/testKey"
    }

    private fun queryDocuments(queryFunction: (partitionKey: String, query: Query) -> Unit) {

        // ensure at least 1 doc to query
        val doc = createNewDocument()

        // create query
        val query = Query.select()
                .from(collectionId)
                .where(CustomDocument.customStringKey, DocumentTest.customStringValue)
                .andWhere(CustomDocument.customNumberKey, DocumentTest.customNumberValue)
                .orderBy("_etag", true)

        queryFunction(doc.testKey, query)

        await().until {
            resourceListResponse != null
        }

        verifyListDocuments()
    }

    @Test
    fun queryDocumentsWithPartition() {

        queryDocuments { partitionKey, query ->
            AzureData.queryDocuments(collectionId, partitionKey, databaseId, query, docType) {
                resourceListResponse = it
            }
        }
    }

    @Test
    fun queryDocumentsCrossPartition() {

        queryDocuments { _, query ->
            AzureData.queryDocuments(collectionId, databaseId, query, docType) {
                resourceListResponse = it
            }
        }
    }

    @Test
    fun queryDocumentsInCollectionWithPartition() {

        queryDocuments { partitionKey, query ->
            collection?.queryDocuments(query, partitionKey, docType) {
                resourceListResponse = it
            }
        }
    }

    @Test
    fun queryDocumentsInCollectionCrossPartition() {

        queryDocuments { _, query ->
            collection?.queryDocuments(query, docType) {
                resourceListResponse = it
            }
        }
    }

    @Test
    fun findDocumentInCollectionById() {

        // ensure at least 1 doc to query
        val doc = createNewDocument()

        AzureData.findDocument(doc.id, collectionId, databaseId, docType) {
            resourceListResponse = it
        }

        await().until {
            resourceListResponse != null
        }

        verifyListDocuments()

        assertEquals("", doc.id, resourceListResponse?.resource?.items!![0].id)
    }

    @Test
    fun findDocumentInCollection() {

        // ensure at least 1 doc to query
        val doc = createNewDocument()

        collection?.findDocument(doc.id, docType) {
            resourceListResponse = it
        }

        await().until {
            resourceListResponse != null
        }

        verifyListDocuments()

        assertEquals("", doc.id, resourceListResponse?.resource?.items!![0].id)
    }
}