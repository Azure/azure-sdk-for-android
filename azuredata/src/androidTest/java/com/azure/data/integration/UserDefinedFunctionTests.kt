package com.azure.data.integration

import android.support.test.runner.AndroidJUnit4
import com.azure.data.*
import com.azure.data.integration.common.ResourceTest
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
            coll.createUserDefinedFunction(createdResourceId, udfBody) {
                response = it
            }
        } else {
            AzureData.createUserDefinedFunction(createdResourceId, udfBody, collectionId, databaseId) {
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

        assertListResponseSuccess(resourceListResponse)
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

        assertListResponseSuccess(resourceListResponse)
        assertTrue(resourceListResponse?.resource?.count!! > 0)
    }

    //region Deletes

    @Test
    fun deleteUDFById() {

        createNewUDF()

        AzureData.deleteUserDefinedFunction(createdResourceId, collectionId, databaseId) {
            dataResponse = it
        }

        await().until {
            dataResponse != null
        }

        assertDataResponseSuccess(dataResponse)
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

        assertDataResponseSuccess(dataResponse)
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

        assertDataResponseSuccess(dataResponse)
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

        assertDataResponseSuccess(dataResponse)
    }

    @Test
    fun deleteUDFFromCollectionById() {

        val udf = createNewUDF()

        collection?.deleteUserDefinedFunction(udf.id) {
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
    fun replaceUDFById() {

        createNewUDF()

        AzureData.replaceUserDefinedFunction(createdResourceId, udfBodyNew, collectionId, databaseId) {
            response = it
        }

        await().until {
            response != null
        }

        assertResourceResponseSuccess(response)
        assertEquals(createdResourceId, response?.resource?.id)

        assertEquals(udfBodyNew, response?.resource?.body)
    }

    @Test
    fun replaceUDF() {

        val udf = createNewUDF()

        AzureData.replaceUserDefinedFunction(udf.id, udfBodyNew, collection!!) {
            response = it
        }

        await().until {
            response != null
        }

        assertResourceResponseSuccess(response)
        assertEquals(createdResourceId, response?.resource?.id)

        assertEquals(udfBodyNew, response?.resource?.body)
    }

    @Test
    fun replaceUDFInCollection() {

        var udf = createNewUDF()

        collection?.replaceUserDefinedFunction(udf.id, udfBodyNew) {
            response = it
        }

        await().until {
            response != null
        }

        udf = response?.resource!!

        assertResourceResponseSuccess(response)
        assertEquals(createdResourceId, udf.id)

        assertEquals(udfBodyNew, udf.body)

        resetResponse()

        udf.body = udfBody

        collection?.replaceUserDefinedFunction(udf) {
            response = it
        }

        await().until {
            response != null
        }

        udf = response?.resource!!

        assertResourceResponseSuccess(response)
        assertEquals(createdResourceId, udf.id)

        assertEquals(udfBody, udf.body)
    }

    //endregion
}