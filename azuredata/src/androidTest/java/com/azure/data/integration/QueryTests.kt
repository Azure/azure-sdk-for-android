package com.azure.data.integration

import android.support.test.runner.AndroidJUnit4
import com.azure.data.AzureData
import com.azure.data.findDocument
import com.azure.data.integration.common.CustomDocument
import com.azure.data.integration.common.DocumentTest
import com.azure.data.integration.common.PartitionedCustomDocment
import com.azure.data.model.Query
import com.azure.data.queryDocuments
import kotlinx.coroutines.runBlocking
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

    private fun createQuery(where: Map<String, Any> = mapOf(CustomDocument::customString.name to customStringValue, CustomDocument::customNumber.name to customNumberValue),
                            orderBy: String? = null, orderByAsc: Boolean = true) : Query {

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

        return query
    }

    @Test
    fun queryDocumentsWithPartition() {

        // ensure at least 1 doc to query
        val doc = createNewDocument()

        // create query
        val query = createQuery()

        AzureData.queryDocuments(collectionId, doc.testKey, databaseId, query, docType) {
            resourceListResponse = it
        }

        await().until { resourceListResponse != null }

        verifyListDocuments(verifyDocValues = false)
    }

    @Test
    fun queryDocumentsCrossPartition() {

        // ensure at least 1 doc to query
        createNewDocument()

        // create query
        val query = createQuery()

        AzureData.queryDocuments(collectionId, databaseId, query, docType) {
            resourceListResponse = it
        }

        await().until { resourceListResponse != null }

        verifyListDocuments(verifyDocValues = false)
    }

    @Test
    fun queryDocumentsInCollectionWithPartition() {

        // ensure at least 1 doc to query
        val doc = createNewDocument()

        // create query
        val query = createQuery()

        collection?.queryDocuments(query, doc.testKey, docType) {
            resourceListResponse = it
        }

        await().until { resourceListResponse != null }

        verifyListDocuments(verifyDocValues = false)
    }

    @Test
    fun queryDocumentsInCollectionCrossPartition() {

        // ensure at least 1 doc to query
        createNewDocument()

        // create query
        val query = createQuery()

        collection?.queryDocuments(query, docType) {
            resourceListResponse = it
        }

        await().until { resourceListResponse != null }

        verifyListDocuments(verifyDocValues = false)
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

        // create query
        var query = createQuery(mapOf(), "customNumber")

        collection?.queryDocuments(query, doc.testKey, docType) {
            resourceListResponse = it
        }

        await().until { resourceListResponse != null }

        verifyListDocuments(3, false)

        assertEquals("Docs not ordered correctly", 1, resourceListResponse?.resource?.items!![0].customNumber)
        assertEquals("Docs not ordered correctly", 2, resourceListResponse?.resource?.items!![1].customNumber)
        assertEquals("Docs not ordered correctly", 3, resourceListResponse?.resource?.items!![2].customNumber)

        // let's sort the other direction now
        resourceListResponse = null

        query = createQuery(mapOf(), "customNumber", false)

        collection?.queryDocuments(query, doc.testKey, docType) {
            resourceListResponse = it
        }

        await().until { resourceListResponse != null }

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

        // create query
        var query = createQuery(mapOf(), "customNumber")

        collection?.queryDocuments(query, docType) {
            resourceListResponse = it
        }

        await().until { resourceListResponse != null }

        // verify we've returned 3 docs
        verifyListDocuments(3, false)

        assertEquals("Docs not ordered correctly", 1, resourceListResponse?.resource?.items!![0].customNumber)
        assertEquals("Docs not ordered correctly", 2, resourceListResponse?.resource?.items!![1].customNumber)
        assertEquals("Docs not ordered correctly", 3, resourceListResponse?.resource?.items!![2].customNumber)

        // let's sort the other direction now
        resourceListResponse = null

        query = createQuery(mapOf(), "customNumber", false)

        collection?.queryDocuments(query, docType) {
            resourceListResponse = it
        }

        await().until { resourceListResponse != null }

        // verify we've returned 3 docs
        verifyListDocuments(3, false)

        assertEquals("Docs not ordered correctly", 3, resourceListResponse?.resource?.items!![0].customNumber)
        assertEquals("Docs not ordered correctly", 2, resourceListResponse?.resource?.items!![1].customNumber)
        assertEquals("Docs not ordered correctly", 1, resourceListResponse?.resource?.items!![2].customNumber)
    }

    @Test
    fun queryDocumentsPaging() {

        val idsFound = mutableListOf<String>()
        val pageSize = 4
        val totalDocs = pageSize * 3

        // let's create 12 docs and page them 4 at a time
        createNewDocuments(totalDocs)

        // create query
        val query = createQuery(mapOf(), "customNumber")

        collection?.queryDocuments(query, docType, pageSize) {
            resourceListResponse = it
        }

        await().until { resourceListResponse != null }

        verifyListDocuments(pageSize, false)
        assertPageN(idsFound, resourceListResponse, pageSize)

        // Get the second one
        resourceListResponse?.let { response ->

            resourceListResponse = null

            response.next {
                assertPageN(idsFound, it, pageSize)
                resourceListResponse = it
            }
        }

        await().until { resourceListResponse != null }

        // Get the third one
        resourceListResponse?.let { response ->

            resourceListResponse = null

            response.next {
                assertPageLast(idsFound, it, pageSize)
                resourceListResponse = it
            }
        }

        await().until { resourceListResponse != null }

        // Try to get one more - should fail
        resourceListResponse!!.next {
            assertPageOnePastLast(it)
        }
    }

    @Test
    fun queryDocumentsPagingLoopCoroutine() {

        val idsFound = mutableListOf<String>()
        val pageSize = 4
        val totalDocs = pageSize * 3

        // let's create 12 docs and page them 4 at a time
        createNewDocuments(totalDocs)

        // create query
        val query = createQuery(mapOf(), "customNumber")

        collection?.queryDocuments(query, docType, pageSize) {
            resourceListResponse = it
        }

        await().until { resourceListResponse != null }

        verifyListDocuments(pageSize, false)
        assertPageN(idsFound, resourceListResponse, pageSize)

        // get all the other pages
        while (resourceListResponse!!.hasMoreResults) {

            // runBlocking{} blocks the current thread until the response comes back
            //  in practice, you'd likely not want to do this on the UI thread, but rather use coroutine/suspend function within your app code
            resourceListResponse = runBlocking { resourceListResponse!!.next() }

            if (resourceListResponse!!.hasMoreResults) {
                assertPageN(idsFound, resourceListResponse, pageSize)
            } else {
                assertPageLast(idsFound, resourceListResponse, pageSize)
            }
        }

        assertEquals("Expected 12 docs but only processed ${idsFound.size}", totalDocs, idsFound.size)

        // Try to get one more - should fail
        resourceListResponse!!.next {
            assertPageOnePastLast(it)
        }
    }

    @Test
    fun queryDocumentsAndGetMorePages() {

        val idsFound = mutableListOf<String>()
        val pageSize = 4
        val totalDocs = pageSize * 3
        val docsToGet = pageSize * 2

        // let's create 12 docs and page them 4 at a time
        createNewDocuments(totalDocs)

        val query = createQuery(where = mapOf())

        collection?.queryDocuments(query, docType, pageSize) {

            it.getMorePages(1) {

                assertPageN(idsFound, it, docsToGet) //checking for docsToGet here rather than pageSize

                resourceListResponse = it
            }
        }

        await().until { resourceListResponse != null }

        assertEquals("Expected $docsToGet docs but ended up with ${resourceListResponse?.resource?.items?.size}", docsToGet, resourceListResponse?.resource?.items?.size)
    }

    @Test
    fun queryDocumentsAndGetAllPages() {

        val idsFound = mutableListOf<String>()
        val pageSize = 4
        val totalDocs = pageSize * 3

        // let's create 12 docs and page them 4 at a time
        createNewDocuments(totalDocs)

        val query = createQuery(where = mapOf())

        collection?.queryDocuments(query, docType, pageSize) {

            it.getAllPages {

                assertPageLast(idsFound, it, totalDocs) //checking for docsToGet here rather than pageSize

                resourceListResponse = it
            }
        }

        await().until { resourceListResponse != null }

        assertEquals("Expected $totalDocs docs but ended up with ${resourceListResponse?.resource?.items?.size}", totalDocs, resourceListResponse?.resource?.items?.size)
    }
}