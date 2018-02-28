package com.azure.data.integration

import android.support.test.runner.AndroidJUnit4
import com.azure.data.*
import com.azure.data.model.DocumentCollection
import com.azure.data.model.ResourceType
import com.azure.data.model.UserDefinedFunction
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
class UserDefinedFunctionTests : ResourceTest<UserDefinedFunction>(ResourceType.Udf, true, true) {

    private val udfBody = """
        function (input) { return input.toLowerCase(); }
        """

    private val udfBodyNew = """
        function (input) { return input.toUpperCase(); }
        """

    private fun createNewUDF(coll: DocumentCollection? = null) : UserDefinedFunction {

        if (coll != null) {
            coll.createUserDefinedFunction(resourceId, udfBody) {
                resourceResponse = it
            }
        } else {
            AzureData.createUserDefinedFunction(resourceId, udfBody, collectionId, databaseId) {
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
    fun createUDF() {

        createNewUDF()
    }

    @Test
    fun createUDFInCollection() {

        createNewUDF(collection)
    }

    @Test
    fun listUDFs() {

        createNewUDF()

        AzureData.getUserDefinedFunctions(collectionId, databaseId) {
            resourceListResponse = it
        }

        await().until {
            resourceListResponse != null
        }

        assertResponseSuccess(resourceListResponse)
        assertTrue(resourceListResponse?.resource?.count!! > 0)
    }

    @Test
    fun listUDFsForCollection() {

        createNewUDF(collection)

        collection?.getUserDefinedFunctions {
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
    fun deleteUDFById() {

        createNewUDF()

        AzureData.deleteUserDefinedFunction(resourceId, collectionId, databaseId) {
            dataResponse = it
        }

        await().until {
            dataResponse != null
        }

        assertResponseSuccess(dataResponse)
    }

    @Test
    fun deleteUDF() {

        val udf = createNewUDF()

        AzureData.deleteUserDefinedFunction(udf, collectionId, databaseId) {
            dataResponse = it
        }

        await().until {
            dataResponse != null
        }

        assertResponseSuccess(dataResponse)
    }

    @Test
    fun deleteUDFInCollection() {

        val udf = createNewUDF()

        AzureData.deleteUserDefinedFunction(udf, collection!!) {
            dataResponse = it
        }

        await().until {
            dataResponse != null
        }

        assertResponseSuccess(dataResponse)
    }

    @Test
    fun deleteUDFFromCollection() {

        val udf = createNewUDF()

        collection?.deleteUserDefinedFunction(udf) {
            dataResponse = it
        }

        await().until {
            dataResponse != null
        }

        assertResponseSuccess(dataResponse)
    }

    @Test
    fun deleteUDFFromCollectionByRId() {

        val udf = createNewUDF()

        collection?.deleteUserDefinedFunction(udf.resourceId!!) {
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
    fun replaceUDFById() {

        createNewUDF()

        AzureData.replaceUserDefinedFunction(resourceId, udfBodyNew, collectionId, databaseId) {
            resourceResponse = it
        }

        await().until {
            resourceResponse != null
        }

        assertResponseSuccess(resourceResponse)
        assertEquals(resourceId, resourceResponse?.resource?.id)

        assertEquals(udfBodyNew, resourceResponse?.resource?.body)
    }

    @Test
    fun replaceUDF() {

        val udf = createNewUDF()

        AzureData.replaceUserDefinedFunction(udf.id, udf.resourceId!!, udfBodyNew, collection!!) {
            resourceResponse = it
        }

        await().until {
            resourceResponse != null
        }

        assertResponseSuccess(resourceResponse)
        assertEquals(resourceId, resourceResponse?.resource?.id)

        assertEquals(udfBodyNew, resourceResponse?.resource?.body)
    }

    @Test
    fun replaceUDFInCollection() {

        var udf = createNewUDF()

        collection?.replaceUserDefinedFunction(resourceId, udf.resourceId!!, udfBodyNew) {
            resourceResponse = it
        }

        await().until {
            resourceResponse != null
        }

        udf = resourceResponse?.resource!!

        assertResponseSuccess(resourceResponse)
        assertEquals(resourceId, udf.id)

        assertEquals(udfBodyNew, udf.body)

        resetResponse()

        udf.body = udfBody

        collection?.replaceUserDefinedFunction(udf) {
            resourceResponse = it
        }

        await().until {
            resourceResponse != null
        }

        udf = resourceResponse?.resource!!

        assertResponseSuccess(resourceResponse)
        assertEquals(resourceId, udf.id)

        assertEquals(udfBody, udf.body)
    }

    //endregion
}