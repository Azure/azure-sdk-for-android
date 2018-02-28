package com.azure.data.integration

import android.support.test.runner.AndroidJUnit4
import com.azure.data.*
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
            coll.createStoredProcedure(resourceId, storedProcedureBody) {
                resourceResponse = it
            }
        } else {
            AzureData.createStoredProcedure(resourceId, storedProcedureBody, collectionId, databaseId) {
                resourceResponse = it
            }
        }

        await().until {
            resourceResponse != null
        }

        assertResponseSuccess(resourceResponse)
        assertEquals(resourceId, resourceResponse?.resource?.id)

        val resource = resourceResponse!!.resource!!

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

        assertResponseSuccess(resourceListResponse)
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

        assertResponseSuccess(resourceListResponse)
        assertTrue(resourceListResponse?.resource?.count!! > 0)
    }

    //region Deletes

    @Test
    fun deleteStoredProcedureById() {

        createNewStoredProc()

        AzureData.deleteStoredProcedure(resourceId, collectionId, databaseId) {
            dataResponse = it
        }

        await().until {
            dataResponse != null
        }

        assertResponseSuccess(dataResponse)
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

        assertResponseSuccess(dataResponse)
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

        assertResponseSuccess(dataResponse)
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

        assertResponseSuccess(dataResponse)
    }

    @Test
    fun deleteStoredProcedureFromCollectionByRId() {

        val sproc = createNewStoredProc()

        collection?.deleteStoredProcedure(sproc.resourceId!!) {
            dataResponse = it
        }

        await().until {
            dataResponse != null
        }

        assertResponseSuccess(dataResponse)
    }

    //endregion

    //region Replaces

    @Test
    fun replaceStoredProcedureById() {

        createNewStoredProc()

        AzureData.replaceStoredProcedure(resourceId, storedProcNewBody, collectionId, databaseId) {
            resourceResponse = it
        }

        await().until {
            resourceResponse != null
        }

        assertResponseSuccess(resourceResponse)
        assertEquals(resourceId, resourceResponse?.resource?.id)

        assertEquals(storedProcNewBody, resourceResponse?.resource?.body)
    }

    @Test
    fun replaceStoredProcedure() {

        val sProc = createNewStoredProc()

        AzureData.replaceStoredProcedure(resourceId, sProc.resourceId!!, storedProcNewBody, collection!!) {
            resourceResponse = it
        }

        await().until {
            resourceResponse != null
        }

        assertResponseSuccess(resourceResponse)
        assertEquals(resourceId, resourceResponse?.resource?.id)

        assertEquals(storedProcNewBody, resourceResponse?.resource?.body)
    }

    @Test
    fun replaceStoredProcedureInCollection() {

        var sProc = createNewStoredProc()

        collection?.replaceStoredProcedure(resourceId, sProc.resourceId!!, storedProcNewBody) {
            resourceResponse = it
        }

        await().until {
            resourceResponse != null
        }

        sProc = resourceResponse?.resource!!

        assertResponseSuccess(resourceResponse)
        assertEquals(resourceId, sProc.id)

        assertEquals(storedProcNewBody, sProc.body)

        resetResponse()

        sProc.body = storedProcedureBody

        collection?.replaceStoredProcedure(sProc) {
            resourceResponse = it
        }

        await().until {
            resourceResponse != null
        }

        sProc = resourceResponse?.resource!!

        assertResponseSuccess(resourceResponse)
        assertEquals(resourceId, sProc.id)

        assertEquals(storedProcedureBody, sProc.body)
    }

    //endregion

    //region Execute

    @Test
    fun executeStoredProcedure() {

        createNewStoredProc()

        AzureData.executeStoredProcedure(resourceId, null, collectionId, databaseId) {
            dataResponse = it
        }

        await().until {
            dataResponse != null
        }

        assertResponseSuccess(dataResponse)

        assertEquals(storedProcResult, dataResponse?.result?.resource.toString())
    }

    @Test
    fun executeStoredProcedureInCollection() {

        val sProc = createNewStoredProc()

        AzureData.executeStoredProcedure(sProc.resourceId!!, null, collection!!) {
            dataResponse = it
        }

        await().until {
            dataResponse != null
        }

        assertResponseSuccess(dataResponse)

        assertEquals(storedProcResult, dataResponse?.result?.resource.toString())
    }

    @Test
    fun executeStoredProcedureFromCollection() {

        val sProc = createNewStoredProc()

        collection?.executeStoredProcedure(sProc.resourceId!!, null) {
            dataResponse = it
        }

        await().until {
            dataResponse != null
        }

        assertResponseSuccess(dataResponse)

        assertEquals(storedProcResult, dataResponse?.result?.resource.toString())
    }

    //endregion
}