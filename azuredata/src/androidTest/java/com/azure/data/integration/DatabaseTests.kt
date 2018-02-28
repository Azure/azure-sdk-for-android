package com.azure.data.integration

import android.support.test.runner.AndroidJUnit4
import com.azure.data.AzureData
import com.azure.data.delete
import com.azure.data.model.Database
import com.azure.data.model.ResourceType
import com.azure.data.refresh
import org.junit.Test
import org.junit.runner.RunWith
import org.awaitility.Awaitility.*
import org.junit.Assert.*

/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

@RunWith(AndroidJUnit4::class)
class DatabaseTests : ResourceTest<Database>(ResourceType.Database, false, false) {

    @Test
    fun createDatabase() {

        ensureDatabase()
    }

    @Test
    fun listDatabases() {

        ensureDatabase()

        AzureData.getDatabases {
            resourceListResponse = it
        }

        await().until {
            resourceListResponse != null
        }

        assertResponseSuccess(resourceListResponse)
        assertTrue(resourceListResponse?.resource?.count!! > 0)
    }

    @Test
    fun getDatabase() {

        ensureDatabase()

        AzureData.getDatabase(databaseId) {
            resourceResponse = it
        }

        await().until {
            resourceResponse != null
        }

        assertResponseSuccess(resourceResponse)
        assertEquals(databaseId, resourceResponse?.resource?.id)
    }

    @Test
    fun refreshDatabase() {

        val db = ensureDatabase()

        db.refresh {
            resourceResponse = it
        }

        await().until {
            resourceResponse != null
        }

        assertResponseSuccess(resourceResponse)
        assertEquals(databaseId, resourceResponse?.resource?.id)
    }

    @Test
    fun deleteDatabaseById() {

        ensureDatabase()

        AzureData.deleteDatabase(databaseId) {
            dataResponse = it
        }

        await().until {
            dataResponse != null
        }

        assertResponseSuccess(dataResponse)
    }

    @Test
    fun deleteDatabase() {

        val db = ensureDatabase()

        AzureData.deleteDatabase(db) {
            dataResponse = it
        }

        await().until {
            dataResponse != null
        }

        assertResponseSuccess(dataResponse)
    }

    @Test
    fun deleteDatabaseWithExtension() {

        val db = ensureDatabase()

        db.delete {
            dataResponse = it
        }

        await().until {
            dataResponse != null
        }

        assertResponseSuccess(dataResponse)
    }
}