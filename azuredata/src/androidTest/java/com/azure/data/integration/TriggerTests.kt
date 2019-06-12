package com.azure.data.integration

import android.support.test.runner.AndroidJUnit4
import com.azure.data.*
import com.azure.data.integration.common.ResourceTest
import com.azure.data.model.DocumentCollection
import com.azure.data.model.Trigger
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
class TriggerTests : ResourceTest<Trigger>("TriggerTests", true, true) {

    init {
        partitionKeyPath = "/testKey"
    }

    private val triggerBody = """
        function updateMetadata() {}
        """

    private val triggerBodyNew = """
        function updateMetadataNew() {}
        """

    private fun createNewTrigger(coll: DocumentCollection? = null) : Trigger {

        if (coll != null) {
            coll.createTrigger(createdResourceId, Trigger.TriggerOperation.All, Trigger.TriggerType.Post, triggerBody) {
                response = it
            }
        } else {
            AzureData.createTrigger(createdResourceId, Trigger.TriggerOperation.All, Trigger.TriggerType.Post, triggerBody, collectionId, databaseId) {
                response = it
            }
        }

        await().until {
            response != null
        }

        assertResourceResponseSuccess(response)
        assertEquals(createdResourceId, response?.resource?.id)

        val resource = response!!.resource!!

        assertEquals(triggerBody, resource.body)

        resetResponse()

        return resource
    }

    @Test
    fun createTrigger() {

        createNewTrigger()
    }

    @Test
    fun createTriggerInCollection() {

        createNewTrigger(collection)
    }

    @Test
    fun listTriggers() {

        createNewTrigger()

        AzureData.getTriggers(collectionId, databaseId) {
            resourceListResponse = it
        }

        await().until {
            resourceListResponse != null
        }

        assertListResponseSuccess(resourceListResponse)
        assertTrue(resourceListResponse?.resource?.count!! > 0)
    }

    @Test
    fun listTriggersForCollection() {

        createNewTrigger(collection)

        collection?.getTriggers {
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
    fun deleteTriggerById() {

        createNewTrigger()

        AzureData.deleteTrigger(createdResourceId, collectionId, databaseId) {
            dataResponse = it
        }

        await().until {
            dataResponse != null
        }

        assertDataResponseSuccess(dataResponse)
    }

    @Test
    fun deleteTrigger() {

        val trigger = createNewTrigger()

        AzureData.deleteTrigger(trigger, collectionId, databaseId) {
            dataResponse = it
        }

        await().until {
            dataResponse != null
        }

        assertDataResponseSuccess(dataResponse)
    }

    @Test
    fun deleteTriggerInCollection() {

        val trigger = createNewTrigger()

        AzureData.deleteTrigger(trigger, collection!!) {
            dataResponse = it
        }

        await().until {
            dataResponse != null
        }

        assertDataResponseSuccess(dataResponse)
    }

    @Test
    fun deleteTriggerFromCollection() {

        val trigger = createNewTrigger()

        collection?.deleteTrigger(trigger) {
            dataResponse = it
        }

        await().until {
            dataResponse != null
        }

        assertDataResponseSuccess(dataResponse)
    }

    @Test
    fun deleteTriggerFromCollectionById() {

        val trigger = createNewTrigger()

        collection?.deleteTrigger(trigger.id) {
            dataResponse = it
        }

        await().until {
            dataResponse != null
        }

        assertDataResponseSuccess(dataResponse)
    }

    @Test
    fun deleteTriggerViaSelf() {

        val trigger = createNewTrigger()

        trigger.delete {
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
    fun replaceTriggerById() {

        createNewTrigger()

        AzureData.replaceTrigger(createdResourceId, Trigger.TriggerOperation.All, Trigger.TriggerType.Post, triggerBodyNew, collectionId, databaseId) {
            response = it
        }

        await().until {
            response != null
        }

        assertResourceResponseSuccess(response)
        assertEquals(createdResourceId, response?.resource?.id)

        assertEquals(triggerBodyNew, response?.resource?.body)
    }

    @Test
    fun replaceTrigger() {

        val trigger = createNewTrigger()

        AzureData.replaceTrigger(trigger.id, Trigger.TriggerOperation.All, Trigger.TriggerType.Post, triggerBodyNew, collection!!) {
            response = it
        }

        await().until {
            response != null
        }

        assertResourceResponseSuccess(response)
        assertEquals(createdResourceId, response?.resource?.id)

        assertEquals(triggerBodyNew, response?.resource?.body)
    }

    @Test
    fun replaceTriggerInCollection() {

        var trigger = createNewTrigger()

        collection?.replaceTrigger(trigger.id, Trigger.TriggerOperation.All, Trigger.TriggerType.Post, triggerBodyNew) {
            response = it
        }

        await().until {
            response != null
        }

        trigger = response?.resource!!

        assertResourceResponseSuccess(response)
        assertEquals(createdResourceId, trigger.id)

        assertEquals(triggerBodyNew, trigger.body)

        resetResponse()

        trigger.body = triggerBody

        collection?.replaceTrigger(trigger) {
            response = it
        }

        await().until {
            response != null
        }

        trigger = response?.resource!!

        assertResourceResponseSuccess(response)
        assertEquals(createdResourceId, trigger.id)

        assertEquals(triggerBody, trigger.body)
    }

    //endregion
}