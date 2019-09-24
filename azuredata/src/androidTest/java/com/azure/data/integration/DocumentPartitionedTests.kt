package com.azure.data.integration

import android.support.test.runner.AndroidJUnit4
import com.azure.core.http.HttpStatusCode
import com.azure.data.AzureData
import com.azure.data.constants.MSHttpHeader
import com.azure.data.createTrigger
import com.azure.data.integration.common.PartitionedCustomDocment
import com.azure.data.model.Trigger
import com.azure.data.model.service.Response
import org.awaitility.Awaitility.await
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

@RunWith(AndroidJUnit4::class)
class DocumentPartitionedTests : DocumentTestsBase<PartitionedCustomDocment>("DocumentPartitionedTests", PartitionedCustomDocment::class.java) {

    init {
        partitionKeyPath = "/testKey"
    }

    @Test
    fun deleteDocumentByIdAndPartitionKey() {

        val doc = createNewDocument()

        AzureData.deleteDocument(doc.id, doc.testKey, collectionId, databaseId) {
            dataResponse = it
        }

        await().until {
            dataResponse != null
        }

        assertDataResponseSuccess(dataResponse)
    }

    @Test
    fun deleteDocumentFromCollectionById() {

        val doc = createNewDocument()

        AzureData.deleteDocument(doc.id, doc.testKey, collection!!) {
            dataResponse = it
        }

        await().until {
            dataResponse != null
        }

        assertDataResponseSuccess(dataResponse)
    }

    //region Trigger support

    @Test
    fun testTriggersArePassedInHeaders() {

        var docResponse: Response<PartitionedCustomDocment>? = null
        val doc = newDocument()

        AzureData.createDocument(doc, collectionId, databaseId, "myPreTrigger", "myPostTrigger") {
            docResponse = it
        }

        await().until { docResponse != null }

        // now inspect the headers to look for triggers
        val preTriggers = docResponse?.request?.headers?.get(MSHttpHeader.MSDocumentDBPreTriggerInclude.value)
        val postTriggers = docResponse?.request?.headers?.get(MSHttpHeader.MSDocumentDBPostTriggerInclude.value)

        assertEquals("myPreTrigger", preTriggers)
        assertEquals("myPostTrigger", postTriggers)
    }

    @Test
    fun testPassingNonExistentTriggerFails() {

        var docResponse: Response<PartitionedCustomDocment>? = null
        val doc = newDocument()

        AzureData.createDocument(doc, collectionId, databaseId, "myTriggerThatDoesntExist", "myOtherTriggerThatDoesntExist") {
            docResponse = it
        }

        await().until { docResponse != null }

        assertErrorResponse(docResponse)

        assertEquals(HttpStatusCode.BadRequest.toString(), docResponse?.error?.code)
    }

    @Test
    fun testPassingExistingTriggerSucceeds() {

        var docResponse: Response<PartitionedCustomDocment>? = null
        var triggerResponse: Response<Trigger>? = null
        val doc = newDocument()
        val triggerBody = """
            function updateMetadata() {}
            """

        collection?.createTrigger("updateMetadataPre", Trigger.Operation.Create, Trigger.Type.Pre, triggerBody) {
            triggerResponse = it
        }

        await().until { triggerResponse != null }
        triggerResponse = null

        collection?.createTrigger("updateMetadataPost", Trigger.Operation.Create, Trigger.Type.Post, triggerBody) {
            triggerResponse = it
        }

        await().until { triggerResponse != null }

        AzureData.createDocument(doc, collectionId, databaseId, "updateMetadataPre", "updateMetadataPost") {
            docResponse = it
        }

        await().until { docResponse != null }

        assertResourceResponseSuccess(docResponse)
    }

    //endregion
}