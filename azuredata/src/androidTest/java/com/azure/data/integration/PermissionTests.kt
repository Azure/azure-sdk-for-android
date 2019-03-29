package com.azure.data.integration

import android.support.test.runner.AndroidJUnit4
import com.azure.core.log.d
import com.azure.data.*
import com.azure.data.integration.common.ResourceTest
import com.azure.data.model.*
import com.azure.data.service.DataResponse
import com.azure.data.service.Response
import org.awaitility.Awaitility.await
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

@RunWith(AndroidJUnit4::class)
class PermissionTests : ResourceTest<Permission>("PermissionTests", true, true) {

    private val userId = "AndroidTest${ResourceType.User.name}"
    private var user: User? = null

    @Before
    override fun setUp() {
        super.setUp()

        AzureData.deleteUser(userId, databaseId) { response ->
            d{"Attempted to delete test user.  Result: ${response.isSuccessful}"}

            user = ensureUser()
        }

        await().until {
            user != null
        }
    }

    @After
    override fun tearDown() {

        var deleteResponse: DataResponse? = null

        AzureData.deleteUser(userId, databaseId) { response ->
            d{"Attempted to delete test user.  Result: ${response.isSuccessful}"}
            deleteResponse = response
        }

        await().until {
            deleteResponse != null
        }

        super.tearDown()
    }

    private fun ensureUser() : User {

        var userResponse: Response<User>? = null

        AzureData.createUser(userId, databaseId) {
            userResponse = it
        }

        await().until {
            userResponse != null
        }

        assertResourceResponseSuccess(userResponse)
        assertEquals(userId, userResponse?.resource?.id)

        return userResponse!!.resource!!
    }

    private fun createNewPermission(coll: DocumentCollection? = null) : Permission {

        if (coll == null) {
            AzureData.createPermission(createdResourceId, PermissionMode.Read, collection!!, userId, databaseId) {
                response = it
            }
        }
        else {
            coll.createPermission(createdResourceId, PermissionMode.Read, user!!) {
                response = it
            }
        }

        await().until {
            response != null
        }

        assertResourceResponseSuccess(response)
        assertEquals(createdResourceId, response?.resource?.id)

        val permission = response!!.resource!!

        resetResponse()

        return permission
    }

    //region Create

    @Test
    fun createPermission() {

        createNewPermission()
    }

    @Test
    fun createPermissionForCollection() {

        createNewPermission(collection)
    }

    @Test
    fun createPermissionForUser() {

        user?.createPermission(createdResourceId, PermissionMode.Read, collection!!) {
            response = it
        }

        await().until {
            response != null
        }

        assertResourceResponseSuccess(response)
        assertEquals(createdResourceId, response?.resource?.id)
    }

    //endregion

    @Test
    fun listPermissions() {

        createNewPermission()

        AzureData.getPermissions(userId, databaseId) {
            resourceListResponse = it
        }

        await().until {
            resourceListResponse != null
        }

        assertListResponseSuccess(resourceListResponse)
    }

    @Test
    fun listPermissionsForUser() {

        createNewPermission()

        user?.getPermissions {
            resourceListResponse = it
        }

        await().until {
            resourceListResponse != null
        }

        assertListResponseSuccess(resourceListResponse)
    }

    @Test
    fun getPermission() {

        createNewPermission()

        AzureData.getPermission(createdResourceId, userId, databaseId) {
            response = it
        }

        await().until {
            response != null
        }

        assertResourceResponseSuccess(response)
        assertEquals(createdResourceId, response?.resource?.id)
    }

    @Test
    fun getPermissionForUser() {

        val permission = createNewPermission()

        user?.getPermission(permission.id) {
            response = it
        }

        await().until {
            response != null
        }

        assertResourceResponseSuccess(response)
        assertEquals(createdResourceId, response?.resource?.id)
    }

    //region Deletes

    @Test
    fun deletePermissionViaSelf() {

        val permission = createNewPermission()

        permission.delete {
            dataResponse = it
        }

        await().until {
            dataResponse != null
        }

        assertDataResponseSuccess(dataResponse)
    }

    @Test
    fun deletePermissionById() {

        createNewPermission()

        AzureData.deletePermission(createdResourceId, userId, databaseId) {
            dataResponse = it
        }

        await().until {
            dataResponse != null
        }

        assertDataResponseSuccess(dataResponse)
    }

    @Test
    fun deletePermission() {

        val permission = createNewPermission()

        AzureData.deletePermission(permission, userId, databaseId) {
            dataResponse = it
        }

        await().until {
            dataResponse != null
        }

        assertDataResponseSuccess(dataResponse)
    }

    @Test
    fun deletePermissionFromUser() {

        val permission = createNewPermission()

        user?.deletePermission(permission) {
            dataResponse = it
        }

        await().until {
            dataResponse != null
        }

        assertDataResponseSuccess(dataResponse)
    }

    @Test
    fun deletePermissionFromUserById() {

        createNewPermission()

        user?.deletePermission(createdResourceId, databaseId) {
            dataResponse = it
        }

        await().until {
            dataResponse != null
        }

        assertDataResponseSuccess(dataResponse)
    }

    @Test
    fun deletePermissionFromUserByResourceId() {

        createNewPermission()

        user?.deletePermission(createdResourceId) {
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
    fun replacePermissionDetailsById() {

        createNewPermission()

        collection?.replacePermission(createdResourceId, PermissionMode.All, userId, databaseId) {
            response = it
        }

        await().until {
            response != null
        }

        assertResourceResponseSuccess(response)
        assertEquals(createdResourceId, response?.resource?.id)

        assertEquals(PermissionMode.All, response?.resource?.permissionMode)
    }

    @Test
    fun replacePermissionForResourceById() {

        val permission = createNewPermission()
        permission.permissionMode = PermissionMode.All

        collection?.replacePermission(permission, userId, databaseId) {
            response = it
        }

        await().until {
            response != null
        }

        assertResourceResponseSuccess(response)
        assertEquals(createdResourceId, response?.resource?.id)

        assertEquals(PermissionMode.All, response?.resource?.permissionMode)
    }

    @Test
    fun replacePermissionDetailsForResource() {

        val permission = createNewPermission()

        collection?.replacePermission(permission.id, PermissionMode.All, user!!) {
            response = it
        }

        await().until {
            response != null
        }

        assertResourceResponseSuccess(response)
        assertEquals(createdResourceId, response?.resource?.id)

        assertEquals(PermissionMode.All, response?.resource?.permissionMode)
    }

    @Test
    fun replacePermissionForUser() {

        val permission = createNewPermission()
        permission.permissionMode = PermissionMode.All

        AzureData.replacePermission(permission, user!!) {
            response = it
        }

        await().until {
            response != null
        }

        assertResourceResponseSuccess(response)
        assertEquals(createdResourceId, response?.resource?.id)

        assertEquals(PermissionMode.All, response?.resource?.permissionMode)
    }

    @Test
    fun replacePermissionOnUser() {

        val permission = createNewPermission()
        permission.permissionMode = PermissionMode.All

        user?.replacePermission(permission) {
            response = it
        }

        await().until {
            response != null
        }

        assertResourceResponseSuccess(response)
        assertEquals(createdResourceId, response?.resource?.id)

        assertEquals(PermissionMode.All, response?.resource?.permissionMode)
    }

    @Test
    fun replacePermissionDetailsOnUserForResource() {

        val permission = createNewPermission()

        user?.replacePermission(permission.id, PermissionMode.All, collection!!) {
            response = it
        }

        await().until {
            response != null
        }

        assertResourceResponseSuccess(response)
        assertEquals(createdResourceId, response?.resource?.id)

        assertEquals(PermissionMode.All, response?.resource?.permissionMode)
    }

    @Test
    fun replacePermissionDetailsOnUserForResourceLink() {

        val permission = createNewPermission()

        user?.replacePermission(permission.id, PermissionMode.All, collection?.selfLink!!) {
            response = it
        }

        await().until {
            response != null
        }

        assertResourceResponseSuccess(response)
        assertEquals(createdResourceId, response?.resource?.id)

        assertEquals(PermissionMode.All, response?.resource?.permissionMode)
    }

    //endregion
}