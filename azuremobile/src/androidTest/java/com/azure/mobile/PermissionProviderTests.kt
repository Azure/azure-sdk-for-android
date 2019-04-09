package com.azure.mobile

import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import android.util.Log
import com.azure.core.log.d
import com.azure.core.log.startLogging
import com.azure.data.*
import com.azure.data.model.*
import com.azure.data.model.service.DataResponse
import com.azure.data.service.ListResponse
import com.azure.data.model.service.Response
import org.awaitility.Awaitility.await
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.net.URL

/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

@RunWith(AndroidJUnit4::class)
class PermissionProviderTests {

    private val functionUrl: URL = URL("") // = your function app url
    private val databaseName: String = "" // = your database account name

    private val databaseId = "PermissionTestDb"
    private val collectionId = "PermissionTestColl"

    private var collection: DocumentCollection? = null
    private var document: Document? = null

    private val customStringValue = "customStringValue"
    private val customNumberValue = 86

    fun ensureDatabase() : Database {

        var dbResponse: Response<Database>? = null

        AzureData.createDatabase(databaseId) {
            dbResponse = it
        }

        await().until {
            dbResponse != null
        }

        assertResourceResponseSuccess(dbResponse)
        assertEquals(databaseId, dbResponse?.resource?.id)

        return dbResponse!!.resource!!
    }

    fun ensureCollection() : DocumentCollection {

        var collectionResponse: Response<DocumentCollection>? = null

        AzureData.createCollection(collectionId, databaseId) {
            collectionResponse = it
        }

        await().until {
            collectionResponse != null
        }

        assertResourceResponseSuccess(collectionResponse)
        assertEquals(collectionId, collectionResponse?.resource?.id)

        return collectionResponse!!.resource!!
    }

    private fun assertResponsePopulated(response: Response<*>?) {

        assertNotNull(response)
        assertNotNull(response!!.request)
        assertNotNull(response.response)
        assertNotNull(response.jsonData)
    }

    private fun assertResourcePropertiesSet(resource: Resource) {

        assertNotNull(resource.id)
        assertNotNull(resource.resourceId)
        assertNotNull(resource.selfLink)
        assertNotNull(resource.altLink)
        assertNotNull(resource.etag)
        assertNotNull(resource.timestamp)
    }

    private fun <TResource : Resource> assertListResponseSuccess(response: ListResponse<TResource>?) {

        assertNotNull(response)
        assertResponsePopulated(response!!)
        assertTrue(response.isSuccessful)
        assertFalse(response.isErrored)
        assertNotNull(response.resource)

        val list = response.resource as ResourceList<*>

        assertTrue(list.isPopulated)

        list.items.forEach { item ->
            assertResourcePropertiesSet(item)
        }
    }

    private fun assertDataResponseSuccess(response: DataResponse?) {

        assertNotNull(response)
        assertResponsePopulated(response!!)
        assertTrue(response.isSuccessful)
        assertFalse(response.isErrored)
    }

    private fun assertResourceResponseSuccess(response: Response<*>?) {

        assertNotNull(response)
        assertResponsePopulated(response!!)
        assertTrue(response.isSuccessful)
        assertFalse(response.isErrored)

        assertResourcePropertiesSet(response.resource as Resource)
    }

    fun assertResponseFailure(response: Response<*>?) {

        assertResponsePopulated(response)
        assertNotNull(response!!.error)
        assertFalse(response.isSuccessful)
        assertTrue(response.isErrored)
    }

    fun assertErrorResponse(response: Response<*>?) {

        assertNotNull(response)
        assertNotNull(response!!.error)
        assertFalse(response.isSuccessful)
        assertTrue(response.isErrored)
    }

    @Before
    fun setUp() {

        startLogging(Log.VERBOSE)

        d{"********* Begin Test Setup *********"}

        if (!AzureData.isConfigured) {
            // Context of the app under test.
            val appContext = InstrumentationRegistry.getTargetContext()

            AzureData.configure(
                    appContext,
                    databaseName,
                    DefaultPermissionProvider(functionUrl))
        }

        d{"********* End Test Setup *********"}
    }

    @Test
    fun testDefaultPermissionProvider() {

        var shouldContinue = false

        AzureData.getCollection(collectionId, databaseId) {

            assertNotNull(it.resource)
            assertResourceResponseSuccess(it)
            collection = it.resource!!
        }

        await().until {
            collection != null
        }

        val newDocument = CustomDocument()

        newDocument.customString = customStringValue
        newDocument.customNumber = customNumberValue

        collection?.createDocument(newDocument) {

            assertNotNull(it.resource)
            assertResourceResponseSuccess(it)
            document = it.resource!!
        }

        await().until {
            document != null
        }

        collection?.getDocuments(CustomDocument::class.java) {

            assertNotNull(it.resource)
            assertListResponseSuccess(it)

            shouldContinue = true
        }

        await().until {
            shouldContinue
        }

        shouldContinue = false

        collection?.getDocument(newDocument.id, CustomDocument::class.java) {

            assertNotNull(it.resource)
            assertResourceResponseSuccess(it)

            shouldContinue = true
        }

        await().until {
            shouldContinue
        }

        shouldContinue = false

        collection?.deleteDocument(newDocument) {

            assertDataResponseSuccess(it)

            shouldContinue = true
        }

        await().until {
            shouldContinue
        }

        // done?
    }
}