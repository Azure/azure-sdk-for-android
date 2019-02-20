package com.azure.data.integration.common

import com.azure.data.*
import com.azure.data.integration.common.CustomDocument.Companion.customNumberKey
import com.azure.data.integration.common.CustomDocument.Companion.customStringKey
import com.azure.data.model.*
import com.azure.data.service.Response
import org.junit.Assert.*
import org.awaitility.Awaitility.await
import org.junit.Rule
import org.junit.rules.ExpectedException
import java.util.*

/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

abstract class DocumentTest<TDoc : CustomDocument>(val docType: Class<TDoc>)
    : ResourceTest<TDoc>(ResourceType.Document, true, true) {

    @Rule
    @JvmField
    var thrown = ExpectedException.none()!!

    fun newDocument(count : Int = 1) : TDoc {

        val doc = docType.newInstance()
        doc.id = createdResourceId(count)
        doc.setValue(customStringKey, customStringValue)
        doc.setValue(customNumberKey, customNumberValue)

        return doc as TDoc
    }

    fun createNewDocument(coll: DocumentCollection? = null) : TDoc {

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

    fun createNewDocuments(count : Int) : List<TDoc> {

        val docs = mutableListOf<TDoc>()

        for (i in 1..count) {

            AzureData.createDocument(newDocument(i), collectionId, databaseId) {

                assertResourceResponseSuccess(it)
                assertEquals(createdResourceId(i), it.resource?.id)
                docs.add(verifyDocument(it.resource!!))
            }
        }

        await().until {
            docs.count() == count
        }

        return docs
    }

    fun verifyDocument(createdDoc: TDoc, stringValue: String? = null) : TDoc {

        assertNotNull(createdDoc.getValue(customStringKey))
        assertNotNull(createdDoc.getValue(customNumberKey))
        assertEquals(stringValue ?: customStringValue, createdDoc.getValue(customStringKey))
        assertEquals(customNumberValue, (createdDoc.getValue(customNumberKey) as Number).toInt())

        return createdDoc
    }

    fun verifyListDocuments(count : Int = 1) {

        assertListResponseSuccess(resourceListResponse)
        assertTrue(resourceListResponse?.resource?.count!! == count)

        resourceListResponse?.resource?.items?.forEach {

            verifyDocument(it)
        }
    }

    companion object {

        const val customStringValue = "Yeah baby, Rock n Roll"
        const val customNumberValue = 86
        val customDateValue : Date
        val customArrayValue = arrayOf(1, 2, 3, 4)
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