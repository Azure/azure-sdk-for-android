package com.azure.data.integration.common

import android.content.Context
import android.support.test.InstrumentationRegistry
import android.util.Log
import com.azure.core.log.i
import com.azure.core.log.startLogging
import com.azure.data.AzureData
import com.azure.data.integration.offlinetests.mocks.MockOkHttpClient
import com.azure.data.model.*
import com.azure.data.model.service.DataResponse
import com.azure.data.model.service.ListResponse
import com.azure.data.model.service.ResourceType
import com.azure.data.model.service.Response
import com.azure.data.service.*
import okhttp3.OkHttpClient
import org.awaitility.Awaitility.await
import org.junit.After
import org.junit.Assert.*
import org.junit.Before

/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

open class ResourceTest<TResource : Resource>(resourceName: String,
                                              private val ensureDatabase : Boolean = true,
                                              private val ensureCollection : Boolean = true,
                                              private val ensureDocument : Boolean = false) {

    val databaseId = "AndroidTest$resourceName${ResourceType.Database.name}"
    val collectionId = "AndroidTest$resourceName${ResourceType.Collection.name}"
    val documentId = "AndroidTest$resourceName${ResourceType.Document.name}"
    val createdResourceId = "AndroidTest$resourceName"

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
    var partitionKeyPath: String? = null

    @Before
    open fun setUp() {

        startLogging(Log.VERBOSE)

        i { "********* Begin Test Setup *********" }

        if (!AzureData.isConfigured) {

            // Context of the app under test.
            val appContext = InstrumentationRegistry.getTargetContext()

            configureAzureData(appContext)
        } else {

            turnOnInternetConnection()
        }

        purgeCache()
        deleteResources()

        if (ensureDatabase || ensureCollection || ensureDocument) {

            // Dbs with provisioned throughput REQUIRE partition keys
            if (partitionKeyPath != null) {
                ensureDatabase(1000)
            } else {
                ensureDatabase()
            }
        }

        if (ensureCollection || ensureDocument) {
            ensureCollection()
        }

        if (ensureDocument) {
            ensureDocument()
        }

        i { "********* End Test Setup *********" }
    }

    @After
    open fun tearDown() {

        i { "********* Begin Test Tear Down *********" }

        deleteResources()
        purgeCache()

        i { "********* End Test Tear Down *********" }
    }

    //region Config/setup/control flow

    open fun configureAzureData(appContext: Context) {

        AzureData.configure(
                appContext,
                azureCosmosDbAccount,
                azureCosmosPrimaryKey,
                PermissionMode.All)
    }

    fun turnOnInternetConnection() {

        if (DocumentClient.client is MockOkHttpClient) {

            DocumentClient.client = OkHttpClient()
        }
    }

    fun turnOffInternetConnection() {

        val client = MockOkHttpClient()
        client.hasNetworkError = true

        DocumentClient.client = client
    }

    private fun purgeCache() {

        ResourceCache.shared.purge()
        ResourceWriteOperationQueue.shared.purge()
    }

    fun resetResponse() {

        response = null
    }

    //endregion

    //region Resource creation

    fun tryCreateDatabase(throughput: Int? = null) : Response<Database>? {

        var dbResponse: Response<Database>? = null

        throughput?.let {

            AzureData.createDatabase(databaseId, throughput) {
                dbResponse = it
            }
        } ?: run {

            AzureData.createDatabase(databaseId) {
                dbResponse = it
            }
        }

        await().until { dbResponse != null }

        return dbResponse
    }

    fun ensureDatabase(throughput: Int? = null) : Database {

        val dbResponse = tryCreateDatabase(throughput)

        assertResourceResponseSuccess(dbResponse)
        assertEquals(databaseId, dbResponse?.resource?.id)

        database = dbResponse!!.resource!!

        return database!!
    }

    fun ensureDatabases(count : Int) : List<Database> {

        var dbResponse: Response<Database>? = null
        val databases = mutableListOf<Database>()

        for (i in 1..count) {

            AzureData.createDatabase(databaseId(i)) {
                dbResponse = it
            }

            await().until { dbResponse != null }

            assertResourceResponseSuccess(dbResponse)
            assertEquals(databaseId(i), dbResponse?.resource?.id)

            databases.add(dbResponse!!.resource!!)
        }

        return databases
    }

    fun tryCreateCollection(throughput: Int? = null) : Response<DocumentCollection>? {

        var collectionResponse: Response<DocumentCollection>? = null

        if (!partitionKeyPath.isNullOrBlank()) {

            throughput?.let {
                AzureData.createCollection(collectionId, throughput, partitionKeyPath!!, databaseId) {
                    collectionResponse = it
                }
            } ?: run {
                AzureData.createCollection(collectionId, partitionKeyPath!!, databaseId) {
                    collectionResponse = it
                }
            }
        } else { //fixed collection - deprecated
            AzureData.createCollection(collectionId, databaseId) {
                collectionResponse = it
            }
        }

        await().until { collectionResponse != null }

        return collectionResponse
    }

    fun ensureCollection(throughput: Int? = null) : DocumentCollection {

        val collectionResponse = tryCreateCollection(throughput)

        assertResourceResponseSuccess(collectionResponse)
        assertEquals(collectionId, collectionResponse?.resource?.id)

        collection = collectionResponse!!.resource!!

        return collection!!
    }

    private fun ensureDocument() : Document {

        return if (partitionKeyPath != null) {

            var docResponse: Response<PartitionedCustomDocment>? = null
            val doc = PartitionedCustomDocment(documentId)

            AzureData.createDocument(doc, collection!!) {
                docResponse = it
            }

            await().until { docResponse != null }

            assertResourceResponseSuccess(docResponse)

            document = docResponse!!.resource!!

            document!!

        } else {

            var docResponse: Response<CustomDocument>? = null
            val doc = CustomDocument(documentId)

            AzureData.createDocument(doc, collection!!) {
                docResponse = it
            }

            await().until { docResponse != null }

            assertResourceResponseSuccess(docResponse)

            document = docResponse!!.resource!!

            document!!
        }
    }

    private fun deleteResources() {

        var deleteResponse: DataResponse? = null

        //delete the DB - this should delete all attached resources

        AzureData.deleteDatabase(databaseId) { response ->

            i { "Attempted to delete test database.  Result: ${response.isSuccessful}" }
            deleteResponse = response
        }

        await().until { deleteResponse != null }
    }

    //endregion

    //region Assertions

    private fun assertResponsePopulated(response: Response<*>?) {

        assertNotNull("Response was null", response)
        assertNotNull(response!!.request)

        // offline responses will not have the OkHttp response or Json data
        if (!response.fromCache) {

            assertNotNull(response.response)
            assertNotNull(response.jsonData)
        }
    }

    fun <TResource : Resource> assertListResponseSuccess(response: ListResponse<TResource>?, verifyDocValues: Boolean = true, verifyIsPopulated: Boolean = true) {

        assertResponsePopulated(response!!)
        assertTrue("response.isSuccessful is not True", response.isSuccessful)
        assertFalse("response.isErrored is not False", response.isErrored)
        assertNotNull("response.resource is null", response.resource)

        if (verifyIsPopulated || verifyDocValues) {

            val list = response.resource as ResourceList<*>

            assertTrue("Returned List<TResource> list.isPopulated is False", list.isPopulated)

            if (verifyDocValues) {

                list.items.forEach { item ->

                    assertResourcePropertiesSet(item)
                }
            }
        }
    }

    fun assertDataResponseSuccess(response: DataResponse?) {

        assertResponsePopulated(response)
        assertTrue(response!!.isSuccessful)
        assertFalse(response.isErrored)
    }

    fun assertResourceResponseSuccess(response: Response<*>?) {

        assertResponsePopulated(response)
        assertTrue(response!!.isSuccessful)
        assertFalse(response.isErrored)

        if (response.resource is Resource) {

            assertResourcePropertiesSet(response.resource as Resource)
        }
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

    fun <T : Resource> assertPageN(idsFound: MutableList<String>, response: ListResponse<T>?, pageSize: Int = 1, checkCreatedId: Boolean = true) {

        assertListResponseSuccess(response)
        assertNotNull(response!!.metadata.continuation)
        assertNotNull(response.resource?.items)
        assertEquals(pageSize, response.resource?.items?.size)

        for (item in response.resource?.items!!) {

            if (checkCreatedId) {
                assertTrue(item.id.startsWith(createdResourceId))
            }

            assertFalse(idsFound.contains(item.id))
            idsFound.add(item.id)
        }

        assertTrue(response.hasMoreResults)
    }

    fun <T : Resource> assertPageLast(idsFound: MutableList<String>, response: ListResponse<T>?, pageSize: Int = 1, checkCreatedId: Boolean = true) {

        assertListResponseSuccess(response)
        assertNotNull(response!!.resource?.items)
        assertEquals(pageSize, response.resource?.items?.size)

        for (item in response.resource?.items!!) {

            if (checkCreatedId) {
                assertTrue(item.id.startsWith(createdResourceId))
            }

            assertFalse(idsFound.contains(item.id))
            idsFound.add(item.id)
        }

        assertFalse(response.hasMoreResults)
    }

    fun <T : Resource> assertPageOnePastLast(response: ListResponse<T>?) {

        assertErrorResponse(response)
    }

    private fun assertResourcePropertiesSet(resource: Resource) {

        assertNotNull("resource.id is null", resource.id)
        assertNotNull("resource.resourceId is null", resource.resourceId)
        assertNotNull("resource.selfLink is null", resource.selfLink)
        assertNotNull("resource.altLink is null", resource.altLink)
        assertNotNull("resource.etag is null", resource.etag)
        assertNotNull("resource.timestamp is null", resource.timestamp)
    }

    //endregion
}