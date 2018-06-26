package com.azure.data.integration.offlinetests

import android.support.test.runner.AndroidJUnit4
import com.azure.data.AzureData
import com.azure.data.integration.CustomDocument
import com.azure.data.model.Database
import com.azure.data.model.DocumentCollection
import com.azure.data.model.Resource
import com.azure.data.service.DataResponse
import com.azure.data.service.ListResponse
import com.azure.data.service.Response
import com.azure.data.service.resourceCacheFile
import org.awaitility.Awaitility.await
import org.awaitility.Duration
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.*

@RunWith(AndroidJUnit4::class)
class OfflineReadTests: OfflineTests("OfflineReadTests") {

    @Test
    fun fromCacheIsTrueForResourcesFetchedFromLocalCache() {
        var createResponse: Response<Database>? = null
        var listResponse: ListResponse<Database>? = null

        deleteResources {
            AzureData.createDatabase(databaseId) { createResponse = it }
        }

        await().until { createResponse != null }

        turnOffInternetConnection()

        AzureData.getDatabases { listResponse = it }

        await().until { listResponse != null }

        assertTrue(listResponse!!.fromCache)
        assertTrue(listResponse!!.isSuccessful)
    }

    @Test
    fun fromCacheIsFalseForResourcesFetchedOnline() {
        var createResponse: Response<Database>? = null
        var listResponse: ListResponse<Database>? = null

        deleteResources {
            AzureData.createDatabase(databaseId) { createResponse = it }
        }

        await().until { createResponse != null }

        AzureData.getDatabases { listResponse = it }

        await().until { listResponse != null }

        assertFalse(listResponse!!.fromCache)
        assertTrue(listResponse!!.isSuccessful)
    }

    @Test
    fun fromCacheIsTrueForASingleResourceFetchedFromLocalCache() {
        var createResponse: Response<Database>? = null
        var getResponse: Response<Database>? = null

        deleteResources {
            AzureData.createDatabase(databaseId) { createResponse = it }
        }

        await().until { createResponse != null }

        await().atLeast(Duration.TWO_SECONDS)

        turnOffInternetConnection()

        AzureData.getDatabase(databaseId) { getResponse = it }

        await().until { getResponse != null }

        assertTrue(getResponse!!.fromCache)
        assertTrue(getResponse!!.isSuccessful)
    }

    @Test
    fun fromCacheIsFalseForAsSingleResourceFetchedOnline() {
        var createResponse: Response<Database>? = null
        var getResponse: Response<Database>? = null

        deleteResources {
            AzureData.createDatabase(databaseId) { createResponse = it }
        }

        await().until { createResponse != null }

        AzureData.getDatabase(databaseId) { getResponse = it }

        await().until { getResponse != null }

        assertFalse(getResponse!!.fromCache)
        assertTrue(getResponse!!.isSuccessful)
    }

    @Test
    fun deletingAResourceRemotelyAlsoDeletesItFromLocalCache() {
        var database: Database? = null
        var deleteResponse: DataResponse? = null

        deleteResources {
            AzureData.createDatabase(databaseId) {
                database = it.resource

                AzureData.deleteDatabase(databaseId) {
                    deleteResponse = it
                }
            }
        }

        await().until { deleteResponse != null }

        await().atLeast(Duration.TWO_SECONDS)

        assertNotNull(database)

        assertNotNull(appContext.resourceCacheFile(database!!))
        assertFalse(appContext.resourceCacheFile(database!!)!!.exists())
    }

    @Test
    fun databasesAreCachedLocallyWhenNetworkIsReachable() {
        var createResponse: Response<Database>? = null

        deleteResources {
            AzureData.createDatabase(databaseId) { createResponse = it }
        }

        await().until { createResponse != null }

        ensureResourcesAreCachedLocallyWhenNetworkIsReachable<Database> {
            AzureData.getDatabases(callback = it)
        }
    }

    @Test
    fun databasesAreFetchedFromLocalCacheWhenNetworkIsNotReachable() {
        var createResponse: Response<Database>? = null

        deleteResources {
            AzureData.createDatabase(databaseId) { createResponse = it }
        }

        await().until { createResponse != null }

        ensureResourcesAreFetchedFromLocalCacheWhenNetworkIsNotReachable<Database> {
            AzureData.getDatabases(callback = it)
        }
    }

    @Test
    fun collectionsAreCachedLocallyWhenNetworkIsReachable() {
        var createResponse: Response<DocumentCollection>? = null

        deleteResources {
            AzureData.createDatabase(databaseId) {
                AzureData.createCollection(collectionId, databaseId) {
                    createResponse = it
                }
            }
        }

        await().until { createResponse != null }

        ensureResourcesAreCachedLocallyWhenNetworkIsReachable<DocumentCollection> {
            AzureData.getCollections(databaseId, callback = it)
        }
    }

    @Test
    fun collectionsAreFetchedFromLocalCacheWhenNetworkIsNotReachable() {
        var createResponse: Response<DocumentCollection>? = null

        deleteResources {
            AzureData.createDatabase(databaseId) {
                AzureData.createCollection(collectionId, databaseId) {
                    createResponse = it
                }
            }
        }

        await().until { createResponse != null }

        ensureResourcesAreFetchedFromLocalCacheWhenNetworkIsNotReachable<DocumentCollection> {
            AzureData.getCollections(databaseId, callback = it)
        }
    }

    @Test
    fun documentsAreCachedLocallyWhenNetworkIsReachable() {
        var createResponse: Response<CustomDocument>? = null

        deleteResources {
            AzureData.createDatabase(databaseId) {
                AzureData.createCollection(collectionId, databaseId) {
                    AzureData.createDocument(CustomDocument(documentId), collectionId, databaseId) {
                        createResponse = it
                    }
                }
            }
        }

        await().until { createResponse != null }

        ensureResourcesAreCachedLocallyWhenNetworkIsReachable<CustomDocument> {
            AzureData.getDocuments(collectionId, databaseId, CustomDocument::class.java, callback = it)
        }
    }

    @Test
    fun documentsAreFetchedFromLocalCacheWhenNetworkIsNotReachable() {
        var createResponse: Response<CustomDocument>? = null

        deleteResources {
            AzureData.createDatabase(databaseId) {
                AzureData.createCollection(collectionId, databaseId) {
                    AzureData.createDocument(CustomDocument(documentId), collectionId, databaseId) {
                        createResponse = it
                    }
                }
            }
        }

        await().until { createResponse != null }

        ensureResourcesAreFetchedFromLocalCacheWhenNetworkIsNotReachable<CustomDocument> {
            AzureData.getDocuments(collectionId, databaseId, CustomDocument::class.java, callback = it)
        }
    }

    //region

    private fun <T: Resource> ensureResourcesAreCachedLocallyWhenNetworkIsReachable(
        getResources: ((ListResponse<T>) -> Unit) -> Unit
    ) {
        var listResponse: ListResponse<T>? = null

        getResources { listResponse = it }

        await().until { listResponse != null }

        assertNotNull(listResponse!!.resource)

        listResponse!!.resource!!.items.forEach {
            assertNotNull(appContext.resourceCacheFile(it))
            assertTrue(appContext.resourceCacheFile(it)!!.exists())
        }
    }

    private fun <T: Resource> ensureResourcesAreFetchedFromLocalCacheWhenNetworkIsNotReachable(
        getResources: ((ListResponse<T>) -> Unit) -> Unit
    ) {
        var onlineResources: List<T>? = null
        var offlineResources: List<T>? = null
        var listResponse: ListResponse<T>? = null

        getResources {
            onlineResources = it.resource?.items

            await().atLeast(Duration.TWO_SECONDS)

            turnOffInternetConnection()

            getResources {
                offlineResources = it.resource?.items

                listResponse = it
            }
        }

        await().until { listResponse != null }

        assertNotNull(onlineResources)
        assertNotNull(offlineResources)

        offlineResources!!.forEach { offlineResource ->
            assertTrue(onlineResources!!.any { it.id == offlineResource.id })
        }
    }

    private fun deleteResources(callback: () -> Unit) {
        AzureData.deleteDatabase(databaseId) { callback() }
    }

    //endregion
}