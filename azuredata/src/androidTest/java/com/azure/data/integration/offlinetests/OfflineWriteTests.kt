package com.azure.data.integration.offlinetests

import android.support.test.runner.AndroidJUnit4
import com.azure.core.http.HttpStatusCode
import com.azure.data.AzureData
import com.azure.data.integration.common.CustomDocument
import com.azure.data.integration.common.DocumentTest
import com.azure.data.integration.common.PartitionedCustomDocment
import com.azure.data.model.Database
import com.azure.data.model.service.DataResponse
import com.azure.data.model.service.ListResponse
import com.azure.data.model.service.Response
import org.junit.Test
import org.awaitility.Awaitility.*
import org.junit.runner.RunWith
import org.junit.Assert.*

@RunWith(AndroidJUnit4::class)
class OfflineWriteTests: DocumentTest<PartitionedCustomDocment>("OfflineWriteTests", PartitionedCustomDocment::class.java, false, false) {

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
        assertEquals(HttpStatusCode.Conflict.code, conflictResponse!!.response!!.code())
    }

    @Test
    fun notFoundIsReturnedWhenTryingToReplaceANonExistingResourceWhileOffline() {

        turnOffInternetConnection()

        var replaceResponse: Response<CustomDocument>? = null

        AzureData.createDatabase(databaseId) {
            AzureData.createCollection(collectionId, databaseId) {
                AzureData.replaceDocument(CustomDocument(documentId), it.resource!!) { replaceRsp ->
                    replaceResponse = replaceRsp
                }
            }
        }

        await().until { replaceResponse != null }

        assertTrue(replaceResponse!!.isErrored)
        assertTrue(replaceResponse!!.fromCache)
        assertNotNull(replaceResponse!!.response)
        assertEquals(HttpStatusCode.NotFound.code, replaceResponse!!.response!!.code())
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
        assertEquals(HttpStatusCode.NotFound.code, deleteResponse!!.response!!.code())
    }
}