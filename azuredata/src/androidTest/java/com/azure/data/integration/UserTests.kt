package com.azure.data.integration

import android.support.test.runner.AndroidJUnit4
import com.azure.data.*
import com.azure.data.model.Database
import com.azure.data.model.ResourceType
import com.azure.data.model.User
import com.azure.data.service.ResourceResponse
import com.azure.data.service.Response
import junit.framework.Assert.assertEquals
import org.awaitility.Awaitility.await
import org.junit.After
import org.junit.Assert.assertNotEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

@RunWith(AndroidJUnit4::class)
class UserTests : ResourceTest<User>(ResourceType.User, true, false) {

    @Before
    override fun setUp() {
        super.setUp()

        deleteTestUser()
    }

    @After
    override fun tearDown() {

        deleteTestUser()

        super.tearDown()
    }

    private fun deleteTestUser(id: String = resourceId) {

        var deleteResponse: Response? = null

        AzureData.deleteUser(id, databaseId) { response ->
            println("Attempted to delete test user.  Result: ${response.isSuccessful}")
            deleteResponse = response
        }

        await().until {
            deleteResponse != null
        }
    }

    private fun createNewUser(db: Database? = null) : User {

        var userResponse: ResourceResponse<User>? = null

        if (db == null) {
            AzureData.createUser(resourceId, databaseId) {
                userResponse = it
            }
        }
        else {
            db.createUser(resourceId) {
                userResponse = it
            }
        }

        await().until {
            userResponse != null
        }

        assertResponseSuccess(userResponse)
        assertEquals(resourceId, userResponse?.resource?.id)

        return userResponse!!.resource!!
    }

    @Test
    fun createUser() {

        createNewUser()
    }

    @Test
    fun createUserInDatabase() {

        createNewUser(database)
    }

    @Test
    fun listUsers() {

        //make sure we have at least one user
        createNewUser()

        AzureData.getUsers(databaseId) {
            resourceListResponse = it
        }

        await().until {
            resourceListResponse != null
        }

        assertResponseSuccess(resourceListResponse)
    }

    @Test
    fun listUsersInDatabase() {

        //make sure we have at least one user
        createNewUser()

        database?.getUsers {
            resourceListResponse = it
        }

        await().until {
            resourceListResponse != null
        }

        assertResponseSuccess(resourceListResponse)
    }

    @Test
    fun getUser() {

        createNewUser()

        AzureData.getUser(resourceId, databaseId) {
            resourceResponse = it
        }

        await().until {
            resourceResponse != null
        }

        assertResponseSuccess(resourceResponse)
        assertEquals(resourceId, resourceResponse?.resource?.id)
    }

    @Test
    fun getUserInDatabase() {

        createNewUser()

        database?.getUser(resourceId) {
            resourceResponse = it
        }

        await().until {
            resourceResponse != null
        }

        assertResponseSuccess(resourceResponse)
        assertEquals(resourceId, resourceResponse?.resource?.id)
    }

    //region Deletes

    @Test
    fun deleteUserById() {

        createNewUser()

        AzureData.deleteUser(resourceId, databaseId) {
            dataResponse = it
        }

        await().until {
            dataResponse != null
        }

        assertResponseSuccess(dataResponse)
    }

    @Test
    fun deleteUserFromDatabaseById() {

        val user = createNewUser()

        AzureData.deleteUser(user, databaseId) {
            dataResponse = it
        }

        await().until {
            dataResponse != null
        }

        assertResponseSuccess(dataResponse)
    }

    @Test
    fun deleteUserFromDatabase() {

        val user = createNewUser()

        database?.deleteUser(user) {
            dataResponse = it
        }

        await().until {
            dataResponse != null
        }

        assertResponseSuccess(dataResponse)
    }

    @Test
    fun deleteUserInDatabaseById() {

        createNewUser()

        database?.deleteUser(resourceId) {
            dataResponse = it
        }

        await().until {
            dataResponse != null
        }

        assertResponseSuccess(dataResponse)
    }

    @Test
    fun deleteUser() {

        val user = createNewUser()

        user.delete {
            dataResponse = it
        }

        await().until {
            dataResponse != null
        }

        assertResponseSuccess(dataResponse)
    }

    //endregion

    @Test
    fun replaceUser() {

        val replaceUserId = "Updated_$resourceId"
        val user = createNewUser()

        AzureData.replaceUser(user.id, replaceUserId, databaseId) {
            resourceResponse = it
        }

        await().until {
            resourceResponse != null
        }

        assertResponseSuccess(resourceResponse)
        assertEquals(replaceUserId, resourceResponse?.resource?.id)
        assertNotEquals(resourceId, resourceResponse?.resource?.id)

        deleteTestUser(replaceUserId)
    }

    @Test
    fun replaceUserInDatabase() {

        val replaceUserId = "Updated_$resourceId"
        val user = createNewUser()

        database?.replaceUser(user.id, replaceUserId) {
            resourceResponse = it
        }

        await().until {
            resourceResponse != null
        }

        assertResponseSuccess(resourceResponse)
        assertEquals(replaceUserId, resourceResponse?.resource?.id)
        assertNotEquals(resourceId, resourceResponse?.resource?.id)

        deleteTestUser(replaceUserId)
    }

    @Test
    fun refreshUser() {

        val user = createNewUser()

        user.refresh {
            resourceResponse = it
        }

        await().until {
            resourceResponse != null
        }

        assertResponseSuccess(resourceResponse)
        assertEquals(resourceId, resourceResponse?.resource?.id)
    }
}