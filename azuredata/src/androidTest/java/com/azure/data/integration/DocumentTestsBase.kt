package com.azure.data.integration

import com.azure.core.log.i
import com.azure.data.*
import com.azure.data.integration.common.CustomDocument
import com.azure.data.integration.common.DocumentTest
import com.azure.data.integration.common.PartitionedCustomDocment
import com.azure.data.model.service.DocumentClientError
import com.azure.data.service.ListResponse
import com.azure.data.service.Response
import com.azure.data.util.json.gson
import org.awaitility.Awaitility.await
import org.junit.Assert.*
import org.junit.Test

/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

abstract class DocumentTestsBase<TDoc : CustomDocument>(resourceName: String, docType: Class<TDoc>) : DocumentTest<TDoc>(resourceName, docType) {

    //region Tests

    @Test
    fun testDocumentDateHandling() {

        val newDocument = newDocument()
        newDocument.customDate = customDateValue

        val json = gson.toJson(newDocument)
        val doc = gson.fromJson(json, docType)

        val compareResult = customDateValue.compareTo(doc.customDate)

        if (compareResult != 0) {
            i { "::DATE COMPARE FAILURE:: \n\tExpected: $customDateValue \n\tActual: $doc.customDate" }
        }

        assertEquals("Round trip dates equal?", 0, compareResult)
        assertEquals(customDateValue.time, doc.customDate.time)
    }

    @Test
    fun testTryCreateDocWithInvalidIds() {

        val badIds = listOf("0123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345",
                "My Id",
                "My/Id",
                "My?Id",
                "My#Id")

        var done = false

        badIds.forEach { id ->

            val doc = newDocument()
            doc.id = id

            var docResponse : Response<TDoc>?

            AzureData.createDocument(doc, collectionId, databaseId) {
                docResponse = it

                assertErrorResponse(docResponse)

                if (id == badIds.last()) {
                    done = true
                }
            }
        }

        await().until {
            done
        }
    }

    @Test
    fun createDocument() {

        createNewDocument()
    }

    @Test
    fun createOrUpdateDocument() {

        val doc = createNewDocument()

        //change something
        doc.customNumber = customNumberValue + 1

        var docResponse: Response<TDoc>? = null

        partitionKeyValue?.let {

            AzureData.createOrUpdateDocument(doc, partitionKeyValue!!, collectionId, databaseId) {
                docResponse = it
            }
        } ?: run {

            AzureData.createOrUpdateDocument(doc, collectionId, databaseId) {
                docResponse = it
            }
        }

        await().until { docResponse != null }

        assertResourceResponseSuccess(docResponse)
        assertEquals(createdResourceId, docResponse?.resource?.id)

        val updatedDoc = docResponse!!.resource!!

        assertNotNull(updatedDoc.customString)
        assertNotNull(updatedDoc.customNumber)
        assertEquals(customNumberValue + 1, updatedDoc.customNumber)
    }

    @Test
    fun createDocumentInCollection() {

        createNewDocument(coll = collection)
    }

    @Test
    fun listDocuments() {

        //ensure at least 1 doc
        createNewDocument()

        AzureData.getDocuments(collectionId, databaseId, docType) {
            resourceListResponse = it
        }

        await().until {
            resourceListResponse != null
        }

        verifyListDocuments()
    }

    @Test
    fun listDocumentsInCollection() {

        //ensure at least 1 doc
        createNewDocument()

        collection?.getDocuments(docType) {
            resourceListResponse = it
        }

        await().until {
            resourceListResponse != null
        }

        verifyListDocuments()
    }

    @Test
    fun listDocumentsWithMaxPerPage() {

        createNewDocuments(3)

        // Test all at once
        resourceListResponse = null
        AzureData.getDocuments(collectionId, databaseId, docType) { resourceListResponse = it }
        await().until { resourceListResponse != null }
        verifyListDocuments(3)

        // Test only 2
        resourceListResponse = null
        AzureData.getDocuments(collectionId, databaseId, docType, 2) { resourceListResponse = it }
        await().until { resourceListResponse != null }
        verifyListDocuments(2)

        // Test only 1
        resourceListResponse = null
        AzureData.getDocuments(collectionId, databaseId, docType, 1) { resourceListResponse = it }
        await().until { resourceListResponse != null }
        verifyListDocuments(1)
    }

    @Test
    fun listDocumentsWithMaxPerPageTooSmall() {
        thrown.expect(DocumentClientError::class.java)
        AzureData.getDocuments(collectionId, databaseId, docType, 0) { resourceListResponse = it }
    }

    @Test
    fun listDocumentsWithMaxPerPageTooBig() {
        thrown.expect(DocumentClientError::class.java)
        AzureData.getDocuments(collectionId, databaseId, docType, 1001) { resourceListResponse = it }
    }

    @Test
    fun listDocumentsPaging() {

        val idsFound = mutableListOf<String>()
        var waitForResponse : ListResponse<TDoc>? = null

        createNewDocuments(3)

        // Get the first one
        AzureData.getDocuments(collectionId, databaseId, docType, 1) { waitForResponse = it }

        await().until { waitForResponse != null }

        assertPageN(idsFound, waitForResponse)

        // Get the second one
        waitForResponse?.let { response ->

            waitForResponse = null

            response.next {
                assertPageN(idsFound, it)
                waitForResponse = it
            }
        }

        await().until { waitForResponse != null }

        // Get the third one
        waitForResponse?.let { response ->

            waitForResponse = null

            response.next {
                assertPageLast(idsFound, it)
                waitForResponse = it
            }
        }

        await().until { waitForResponse != null }

        // Try to get one more - should fail
        waitForResponse!!.next {
            assertPageOnePastLast(it)
        }
    }

    @Test
    fun getDocument() {

        val doc = createNewDocument()

        val partitionKeyValue = partitionKeyValue ?: (doc as? PartitionedCustomDocment)?.testKey

        if (partitionKeyValue != null) {

            AzureData.getDocument(createdResourceId, partitionKeyValue, collectionId, databaseId, docType) {
                response = it
            }
        }
        else {

            AzureData.getDocument(createdResourceId, collectionId, databaseId, docType) {
                response = it
            }
        }

        await().until {
            response != null
        }

        assertResourceResponseSuccess(response)

        val createdDoc = response!!.resource!!
        verifyDocument(createdDoc, doc)
    }

    @Test
    fun getDocumentInCollection() {

        val doc = createNewDocument()

        val partitionKeyValue = partitionKeyValue ?: (doc as? PartitionedCustomDocment)?.testKey

        if (partitionKeyValue != null) {

            collection?.getDocument(doc.id, partitionKeyValue, docType) {
                response = it
            }
        }
        else {

            collection?.getDocument(doc.id, docType) {
                response = it
            }
        }

        await().until {
            response != null
        }

        assertResourceResponseSuccess(response)

        val createdDoc = response!!.resource!!
        verifyDocument(createdDoc, doc)
    }

    @Test
    fun refreshDocument() {

        val doc = createNewDocument()

        partitionKeyValue?.let {

            AzureData.refresh(doc, partitionKeyValue!!) {
                response = it
            }
        } ?: run {

            doc.refresh {
                response = it
            }
        }

        await().until {
            response != null
        }

        val createdDoc = response!!.resource!!
        verifyDocument(createdDoc, doc)
    }

    //region Deletes

    @Test
    fun deleteDocument() {

        val doc = createNewDocument()

        partitionKeyValue?.let {

            AzureData.delete(doc, partitionKeyValue!!) {
                dataResponse = it
            }
        } ?: run {

            doc.delete {
                dataResponse = it
            }
        }

        await().until {
            dataResponse != null
        }

        assertDataResponseSuccess(dataResponse)
    }

    @Test
    fun deleteDocumentFromCollection() {

        val doc = createNewDocument()

        partitionKeyValue?.let {

            AzureData.delete(doc, partitionKeyValue!!) {
                dataResponse = it
            }

        } ?: run {

            collection?.deleteDocument(doc) {
                dataResponse = it
            }
        }

        await().until {
            dataResponse != null
        }

        assertDataResponseSuccess(dataResponse)
    }

    //endregion

    @Test
    fun replaceDocument() {

        val doc = createNewDocument()
        doc.customString = replacedStringValue

        partitionKeyValue?.let {

            AzureData.replaceDocument(doc, partitionKeyValue!!, collectionId, databaseId) {
                response = it
            }
        } ?: run {

            AzureData.replaceDocument(doc, collectionId, databaseId) {
                response = it
            }
        }

        await().until {
            response != null
        }

        assertResourceResponseSuccess(response)

        val replacedDoc = response!!.resource!!
        verifyDocument(replacedDoc, doc)
    }

    @Test
    fun replaceDocumentInCollection() {

        val doc = createNewDocument()
        doc.customString = replacedStringValue

        partitionKeyValue?.let {

            collection?.replaceDocument(doc, partitionKeyValue!!) {
                response = it
            }
        } ?: run {

            collection?.replaceDocument(doc) {
                response = it
            }
        }

        await().until {
            response != null
        }

        assertResourceResponseSuccess(response)

        val replacedDoc = response!!.resource!!
        verifyDocument(replacedDoc, doc)
    }

    //endregion
}