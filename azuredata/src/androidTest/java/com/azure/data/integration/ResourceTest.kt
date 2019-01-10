package com.azure.data.integration

import android.content.Context
import android.support.test.InstrumentationRegistry
import android.util.Log
import com.azure.core.log.d
import com.azure.core.log.startLogging
import com.azure.data.AzureData
import com.azure.data.model.*
import com.azure.data.service.DataResponse
import com.azure.data.service.ListResponse
import com.azure.data.service.Response
import junit.framework.Assert
import org.awaitility.Awaitility.await
import org.junit.After
import org.junit.Assert.*
import org.junit.Before

/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

open class ResourceTest<TResource : Resource>(resourceType: ResourceType,
                                              private val ensureDatabase : Boolean = true,
                                              private val ensureCollection : Boolean = true,
                                              private val ensureDocument : Boolean = false) {

    val databaseId = "AndroidTest${ResourceType.Database.name}"
    val collectionId = "AndroidTest${ResourceType.Collection.name}"
    val documentId = "AndroidTest${ResourceType.Document.name}"
    val createdResourceId = "AndroidTest${resourceType.name}"

    fun databaseId(count : Int = 0) = "$databaseId${if (count<2) "" else count.toString()}"
    fun collectionId(count : Int = 0) = "$collectionId${if (count<2) "" else count.toString()}"
    fun documentId(count : Int = 0) = "$documentId${if (count<2) "" else count.toString()}"
    fun createdResourceId(count : Int? = 0) = "$createdResourceId${if (count==null) "" else if (count<2) "" else count.toString()}"

    var response: Response<TResource>? = null
    var resourceListResponse: ListResponse<TResource>? = null
    var dataResponse: DataResponse? = null

    var database: Database? = null
    var collection: DocumentCollection? = null
    var document: Document? = null

    @Before
    open fun setUp() {

        startLogging(Log.VERBOSE)

        d{"********* Begin Test Setup *********"}

        if (!AzureData.isConfigured) {
            // Context of the app under test.
            val appContext = InstrumentationRegistry.getTargetContext()

            configureAzureData(appContext)
        }

        deleteResources()

        if (ensureDatabase || ensureCollection || ensureDocument) {
            ensureDatabase()
        }

        if (ensureCollection || ensureDocument) {
            ensureCollection()
        }

        if (ensureDocument) {
            ensureDocument()
        }

        d{"********* End Test Setup *********"}
    }

    open fun configureAzureData(appContext: Context) {

        AzureData.configure(
                appContext,
                azureCosmosDbAccount,
                azureCosmosPrimaryKey,
                PermissionMode.All)
    }

    @After
    open fun tearDown() {

        d{"********* Begin Test Tear Down *********"}

        deleteResources()

        d{"********* End Test Tear Down *********"}
    }

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

        database = dbResponse!!.resource!!

        return database!!
    }

    fun ensureDatabase(count : Int) : List<Database> {

        var dbResponse: Response<Database>? = null
        val databases = mutableListOf<Database>()

        for(i in 1..count) {
            AzureData.createDatabase(databaseId(i)) {
                dbResponse = it
            }

            await().until {
                dbResponse != null
            }

            assertResourceResponseSuccess(dbResponse)
            assertEquals(databaseId(i), dbResponse?.resource?.id)

            databases.add(dbResponse!!.resource!!)
        }
        return databases
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

        collection = collectionResponse!!.resource!!

        return collection!!
    }

    private fun ensureDocument() : Document {

        var docResponse: Response<CustomDocument>? = null
        val doc = CustomDocument(documentId)

        AzureData.createDocument(doc, collection!!) {
            docResponse = it
        }

        await().until {
            docResponse != null
        }

        document = docResponse!!.resource!!

        return document!!
    }

    private fun deleteResources() {

        var deleteResponse: DataResponse? = null

        //delete the DB - this should delete all attached resources

        AzureData.deleteDatabase(databaseId) { response ->
            d{"Attempted to delete test database.  Result: ${response.isSuccessful}"}
            deleteResponse = response
        }

        await().until {
            deleteResponse != null
        }
    }

    private fun assertResponsePopulated(response: Response<*>?) {

        assertNotNull(response)
        assertNotNull(response!!.request)
        assertNotNull(response.response)
        assertNotNull(response.jsonData)
    }

    fun <TResource : Resource> assertListResponseSuccess(response: ListResponse<TResource>?) {

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

    fun assertDataResponseSuccess(response: DataResponse?) {

        assertNotNull(response)
        assertResponsePopulated(response!!)
        assertTrue(response.isSuccessful)
        assertFalse(response.isErrored)
    }

    fun assertResourceResponseSuccess(response: Response<*>?) {

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

    fun <T : Resource> assertPage1(idsFound: MutableList<String>, response: ListResponse<T>?) {
        Assert.assertNotNull(response!!.metadata.continuation)
        Assert.assertNotNull(response.resource?.items)
        Assert.assertEquals(1, response.resource?.items?.size)
        val id = response.resource?.items?.get(0)?.id!!
        Assert.assertTrue(id.startsWith(createdResourceId))
        Assert.assertTrue(response.hasMoreResults)
        idsFound.add(id)
    }

    fun <T : Resource> assertPageN(idsFound: MutableList<String>, response: ListResponse<T>?) {
        Assert.assertNotNull(response!!.metadata.continuation)
        Assert.assertNotNull(response.resource?.items)
        Assert.assertEquals(1, response.resource?.items?.size)
        val id = response.resource?.items?.get(0)?.id!!
        Assert.assertTrue(id.startsWith(createdResourceId))
        Assert.assertFalse(idsFound.contains(id))
        Assert.assertTrue(response.hasMoreResults)
        idsFound.add(id)
    }

    fun <T : Resource> assertPageLast(idsFound: MutableList<String>, response: ListResponse<T>?) {
        Assert.assertNotNull(response!!.resource?.items)
        Assert.assertEquals(1, response.resource?.items?.size)
        val id = response.resource?.items?.get(0)?.id!!
        Assert.assertTrue(id.startsWith(createdResourceId))
        Assert.assertFalse(idsFound.contains(id))
        idsFound.add(id)
        Assert.assertFalse(response.hasMoreResults)
    }

    fun <T : Resource> assertPageOnePastLast(response: ListResponse<T>?) {
        assertErrorResponse(response)
    }

    private fun assertResourcePropertiesSet(resource: Resource) {

        assertNotNull(resource.id)
        assertNotNull(resource.resourceId)
        assertNotNull(resource.selfLink)
        assertNotNull(resource.altLink)
        assertNotNull(resource.etag)
        assertNotNull(resource.timestamp)
    }

    fun resetResponse() {

        response = null
    }
}
