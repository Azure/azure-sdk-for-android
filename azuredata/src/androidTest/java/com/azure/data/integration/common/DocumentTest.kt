package com.azure.data.integration.common

import com.azure.data.*
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

abstract class DocumentTest<TDoc : CustomDocument>(resourceName: String, val docType: Class<TDoc>)
    : ResourceTest<TDoc>(resourceName, true, true) {

    @Rule
    @JvmField
    var thrown = ExpectedException.none()!!

    fun newDocument(count : Int = 1) : TDoc {

        val doc = docType.newInstance()
        doc.id = createdResourceId(count)
        doc.customString = customStringValue
        doc.customNumber = customNumberValue

        return doc as TDoc
    }

    fun createNewDocument(doc: TDoc = newDocument(), partitionKey: String? = null, coll: DocumentCollection? = null) : TDoc {

        var docResponse: Response<TDoc>? = null

        partitionKey?.let {

            if (coll != null) {

                coll.createDocument(doc, partitionKey) {
                    docResponse = it
                }
            } else {

                AzureData.createDocument(doc, partitionKey, collectionId, databaseId) {
                    docResponse = it
                }
            }
        } ?: run {

            if (coll != null) {

                coll.createDocument(doc) {
                    docResponse = it
                }
            } else {

                AzureData.createDocument(doc, collectionId, databaseId) {
                    docResponse = it
                }
            }
        }

        await().forever().until {
            docResponse != null
        }

        assertResourceResponseSuccess(docResponse)
        assertEquals(doc.id, docResponse?.resource?.id)

        val createdDoc = docResponse!!.resource!!

        return verifyDocument(createdDoc, doc)
    }

    fun createNewDocuments(count : Int) : List<TDoc> {

        val docs = mutableListOf<TDoc>()

        for (i in 1..count) {

            val docToCreate = newDocument(i)

            AzureData.createDocument(docToCreate, collectionId, databaseId) {

                assertResourceResponseSuccess(it)
                assertEquals(createdResourceId(i), it.resource?.id)
                docs.add(verifyDocument(it.resource!!, docToCreate))
            }
        }

        await().until {
            docs.count() == count
        }

        return docs
    }

    fun verifyDocument(createdDoc: TDoc, referenceDoc: TDoc? = null, verifyDocValues: Boolean = true) : TDoc {

        assertNotNull(createdDoc.customString)
        assertNotNull(createdDoc.customNumber)

        if (verifyDocValues) {
            assertEquals(referenceDoc?.customString ?: customStringValue, createdDoc.customString)
            assertEquals(referenceDoc?.customNumber ?: customNumberValue, createdDoc.customNumber)
        }

        return createdDoc
    }

    fun verifyListDocuments(count : Int = 1, verifyDocValues: Boolean = true) {

        assertListResponseSuccess(resourceListResponse)
        assertTrue(resourceListResponse?.resource?.count!! == count)

        resourceListResponse?.resource?.items?.forEach {

            verifyDocument(it, verifyDocValues = verifyDocValues)
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