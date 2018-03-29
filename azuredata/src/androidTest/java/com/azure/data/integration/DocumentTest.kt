package com.azure.data.integration

import com.azure.data.*
import com.azure.data.model.*
import com.azure.data.services.ResourceResponse
import com.azure.data.util.json.gson
import junit.framework.Assert.*
import org.awaitility.Awaitility.await
import org.junit.Test
import java.util.*

/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

abstract class DocumentTest<TDoc : Document>(private val docType: Class<TDoc>)
    : ResourceTest<TDoc>(ResourceType.Document, true, true) {

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

            var docResponse : ResourceResponse<TDoc>? = null

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

        AzureData.getDocument(resourceId, collectionId, databaseId, docType) {
            resourceResponse = it
        }

        await().until {
            resourceResponse != null
        }

        val createdDoc = resourceResponse!!.resource!!
        verifyDocument(createdDoc)
    }

    @Test
    fun getDocumentInCollection() {

        val doc = createNewDocument()

        collection?.getDocument(doc.resourceId!!, docType) {
            resourceResponse = it
        }

        await().until {
            resourceResponse != null
        }

        val createdDoc = resourceResponse!!.resource!!
        verifyDocument(createdDoc)
    }

    @Test
    fun refreshDocument() {

        val doc = createNewDocument()

        doc.refresh {
            resourceResponse = it
        }

        await().until {
            resourceResponse != null
        }

        val createdDoc = resourceResponse!!.resource!!
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

        assertResponseSuccess(dataResponse)
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

        assertResponseSuccess(dataResponse)
    }

    @Test
    fun deleteDocumentById() {

        createNewDocument()

        AzureData.deleteDocument(resourceId, collectionId, databaseId) {
            dataResponse = it
        }

        await().until {
            dataResponse != null
        }

        assertResponseSuccess(dataResponse)
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

        assertResponseSuccess(dataResponse)
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

        assertResponseSuccess(dataResponse)
    }

    @Test
    fun deleteDocumentFromCollectionByResourceId() {

        val doc = createNewDocument()

        collection?.deleteDocument(doc.resourceId!!) {
            dataResponse = it
        }

        await().until {
            dataResponse != null
        }

        assertResponseSuccess(dataResponse)
    }

    //endregion

    @Test
    fun replaceDocument() {

        val doc = createNewDocument()

        doc.setValue(customStringKey, replacedStringValue)

        AzureData.replaceDocument(doc, collectionId, databaseId) {
            resourceResponse = it
        }

        await().until {
            resourceResponse != null
        }

        val replacedDoc = resourceResponse!!.resource!!
        verifyDocument(replacedDoc, replacedStringValue)
    }

    @Test
    fun replaceDocumentInCollection() {

        val doc = createNewDocument()

        doc.setValue(customStringKey, replacedStringValue)

        collection?.replaceDocument(doc) {
            resourceResponse = it
        }

        await().until {
            resourceResponse != null
        }

        val replacedDoc = resourceResponse!!.resource!!
        verifyDocument(replacedDoc, replacedStringValue)
    }

    //endregion

    private fun newDocument() : TDoc {

        val doc = docType.newInstance()
        doc.id = resourceId
        doc.setValue(customStringKey, customStringValue)
        doc.setValue(customNumberKey, customNumberValue)

        return doc as TDoc
    }

    private fun createNewDocument(coll: DocumentCollection? = null) : TDoc {

        var docResponse: ResourceResponse<TDoc>? = null
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

        assertResponseSuccess(docResponse)
        assertEquals(resourceId, docResponse?.resource?.id)

        val createdDoc = docResponse!!.resource!!

        return verifyDocument(createdDoc)
    }

    private fun verifyDocument(createdDoc: TDoc, stringValue: String? = null) : TDoc {

        assertNotNull(createdDoc.getValue(customStringKey))
        assertNotNull(createdDoc.getValue(customNumberKey))
        assertEquals(stringValue ?: customStringValue, createdDoc.getValue(customStringKey))
        assertEquals(customNumberValue, (createdDoc.getValue(customNumberKey) as Number).toInt())

        return createdDoc
    }

    private fun verifyListDocuments() {

        assertResponseSuccess(resourceListResponse)
        assertTrue(resourceListResponse?.resource?.count!! > 0)

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