package com.azure.data.integration

import com.azure.core.log.d
import com.azure.data.*
import com.azure.data.model.*
import com.azure.data.service.Response
import com.azure.data.util.json.gson
import junit.framework.Assert.*
import org.awaitility.Awaitility.await
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException
import java.util.*

/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

abstract class DocumentTest<TDoc : Document>(private val docType: Class<TDoc>)
    : ResourceTest<TDoc>(ResourceType.Document, true, true) {

    @Rule
    @JvmField
    var thrown = ExpectedException.none()!!

    //region Tests

    @Test
    fun testDocumentDateHandling() {

        val newDocument = newDocument()
        newDocument.setValue(customDateKey, customDateValue)

        val json = gson.toJson(newDocument)
        val doc = gson.fromJson(json, docType)

        val date = doc.getValue(customDateKey) as Date

        assertEquals(customDateValue, date)
        assertEquals(customDateValue.time, date.time)
    }

    @Test
    fun testDocumentDataMapHandling() {

        val newDocument = newDocument()

        newDocument.setValue(customStringKey, customStringValue)
        newDocument.setValue(customNumberKey, customNumberValue)
        newDocument.setValue(customBoolKey, true)
        newDocument.setValue(customArrayKey, customArrayValue)
        newDocument.setValue(customDateKey, customDateValue)
        newDocument.setValue(customObjectKey, customObjectValue)

        val json = gson.toJson(newDocument)

        val doc = gson.fromJson(json, docType)

        assertEquals(customStringValue, doc.getValue(customStringKey))
        assertEquals(customNumberValue, (doc.getValue(customNumberKey) as Number).toInt())
        assertEquals(true, doc.getValue(customBoolKey))

        val date = doc.getValue(customDateKey) as Date

        // TODO: need to check this comparison as it seems to fail randomly every now and then
        assertEquals(customDateValue, date)
        assertEquals(customDateValue.time, date.time)

        val list = doc.getValue(customArrayKey)

        if (list is ArrayList<*>) {

            assertEquals(customArrayValue.size, list.size)

            list.forEachIndexed { index, any ->
                assertEquals(customArrayValue[index], (any as Number).toInt())
            }
        }
        else if (list is Array<*>) {

            assertEquals(customArrayValue.size, list.size)

            list.forEachIndexed { index, any ->
                assertEquals(customArrayValue[index], (any as Number).toInt())
            }
        }

        val userObj = doc.getValue(customObjectKey)

        if (userObj is User) {
            assertEquals(customObjectValue.id, userObj.id)
        }
        else {
            assertEquals(customObjectValue.id, (userObj as Map<*, *>)["id"])
        }
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

            var docResponse : Response<TDoc>? = null

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
    fun createDocumentInCollection() {

        createNewDocument(collection)
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

        resourceListResponse = null

        createNewDocuments(3)

        // Get the first one
        AzureData.getDocuments(collectionId, databaseId, docType, 1) { resourceListResponse = it }
        await().until { resourceListResponse != null }
        resourceListResponse.let {
            d{
                resourceListResponse.toString()
            }
            assertNotNull(it?.metadata?.continuation)
            assertTrue(it!!.hasMoreResults)
        }
        verifyListDocuments(1)

        // Get the next one
//        resourceListResponse.next {
//            assertNotNull(it?.metadata?.continuation)
//            assertTrue(it!!.hasMoreResults)
//        }
//
//        // Get the last one
//        resourceListResponse.next {
//            assertNull(it?.metadata?.continuation)
//            assertFalse(it!!.hasMoreResults)
//        }
//
//        // Try to get one more
//        thrown.expect(DocumentClientError::class.java)
//        resourceListResponse.next {}

    }

    @Test
    fun queryDocuments() {

        //ensure at least 1 doc
        createNewDocument()

        val query = Query.select()
                .from(collectionId)
                .where(customStringKey, customStringValue)
                .andWhere(customNumberKey, customNumberValue)
                .orderBy("_etag", true)

        AzureData.queryDocuments(collectionId, databaseId, query, docType) {
            resourceListResponse = it
        }

        await().until {
            resourceListResponse != null
        }

        verifyListDocuments()
    }

    @Test
    fun queryDocumentsInCollection() {

        //ensure at least 1 doc
        createNewDocument()

        val query = Query.select()
                .from(collectionId)
                .where(customStringKey, customStringValue)
                .andWhere(customNumberKey, customNumberValue)
                .orderBy("_etag", true)

        collection?.queryDocuments(query, docType) {
            resourceListResponse = it
        }

        await().until {
            resourceListResponse != null
        }

        verifyListDocuments()
    }

    @Test
    fun getDocument() {

        createNewDocument()

        AzureData.getDocument(createdResourceId, collectionId, databaseId, docType) {
            response = it
        }

        await().until {
            response != null
        }

        val createdDoc = response!!.resource!!
        verifyDocument(createdDoc)
    }

    @Test
    fun getDocumentInCollection() {

        val doc = createNewDocument()

        collection?.getDocument(doc.id, docType) {
            response = it
        }

        await().forever().until {
            response != null
        }

        val createdDoc = response!!.resource!!
        verifyDocument(createdDoc)
    }

    @Test
    fun refreshDocument() {

        val doc = createNewDocument()

        doc.refresh {
            response = it
        }

        await().until {
            response != null
        }

        val createdDoc = response!!.resource!!
        verifyDocument(createdDoc)
    }

    //region Deletes

    @Test
    fun deleteDocument() {

        val doc = createNewDocument()

        doc.delete {
            dataResponse = it
        }

        await().until {
            dataResponse != null
        }

        assertDataResponseSuccess(dataResponse)
    }

    @Test
    fun deleteDocument2() {

        val doc = createNewDocument()

        AzureData.deleteDocument(doc, collectionId, databaseId) {
            dataResponse = it
        }

        await().until {
            dataResponse != null
        }

        assertDataResponseSuccess(dataResponse)
    }

    @Test
    fun deleteDocumentById() {

        createNewDocument()

        AzureData.deleteDocument(createdResourceId, collectionId, databaseId) {
            dataResponse = it
        }

        await().until {
            dataResponse != null
        }

        assertDataResponseSuccess(dataResponse)
    }

    @Test
    fun deleteDocumentFromCollection() {

        val doc = createNewDocument()

        collection?.deleteDocument(doc) {
            dataResponse = it
        }

        await().until {
            dataResponse != null
        }

        assertDataResponseSuccess(dataResponse)
    }

    @Test
    fun deleteDocumentFromCollection2() {

        val doc = createNewDocument()

        AzureData.deleteDocument(doc, collection!!) {
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

        collection?.deleteDocument(doc.id) {
            dataResponse = it
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

        doc.setValue(customStringKey, replacedStringValue)

        AzureData.replaceDocument(doc, collectionId, databaseId) {
            response = it
        }

        await().until {
            response != null
        }

        val replacedDoc = response!!.resource!!
        verifyDocument(replacedDoc, replacedStringValue)
    }

    @Test
    fun replaceDocumentInCollection() {

        val doc = createNewDocument()

        doc.setValue(customStringKey, replacedStringValue)

        collection?.replaceDocument(doc) {
            response = it
        }

        await().until {
            response != null
        }

        val replacedDoc = response!!.resource!!
        verifyDocument(replacedDoc, replacedStringValue)
    }

    //endregion

    private fun newDocument(count : Int=1) : TDoc {

        val doc = docType.newInstance()
        doc.id = createdResourceId(count)
        doc.setValue(customStringKey, customStringValue)
        doc.setValue(customNumberKey, customNumberValue)

        return doc as TDoc
    }

    private fun createNewDocument(coll: DocumentCollection? = null) : TDoc {

        var docResponse: Response<TDoc>? = null

        val doc = newDocument()

        if (coll != null) {
            coll.createDocument(doc) {
                docResponse = it
            }
        } else {
            AzureData.createDocument(doc, collectionId, databaseId) {
                docResponse = it
            }
        }

        await().until {
            docResponse != null
        }

        assertResourceResponseSuccess(docResponse)
        assertEquals(createdResourceId, docResponse?.resource?.id)

        val createdDoc = docResponse!!.resource!!

        return verifyDocument(createdDoc)
    }

    private fun createNewDocuments(count : Int) : List<TDoc> {
        val docs = mutableListOf<TDoc>()
        for(i in 1..count) {
            AzureData.createDocument(newDocument(i), collectionId, databaseId) {
                assertResourceResponseSuccess(it)
                assertEquals(createdResourceId(i), it?.resource?.id)
                docs.add(verifyDocument(it!!.resource!!))
            }
        }
        await().until {
            docs.count() == count
        }
        return docs
    }

    private fun verifyDocument(createdDoc: TDoc, stringValue: String? = null) : TDoc {

        assertNotNull(createdDoc.getValue(customStringKey))
        assertNotNull(createdDoc.getValue(customNumberKey))
        assertEquals(stringValue ?: customStringValue, createdDoc.getValue(customStringKey))
        assertEquals(customNumberValue, (createdDoc.getValue(customNumberKey) as Number).toInt())

        return createdDoc
    }

    private fun verifyListDocuments(count : Int = 1) {

        assertListResponseSuccess(resourceListResponse)
        assertTrue(resourceListResponse?.resource?.count!! == count)

        resourceListResponse?.resource?.items?.forEach {

            verifyDocument(it)
        }
    }

    fun Document.getValue(key: String) : Any? {

        return when {
            this is DictionaryDocument -> return this[key]
            this is CustomDocument -> return when (key) {
                customStringKey -> this.customString
                customNumberKey -> this.customNumber
                customDateKey -> this.customDate
                customBoolKey -> this.customBool
                customArrayKey -> this.customArray
                customObjectKey -> this.customObject
                else -> null
            }
            else -> null
        }
    }

    fun Document.setValue(key: String, value: Any?) {

        when {
            this is DictionaryDocument -> this[key] = value
            this is CustomDocument -> when (key) {
                customStringKey -> this.customString = value as String
                customNumberKey -> this.customNumber = value as Int
                customDateKey -> this.customDate = value as Date
                customBoolKey -> this.customBool = value as Boolean
                customArrayKey -> this.customArray = value as Array<Int>
                customObjectKey -> this.customObject = value as User
            }
        }
    }

    companion object {

        const val customStringKey = "customString"
        const val customStringValue = "Yeah baby\nRock n Roll"
        const val customNumberKey = "customNumber"
        const val customNumberValue = 86
        const val customDateKey = "customDate"
        val customDateValue : Date
        const val customBoolKey = "customBool"
        const val customArrayKey = "customArray"
        val customArrayValue = arrayOf(1, 2, 3, 4)
        const val customObjectKey = "customObject"
        val customObjectValue = User()
        val replacedStringValue = "My replaced string content"

        init {
            val cal = Calendar.getInstance()
            cal.set(Calendar.YEAR, 1988)
            cal.set(Calendar.MONTH, Calendar.JANUARY)
            cal.set(Calendar.DAY_OF_MONTH, 1)
            customDateValue = cal.time
        }
    }
}