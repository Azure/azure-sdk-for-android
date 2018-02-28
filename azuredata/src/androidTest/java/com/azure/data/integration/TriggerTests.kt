package com.azure.data.integration

import android.support.test.runner.AndroidJUnit4
import com.azure.data.*
import com.azure.data.model.DocumentCollection
import com.azure.data.model.ResourceType
import com.azure.data.model.Trigger
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
class TriggerTests : ResourceTest<Trigger>(ResourceType.Trigger, true, true) {

    private val triggerBody = """
        function updateMetadata() {}
        """

    private val triggerBodyNew = """
        function updateMetadataNew() {}
        """

    private fun createNewTrigger(coll: DocumentCollection? = null) : Trigger {

        if (coll != null) {
            coll.createTrigger(resourceId, Trigger.TriggerOperation.All, Trigger.TriggerType.Post, triggerBody) {
                resourceResponse = it
            }
        } else {
            AzureData.createTrigger(resourceId, Trigger.TriggerOperation.All, Trigger.TriggerType.Post, triggerBody, collectionId, databaseId) {
                resourceResponse = it
            }
        }

        await().until {
            resourceResponse != null
        }

        assertResponseSuccess(resourceResponse)
        assertEquals(resourceId, resourceResponse?.resource?.id)

        val resource = resourceResponse!!.resource!!

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

        assertResponseSuccess(resourceListResponse)
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

        assertResponseSuccess(resourceListResponse)
        assertTrue(resourceListResponse?.resource?.count!! > 0)
    }

    //region Deletes

    @Test
    fun deleteTriggerById() {

        createNewTrigger()

        AzureData.deleteTrigger(resourceId, collectionId, databaseId) {
            dataResponse = it
        }

        await().until {
            dataResponse != null
        }

        assertResponseSuccess(dataResponse)
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

        assertResponseSuccess(dataResponse)
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

        assertResponseSuccess(dataResponse)
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

        assertResponseSuccess(dataResponse)
    }

    @Test
    fun deleteTriggerFromCollectionByRId() {

        val trigger = createNewTrigger()

        collection?.deleteTrigger(trigger.resourceId!!) {
            dataResponse = it
        }

        await().until {
            dataResponse != null
        }

        assertResponseSuccess(dataResponse)
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

        assertResponseSuccess(dataResponse)
    }

    //endregion

    //region Replaces

    @Test
    fun replaceTriggerById() {

        createNewTrigger()

        AzureData.replaceTrigger(resourceId, Trigger.TriggerOperation.All, Trigger.TriggerType.Post, triggerBodyNew, collectionId, databaseId) {
            resourceResponse = it
        }

        await().until {
            resourceResponse != null
        }

        assertResponseSuccess(resourceResponse)
        assertEquals(resourceId, resourceResponse?.resource?.id)

        assertEquals(triggerBodyNew, resourceResponse?.resource?.body)
    }

    @Test
    fun replaceTrigger() {

        val trigger = createNewTrigger()

        AzureData.replaceTrigger(trigger.id, trigger.resourceId!!, Trigger.TriggerOperation.All, Trigger.TriggerType.Post, triggerBodyNew, collection!!) {
            resourceResponse = it
        }

        await().until {
            resourceResponse != null
        }

        assertResponseSuccess(resourceResponse)
        assertEquals(resourceId, resourceResponse?.resource?.id)

        assertEquals(triggerBodyNew, resourceResponse?.resource?.body)
    }

    @Test
    fun replaceTriggerInCollection() {

        var trigger = createNewTrigger()

        collection?.replaceTrigger(resourceId, trigger.resourceId!!, Trigger.TriggerOperation.All, Trigger.TriggerType.Post, triggerBodyNew) {
            resourceResponse = it
        }

        await().until {
            resourceResponse != null
        }

        trigger = resourceResponse?.resource!!

        assertResponseSuccess(resourceResponse)
        assertEquals(resourceId, trigger.id)

        assertEquals(triggerBodyNew, trigger.body)

        resetResponse()

        trigger.body = triggerBody

        collection?.replaceTrigger(trigger) {
            resourceResponse = it
        }

        await().until {
            resourceResponse != null
        }

        trigger = resourceResponse?.resource!!

        assertResponseSuccess(resourceResponse)
        assertEquals(resourceId, trigger.id)

        assertEquals(triggerBody, trigger.body)
    }

    //endregion
}