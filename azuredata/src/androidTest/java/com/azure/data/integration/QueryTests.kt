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

    private fun queryDocuments(partitionKey: String? = null, expectedDocs: Int = 1,
                               where: Map<String, Any> = mapOf(CustomDocument::customString.name to customStringValue, CustomDocument::customNumber.name to customNumberValue),
                               orderBy: String? = null, orderByAsc: Boolean = true, queryFunction: (partitionKey: String?, query: Query) -> Unit) {

        // create query
        var query = Query.select()
                .from(collectionId)

        for (i in 0 until where.count()) {

            query = if (i == 0) {
                query.where(where.keys.elementAt(i), where.values.elementAt(i))
            } else {
                query.andWhere(where.keys.elementAt(i), where.values.elementAt(i))
            }
        }

        orderBy?.let {
            query = query.orderBy(orderBy, !orderByAsc)
        }

        queryFunction(partitionKey, query)

        await().until {
            resourceListResponse != null
        }

        verifyListDocuments(expectedDocs, false)
    }

    @Test
    fun queryDocumentsWithPartition() {

        // ensure at least 1 doc to query
        val doc = createNewDocument()

        queryDocuments(doc.testKey) { partitionKey, query ->
            AzureData.queryDocuments(collectionId, partitionKey!!, databaseId, query, docType) {
                resourceListResponse = it
            }
        }
    }

    @Test
    fun queryDocumentsCrossPartition() {

        // ensure at least 1 doc to query
        createNewDocument()

        queryDocuments { _, query ->
            AzureData.queryDocuments(collectionId, databaseId, query, docType) {
                resourceListResponse = it
            }
        }
    }

    @Test
    fun queryDocumentsInCollectionWithPartition() {

        // ensure at least 1 doc to query
        val doc = createNewDocument()

        queryDocuments(doc.testKey) { partitionKey, query ->
            collection?.queryDocuments(query, partitionKey!!, docType) {
                resourceListResponse = it
            }
        }
    }

    @Test
    fun queryDocumentsInCollectionCrossPartition() {

        // ensure at least 1 doc to query
        createNewDocument()

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

    @Test
    fun queryPartitionedDocumentsOrderByNumber() {

        // create multiple docs so we can test the ordering
        var doc = PartitionedCustomDocment()
        doc.customNumber = 1
        createNewDocument(doc)

        doc = PartitionedCustomDocment()
        doc.customNumber = 2
        createNewDocument(doc)

        doc = PartitionedCustomDocment()
        doc.customNumber = 3
        createNewDocument(doc)

        queryDocuments(doc.testKey, 3, mapOf(), "customNumber") { partitionKey, query ->
            collection?.queryDocuments(query, partitionKey!!, docType) {
                resourceListResponse = it
            }
        }

        await().until {
            resourceListResponse != null
        }

        // verify we've returned 3 docs
        verifyListDocuments(3, false)

        assertEquals("Docs not ordered correctly", 1, resourceListResponse?.resource?.items!![0].customNumber)
        assertEquals("Docs not ordered correctly", 2, resourceListResponse?.resource?.items!![1].customNumber)
        assertEquals("Docs not ordered correctly", 3, resourceListResponse?.resource?.items!![2].customNumber)

        // let's sort the other direction now
        resourceListResponse = null

        queryDocuments(doc.testKey, 3, mapOf(), "customNumber", false) { partitionKey, query ->
            collection?.queryDocuments(query, partitionKey!!, docType) {
                resourceListResponse = it
            }
        }

        await().until {
            resourceListResponse != null
        }

        // verify we've returned 3 docs
        verifyListDocuments(3, false)

        assertEquals("Docs not ordered correctly", 3, resourceListResponse?.resource?.items!![0].customNumber)
        assertEquals("Docs not ordered correctly", 2, resourceListResponse?.resource?.items!![1].customNumber)
        assertEquals("Docs not ordered correctly", 1, resourceListResponse?.resource?.items!![2].customNumber)
    }

    @Test
    fun queryCrossPartitionDocumentsOrderByNumber() {

        // create multiple docs so we can test the ordering
        var doc = PartitionedCustomDocment()
        doc.customNumber = 1
        doc.testKey = "Partition1"
        createNewDocument(doc)

        doc = PartitionedCustomDocment()
        doc.customNumber = 2
        doc.testKey = "Partition2"
        createNewDocument(doc)

        doc = PartitionedCustomDocment()
        doc.customNumber = 3
        doc.testKey = "Partition1"
        createNewDocument(doc)

        // we'll omit the partition key, making this a cross partition query
        queryDocuments(expectedDocs = 3, where = mapOf(), orderBy = "customNumber") { _, query ->
            collection?.queryDocuments(query, docType) {
                resourceListResponse = it
            }
        }

        await().until {
            resourceListResponse != null
        }

        // verify we've returned 3 docs
        verifyListDocuments(3, false)

        assertEquals("Docs not ordered correctly", 1, resourceListResponse?.resource?.items!![0].customNumber)
        assertEquals("Docs not ordered correctly", 2, resourceListResponse?.resource?.items!![1].customNumber)
        assertEquals("Docs not ordered correctly", 3, resourceListResponse?.resource?.items!![2].customNumber)

        // let's sort the other direction now
        resourceListResponse = null

        // we'll omit the partition key, making this a cross partition query
        queryDocuments(expectedDocs = 3, where = mapOf(), orderBy = "customNumber", orderByAsc = false) { _, query ->
            collection?.queryDocuments(query, docType) {
                resourceListResponse = it
            }
        }

        await().until {
            resourceListResponse != null
        }

        // verify we've returned 3 docs
        verifyListDocuments(3, false)

        assertEquals("Docs not ordered correctly", 3, resourceListResponse?.resource?.items!![0].customNumber)
        assertEquals("Docs not ordered correctly", 2, resourceListResponse?.resource?.items!![1].customNumber)
        assertEquals("Docs not ordered correctly", 1, resourceListResponse?.resource?.items!![2].customNumber)
    }
}