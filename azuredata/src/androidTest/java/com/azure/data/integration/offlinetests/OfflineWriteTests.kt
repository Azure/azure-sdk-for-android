package com.azure.data.integration.offlinetests

import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import android.util.Log
import com.azure.core.log.startLogging
import com.azure.data.AzureData
import com.azure.data.integration.CustomDocument
import com.azure.data.integration.azureCosmosDbAccount
import com.azure.data.integration.azureCosmosPrimaryKey
import com.azure.data.model.Database
import com.azure.data.integration.offlinetests.mocks.MockOkHttpClient
import com.azure.data.model.PermissionMode
import com.azure.data.service.*
import okhttp3.OkHttpClient
import org.junit.Before
import org.junit.Test
import org.awaitility.Awaitility.*
import org.junit.runner.RunWith
import org.junit.Assert.*

@RunWith(AndroidJUnit4::class)
class OfflineWriteTests {

    private val resourceName = "OfflineWriteTests"
    private val databaseId = "${resourceName}Database"
    private val collectionId = "${resourceName}Collection"
    private val documentId = "${resourceName}Document"

    @Before
    fun setUp() {
        startLogging(Log.VERBOSE)

        if (!AzureData.isConfigured) {
            val appContext = InstrumentationRegistry.getTargetContext()

            AzureData.configure(appContext, azureCosmosDbAccount, azureCosmosPrimaryKey, PermissionMode.All)
        }

        turnOnInternetConnection()
        purgeCache()
    }

    //region Tests

    @Test
    fun resourcesAreCreatedLocallyWhenTheNetworkIsNotReachable() {
        turnOffInternetConnection()

        var createResponse: Response<Database>? = null
        var listResponse: ListResponse<Database>? = null

        AzureData.createDatabase(databaseId) {
            createResponse = it
        }

        await().until { createResponse != null }

        assertTrue(createResponse!!.isSuccessful)
        assertTrue(createResponse!!.fromCache)

        AzureData.getDatabases {
            listResponse = it
        }

        await().until { listResponse != null }

        assertTrue(listResponse!!.isSuccessful)
        assertTrue(listResponse!!.fromCache)
        assertNotNull(listResponse!!.resource)
        assertTrue(listResponse!!.resource!!.items.any { it.id == databaseId })
    }

    @Test
    fun conflictIsReturnedWhenTheSameResourceIsCreatedTwiceOffline() {
        turnOffInternetConnection()

        var createResponse: Response<Database>? = null
        var conflictResponse: Response<Database>? = null

        AzureData.createDatabase(databaseId) {
            createResponse = it
        }

        await().until { createResponse != null }

        assertTrue(createResponse!!.isSuccessful)
        assertTrue(createResponse!!.fromCache)

        AzureData.createDatabase(databaseId) {
            conflictResponse = it
        }

        await().until { conflictResponse != null }

        assertTrue(conflictResponse!!.isErrored)
        assertTrue(conflictResponse!!.fromCache)
        assertNotNull(conflictResponse!!.response)
        assertEquals(conflictResponse!!.response!!.code(), 409)
    }

    @Test
    fun notFoundIsReturnedWhenTryingToReplaceANonExistingResourceWhileOffline() {
        turnOffInternetConnection()

        var replaceResponse: Response<CustomDocument>? = null

        AzureData.createDatabase(databaseId) {
            AzureData.createCollection(collectionId, databaseId) {
                AzureData.replaceDocument(CustomDocument(documentId), it.resource!!) {
                    replaceResponse = it
                }
            }
        }

        await().until { replaceResponse != null }

        assertTrue(replaceResponse!!.isErrored)
        assertTrue(replaceResponse!!.fromCache)
        assertNotNull(replaceResponse!!.response)
        assertEquals(replaceResponse!!.response!!.code(), 404)
    }

    @Test
    fun notFoundIsReturnedWhenTryingToDeleteANonExistingResource() {
        turnOffInternetConnection()

        var deleteResponse: DataResponse? = null

        AzureData.deleteDatabase(databaseId) {
            deleteResponse = it
        }

        await().until { deleteResponse != null }

        assertTrue(deleteResponse!!.isErrored)
        assertTrue(deleteResponse!!.fromCache)
        assertNotNull(deleteResponse!!.response)
        assertEquals(deleteResponse!!.response!!.code(), 404)
    }

    //endregion

    //region Private helpers

    private fun turnOffInternetConnection() {
        val client = MockOkHttpClient()
        client.hasNetworkError = true

        DocumentClient.client = client
    }

    private fun turnOnInternetConnection() {
        DocumentClient.client = OkHttpClient()
    }

    private fun purgeCache() {
        ResourceCache.shared.purge()
    }

    //endregion
}