package com.azure.data.integration.offlinetests

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.azure.core.util.ContextProvider
import com.azure.data.AzureData
import com.azure.data.integration.common.CustomDocument
import com.azure.data.integration.common.DocumentTest
import com.azure.data.integration.common.PartitionedCustomDocment
import com.azure.data.model.Database
import com.azure.data.model.DocumentCollection
import com.azure.data.model.Query
import com.azure.data.model.Resource
import com.azure.data.model.service.DataResponse
import com.azure.data.model.service.ListResponse
import com.azure.data.model.service.Response
import com.azure.data.service.resourceCacheFile
import org.awaitility.Awaitility.await
import org.awaitility.Duration
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.*

@RunWith(AndroidJUnit4::class)
class OfflineReadTests: DocumentTest<PartitionedCustomDocment>("OfflineReadTests", PartitionedCustomDocment::class.java, true, false) {

    init {
        partitionKeyPath = "/testKey"
    }

    @Test
    fun fromCacheIsTrueForResourcesFetchedFromLocalCache() {

        var listResponse: ListResponse<Database>? = null

        turnOffInternetConnection()

        AzureData.getDatabases { listResponse = it }

        await().until { listResponse != null }

        assertTrue(listResponse!!.fromCache)
        assertTrue(listResponse!!.isSuccessful)
    }

    @Test
    fun fromCacheIsFalseForResourcesFetchedOnline() {

        var listResponse: ListResponse<Database>? = null

        AzureData.getDatabases { listResponse = it }

        await().until { listResponse != null }

        assertFalse(listResponse!!.fromCache)
        assertTrue(listResponse!!.isSuccessful)
    }

    @Test
    fun fromCacheIsTrueForASingleResourceFetchedFromLocalCache() {

        var getResponse: Response<Database>? = null

        await().atLeast(Duration.TWO_SECONDS)

        turnOffInternetConnection()

        AzureData.getDatabase(databaseId) { getResponse = it }

        await().until { getResponse != null }

        assertTrue(getResponse!!.fromCache)
        assertTrue(getResponse!!.isSuccessful)
    }

    @Test
    fun fromCacheIsFalseForAsSingleResourceFetchedOnline() {

        var getResponse: Response<Database>? = null

        AzureData.getDatabase(databaseId) { getResponse = it }

        await().until { getResponse != null }

        assertFalse(getResponse!!.fromCache)
        assertTrue(getResponse!!.isSuccessful)
    }

    @Test
    fun deletingAResourceRemotelyAlsoDeletesItFromLocalCache() {

        var deleteResponse: DataResponse? = null

        AzureData.deleteDatabase(databaseId) {
            deleteResponse = it
        }

        await().until { deleteResponse != null }

        await().atLeast(Duration.TWO_SECONDS)

        assertNotNull(database)

        assertNotNull(ContextProvider.appContext.resourceCacheFile(database!!))
        assertFalse(ContextProvider.appContext.resourceCacheFile(database!!)!!.exists())
    }

    @Test
    fun databasesAreCachedLocallyWhenNetworkIsReachable() {

        ensureResourcesAreCachedLocallyWhenNetworkIsReachable<Database> {
            AzureData.getDatabases(callback = it)
        }
    }

    @Test
    fun databasesAreFetchedFromLocalCacheWhenNetworkIsNotReachable() {

        ensureResourcesAreFetchedFromLocalCacheWhenNetworkIsNotReachable<Database> {
            AzureData.getDatabases(callback = it)
        }
    }

    @Test
    fun collectionsAreCachedLocallyWhenNetworkIsReachable() {

        ensureCollection()

        ensureResourcesAreCachedLocallyWhenNetworkIsReachable<DocumentCollection> {
            AzureData.getCollections(databaseId, callback = it)
        }
    }

    @Test
    fun collectionsAreFetchedFromLocalCacheWhenNetworkIsNotReachable() {

        ensureCollection()

        ensureResourcesAreFetchedFromLocalCacheWhenNetworkIsNotReachable<DocumentCollection> {
            AzureData.getCollections(databaseId, callback = it)
        }
    }

    @Test
    fun documentsAreCachedLocallyWhenNetworkIsReachable() {

        var createResponse: Response<CustomDocument>? = null

        ensureCollection()

        AzureData.createDocument(CustomDocument(documentId), collectionId, databaseId) {
            createResponse = it
        }


        await().until { createResponse != null }

        ensureResourcesAreCachedLocallyWhenNetworkIsReachable<CustomDocument> {
            AzureData.getDocuments(collectionId, databaseId, CustomDocument::class.java, callback = it)
        }
    }

    @Test
    fun documentsAreFetchedFromLocalCacheWhenNetworkIsNotReachable() {

        var createResponse: Response<CustomDocument>? = null

        ensureCollection()

        AzureData.createDocument(CustomDocument(documentId), collectionId, databaseId) {
            createResponse = it
        }

        await().until { createResponse != null }

        ensureResourcesAreFetchedFromLocalCacheWhenNetworkIsNotReachable<CustomDocument> {
            AzureData.getDocuments(collectionId, databaseId, CustomDocument::class.java, callback = it)
        }
    }

    @Test
    fun queryResultsAreCachedLocally() {

        ensureCollection()

        createNewDocuments(2)

        val query = Query.select().from(collectionId)

        AzureData.queryDocuments(collection!!, query, docType) {

            resourceListResponse = it
        }

        await().until { resourceListResponse != null }

        resourceListResponse!!.resource!!.items.forEach {

            val file = ContextProvider.appContext.resourceCacheFile(it, query)

            // resources are cached on a background thread so we want to be able to wait a bit
            await().atMost(Duration.TWO_SECONDS).until { file.exists() }
        }
    }

    @Test
    fun queryResultsAreFetchedFromLocalCacheWhenNetworkIsNotReachable() {

        ensureCollection()

        createNewDocuments(2)

        val query = Query.select().from(collectionId)

        AzureData.queryDocuments(collectionId, databaseId, query, docType) {

            resourceListResponse = it
        }

        await().until { resourceListResponse != null }

        val onlineResources = resourceListResponse!!.resource!!.items

        // now try offline
        turnOffInternetConnection()
        resourceListResponse = null

        AzureData.queryDocuments(collectionId, databaseId, query, docType) {

            resourceListResponse = it
        }

        await().until { resourceListResponse != null }

        assertListResponseSuccess(resourceListResponse)
        assertTrue(resourceListResponse!!.fromCache)

        val offlineResources = resourceListResponse!!.resource!!.items

        assertFalse(onlineResources.isEmpty())
        assertFalse(offlineResources.isEmpty())
        assertEquals(onlineResources.count(), offlineResources.count())

        offlineResources.forEach {

            assertTrue(onlineResources.any { resource -> resource.resourceId == it.resourceId })
        }
    }

    //region Helpers

    private fun <T: Resource> ensureResourcesAreCachedLocallyWhenNetworkIsReachable(getResources: ((ListResponse<T>) -> Unit) -> Unit) {

        var listResponse: ListResponse<T>? = null

        getResources { listResponse = it }

        await().until { listResponse != null }

        assertNotNull(listResponse!!.resource)

        listResponse!!.resource!!.items.forEach {

            assertNotNull(ContextProvider.appContext.resourceCacheFile(it))
            assertTrue(ContextProvider.appContext.resourceCacheFile(it)!!.exists())
        }
    }

    private fun <T: Resource> ensureResourcesAreFetchedFromLocalCacheWhenNetworkIsNotReachable(getResources: ((ListResponse<T>) -> Unit) -> Unit) {

        var onlineResources: List<T>? = null
        var offlineResources: List<T>? = null
        var listResponse: ListResponse<T>? = null

        getResources {

            onlineResources = it.resource?.items

            await().atLeast(Duration.TWO_SECONDS)

            turnOffInternetConnection()

            getResources { offlineResponse ->

                offlineResources = offlineResponse.resource?.items

                listResponse = offlineResponse
            }
        }

        await().until { listResponse != null }

        assertNotNull(onlineResources)
        assertNotNull(offlineResources)

        offlineResources!!.forEach { offlineResource ->

            assertTrue(onlineResources!!.any { it.id == offlineResource.id })
        }
    }

    //endregion
}