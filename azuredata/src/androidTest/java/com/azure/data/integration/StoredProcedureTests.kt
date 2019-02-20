package com.azure.data.integration

import android.support.test.runner.AndroidJUnit4
import com.azure.data.*
import com.azure.data.integration.common.ResourceTest
import com.azure.data.model.*
import junit.framework.Assert.assertEquals
import junit.framework.Assert.assertTrue
import org.awaitility.Awaitility.await
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

@RunWith(AndroidJUnit4::class)
class StoredProcedureTests : ResourceTest<StoredProcedure>(ResourceType.StoredProcedure, true, true) {

    init {
        partitionKeyPath = "/testKey"
    }

    private val storedProcedureBody = """
        function () {
            var context = getContext();
            var r = context.getResponse();

            r.setBody('Hello World!');
        }
        """

    private val storedProcNewBody = """function () {}"""

    private val storedProcResult = "\"Hello World!\""

    private fun createNewStoredProc(coll: DocumentCollection? = null) : StoredProcedure {

        if (coll != null) {
            coll.createStoredProcedure(createdResourceId, storedProcedureBody) {
                response = it
            }
        } else {
            AzureData.createStoredProcedure(createdResourceId, storedProcedureBody, collectionId, databaseId) {
                response = it
            }
        }

        await().until {
            response != null
        }

        assertResourceResponseSuccess(response)
        assertEquals(createdResourceId, response?.resource?.id)

        val resource = response!!.resource!!

        resetResponse()

        return resource
    }

    @Test
    fun createStoredProc() {

        createNewStoredProc()
    }

    @Test
    fun createStoredProcInCollection() {

        createNewStoredProc(collection)
    }

    @Test
    fun listStoredProcs() {

        createNewStoredProc()

        AzureData.getStoredProcedures(collectionId, databaseId) {
            resourceListResponse = it
        }

        await().until {
            resourceListResponse != null
        }

        assertListResponseSuccess(resourceListResponse)
        assertTrue(resourceListResponse?.resource?.count!! > 0)
    }

    @Test
    fun listStoredProcsForCollection() {

        createNewStoredProc()

        collection?.getStoredProcedures {
            resourceListResponse = it
        }

        await().until {
            resourceListResponse != null
        }

        assertListResponseSuccess(resourceListResponse)
        assertTrue(resourceListResponse?.resource?.count!! > 0)
    }

    //region Deletes

    @Test
    fun deleteStoredProcedureById() {

        createNewStoredProc()

        AzureData.deleteStoredProcedure(createdResourceId, collectionId, databaseId) {
            dataResponse = it
        }

        await().until {
            dataResponse != null
        }

        assertDataResponseSuccess(dataResponse)
    }

    @Test
    fun deleteStoredProcedure() {

        val sproc = createNewStoredProc()

        AzureData.deleteStoredProcedure(sproc, collectionId, databaseId) {
            dataResponse = it
        }

        await().until {
            dataResponse != null
        }

        assertDataResponseSuccess(dataResponse)
    }

    @Test
    fun deleteStoredProcedureInCollection() {

        val sproc = createNewStoredProc()

        AzureData.deleteStoredProcedure(sproc, collection!!) {
            dataResponse = it
        }

        await().until {
            dataResponse != null
        }

        assertDataResponseSuccess(dataResponse)
    }

    @Test
    fun deleteStoredProcedureFromCollection() {

        val sproc = createNewStoredProc()

        collection?.deleteStoredProcedure(sproc) {
            dataResponse = it
        }

        await().until {
            dataResponse != null
        }

        assertDataResponseSuccess(dataResponse)
    }

    @Test
    fun deleteStoredProcedureFromCollectionById() {

        val sproc = createNewStoredProc()

        collection?.deleteStoredProcedure(sproc.id) {
            dataResponse = it
        }

        await().until {
            dataResponse != null
        }

        assertDataResponseSuccess(dataResponse)
    }

    //endregion

    //region Replaces

    @Test
    fun replaceStoredProcedureById() {

        createNewStoredProc()

        AzureData.replaceStoredProcedure(createdResourceId, storedProcNewBody, collectionId, databaseId) {
            response = it
        }

        await().until {
            response != null
        }

        assertResourceResponseSuccess(response)
        assertEquals(createdResourceId, response?.resource?.id)

        assertEquals(storedProcNewBody, response?.resource?.body)
    }

    @Test
    fun replaceStoredProcedure() {

        val sProc = createNewStoredProc()

        AzureData.replaceStoredProcedure(sProc.id, storedProcNewBody, collection!!) {
            response = it
        }

        await().until {
            response != null
        }

        assertResourceResponseSuccess(response)
        assertEquals(createdResourceId, response?.resource?.id)

        assertEquals(storedProcNewBody, response?.resource?.body)
    }

    @Test
    fun replaceStoredProcedureInCollection() {

        var sProc = createNewStoredProc()

        collection?.replaceStoredProcedure(sProc.id, storedProcNewBody) {
            response = it
        }

        await().until {
            response != null
        }

        sProc = response?.resource!!

        assertResourceResponseSuccess(response)
        assertEquals(createdResourceId, sProc.id)

        assertEquals(storedProcNewBody, sProc.body)

        resetResponse()

        sProc.body = storedProcedureBody

        collection?.replaceStoredProcedure(sProc) {
            response = it
        }

        await().until {
            response != null
        }

        sProc = response?.resource!!

        assertResourceResponseSuccess(response)
        assertEquals(createdResourceId, sProc.id)

        assertEquals(storedProcedureBody, sProc.body)
    }

    //endregion

    //region Execute

    @Test
    fun executeStoredProcedure() {

        createNewStoredProc()

        AzureData.executeStoredProcedure(createdResourceId, null, collectionId, databaseId) {
            dataResponse = it
        }

        await().until {
            dataResponse != null
        }

        assertDataResponseSuccess(dataResponse)

        assertEquals(storedProcResult, dataResponse?.result?.resource.toString())
    }

    @Test
    fun executeStoredProcedureInCollection() {

        val sProc = createNewStoredProc()

        AzureData.executeStoredProcedure(sProc.id, null, collection!!) {
            dataResponse = it
        }

        await().forever().until {
            dataResponse != null
        }

        assertDataResponseSuccess(dataResponse)

        assertEquals(storedProcResult, dataResponse?.result?.resource.toString())
    }

    @Test
    fun executeStoredProcedureFromCollection() {

        val sProc = createNewStoredProc()

        collection?.executeStoredProcedure(sProc.id, null) {
            dataResponse = it
        }

        await().until {
            dataResponse != null
        }

        assertDataResponseSuccess(dataResponse)

        assertEquals(storedProcResult, dataResponse?.result?.resource.toString())
    }

    //endregion
}