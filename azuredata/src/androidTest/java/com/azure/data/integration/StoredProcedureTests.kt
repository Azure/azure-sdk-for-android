package com.azure.data.integration

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.azure.data.*
import com.azure.data.integration.common.PartitionedCustomDocment
import com.azure.data.integration.common.ResourceTest
import com.azure.data.model.*
import org.awaitility.Awaitility.await
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

@RunWith(AndroidJUnit4::class)
class StoredProcedureTests : ResourceTest<StoredProcedure>("StoredProcedureTests", true, true) {

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
    private val partitionKeyValue = "PartitionKeyValue"

    private fun createNewStoredProc(coll: DocumentCollection? = null, body: String = storedProcedureBody) : StoredProcedure {

        if (coll != null) {
            coll.createStoredProcedure(createdResourceId, body) {
                response = it
            }
        } else {
            AzureData.createStoredProcedure(createdResourceId, body, collectionId, databaseId) {
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

        await().until {
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

    @Test
    fun executeStoredProcedureWithQuery() {

        // this type of stored proc will require a partition key
        createTestDocuments()

        val body = """
            function(arg) {
                var collection = getContext().getCollection();

                var isAccepted = collection.queryDocuments(
                    collection.getSelfLink(),
                    `SELECT * FROM $collectionId`,

                    function (err, feed, options) {
                        if (err) throw err;

                        if (!feed || !feed.length) {
                            var response = getContext().getResponse();
                            response.setBody('no docs found');
                        }
                        else {
                            var response = getContext().getResponse();
                            var body = { feed: feed };
                            response.setBody(JSON.stringify(body));
                        }
                    });

                if (!isAccepted) throw new Error('The query was not accepted by the server.');
            }
        """.trimIndent()

        createNewStoredProc(collection, body)

        AzureData.executeStoredProcedure(createdResourceId, null, partitionKeyValue, collectionId, databaseId) {
            dataResponse = it
        }

        await().until {
            dataResponse != null
        }

        assertDataResponseSuccess(dataResponse)

        assertTrue(!dataResponse?.result?.resource.toString().isEmpty())
    }

    //endregion

    private fun createTestDocuments(count: Int = 3) : List<PartitionedCustomDocment> {

        val docs = mutableListOf<PartitionedCustomDocment>()

        for (i in 1..count) {

            val docToCreate = PartitionedCustomDocment(createdResourceId(i))
            docToCreate.testKey = partitionKeyValue

            AzureData.createDocument(docToCreate, collectionId, databaseId) {

                assertResourceResponseSuccess(it)
                assertEquals(createdResourceId(i), it.resource?.id)
                docs.add(it.resource!!)
            }
        }

        await().until {
            docs.count() == count
        }

        return docs
    }
}