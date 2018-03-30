package com.azure.data.integration

import android.support.test.runner.AndroidJUnit4
import com.azure.data.*
import com.azure.data.model.DocumentCollection
import com.azure.data.model.Permission
import com.azure.data.model.ResourceType
import com.azure.data.model.User
import com.azure.data.service.ResourceResponse
import com.azure.data.service.Response
import junit.framework.Assert.assertEquals
import org.awaitility.Awaitility.await
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

@RunWith(AndroidJUnit4::class)
class PermissionTests : ResourceTest<Permission>(ResourceType.Permission, true, true) {

    private val userId = "AndroidTest${ResourceType.User.name}"
    var user: User? = null

    @Before
    override fun setUp() {
        super.setUp()

        AzureData.deleteUser(userId, databaseId) { response ->
            println("Attempted to delete test user.  Result: ${response.isSuccessful}")

            user = ensureUser()
        }

        await().until {
            user != null
        }
    }

    @After
    override fun tearDown() {

        var deleteResponse: Response? = null

        AzureData.deleteUser(userId, databaseId) { response ->
            println("Attempted to delete test user.  Result: ${response.isSuccessful}")
            deleteResponse = response
        }

        await().until {
            deleteResponse != null
        }

        super.tearDown()
    }

    private fun ensureUser() : User {

        var userResponse: ResourceResponse<User>? = null

        AzureData.createUser(userId, databaseId) {
            userResponse = it
        }

        await().until {
            userResponse != null
        }

        assertResponseSuccess(userResponse)
        assertEquals(userId, userResponse?.resource?.id)

        return userResponse!!.resource!!
    }

    private fun createNewPermission(coll: DocumentCollection? = null) : Permission {

        if (coll == null) {
            AzureData.createPermission(resourceId, Permission.PermissionMode.Read, collection!!, userId, databaseId) {
                resourceResponse = it
            }
        }
        else {
            coll.createPermission(resourceId, Permission.PermissionMode.Read, user!!) {
                resourceResponse = it
            }
        }

        await().until {
            resourceResponse != null
        }

        assertResponseSuccess(resourceResponse)
        assertEquals(resourceId, resourceResponse?.resource?.id)

        val permission = resourceResponse!!.resource!!

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

        user?.createPermission(resourceId, Permission.PermissionMode.Read, collection!!) {
            resourceResponse = it
        }

        await().until {
            resourceResponse != null
        }

        assertResponseSuccess(resourceResponse)
        assertEquals(resourceId, resourceResponse?.resource?.id)
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

        assertResponseSuccess(resourceListResponse)
    }

    @Test
    fun listPermissionsForUser() {

        createNewPermission()

        user?.getPermissions() {
            resourceListResponse = it
        }

        await().until {
            resourceListResponse != null
        }

        assertResponseSuccess(resourceListResponse)
    }

    @Test
    fun getPermission() {

        createNewPermission()

        AzureData.getPermission(resourceId, userId, databaseId) {
            resourceResponse = it
        }

        await().until {
            resourceResponse != null
        }

        assertResponseSuccess(resourceResponse)
        assertEquals(resourceId, resourceResponse?.resource?.id)
    }

    @Test
    fun getPermissionForUser() {

        val permission = createNewPermission()

        user?.getPermission(permission.resourceId!!) {
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
    fun deletePermissionViaSelf() {

        val permission = createNewPermission()

        permission.delete {
            dataResponse = it
        }

        await().until {
            dataResponse != null
        }

        assertResponseSuccess(dataResponse)
    }

    @Test
    fun deletePermissionById() {

        createNewPermission()

        AzureData.deletePermission(resourceId, userId, databaseId) {
            dataResponse = it
        }

        await().until {
            dataResponse != null
        }

        assertResponseSuccess(dataResponse)
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

        assertResponseSuccess(dataResponse)
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

        assertResponseSuccess(dataResponse)
    }

    @Test
    fun deletePermissionFromUserById() {

        createNewPermission()

        user?.deletePermission(resourceId, databaseId) {
            dataResponse = it
        }

        await().until {
            dataResponse != null
        }

        assertResponseSuccess(dataResponse)
    }

    @Test
    fun deletePermissionFromUserByResourceId() {

        val permission = createNewPermission()

        user?.deletePermission(permission.resourceId!!) {
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
    fun replacePermissionDetailsById() {

        createNewPermission()

        collection?.replacePermission(resourceId, Permission.PermissionMode.All, userId, databaseId) {
            resourceResponse = it
        }

        await().until {
            resourceResponse != null
        }

        assertResponseSuccess(resourceResponse)
        assertEquals(resourceId, resourceResponse?.resource?.id)

        assertEquals(Permission.PermissionMode.All, resourceResponse?.resource?.permissionMode)
    }

    @Test
    fun replacePermissionForResourceById() {

        val permission = createNewPermission()
        permission.permissionMode = Permission.PermissionMode.All

        collection?.replacePermission(permission, userId, databaseId) {
            resourceResponse = it
        }

        await().until {
            resourceResponse != null
        }

        assertResponseSuccess(resourceResponse)
        assertEquals(resourceId, resourceResponse?.resource?.id)

        assertEquals(Permission.PermissionMode.All, resourceResponse?.resource?.permissionMode)
    }

    @Test
    fun replacePermissionDetailsForResource() {

        val permission = createNewPermission()

        collection?.replacePermission(resourceId, permission.resourceId!!, Permission.PermissionMode.All, user!!) {
            resourceResponse = it
        }

        await().until {
            resourceResponse != null
        }

        assertResponseSuccess(resourceResponse)
        assertEquals(resourceId, resourceResponse?.resource?.id)

        assertEquals(Permission.PermissionMode.All, resourceResponse?.resource?.permissionMode)
    }

    @Test
    fun replacePermissionForUser() {

        val permission = createNewPermission()
        permission.permissionMode = Permission.PermissionMode.All

        AzureData.replacePermission(permission, user!!) {
            resourceResponse = it
        }

        await().until {
            resourceResponse != null
        }

        assertResponseSuccess(resourceResponse)
        assertEquals(resourceId, resourceResponse?.resource?.id)

        assertEquals(Permission.PermissionMode.All, resourceResponse?.resource?.permissionMode)
    }

    @Test
    fun replacePermissionOnUser() {

        val permission = createNewPermission()
        permission.permissionMode = Permission.PermissionMode.All

        user?.replacePermission(permission) {
            resourceResponse = it
        }

        await().until {
            resourceResponse != null
        }

        assertResponseSuccess(resourceResponse)
        assertEquals(resourceId, resourceResponse?.resource?.id)

        assertEquals(Permission.PermissionMode.All, resourceResponse?.resource?.permissionMode)
    }

    @Test
    fun replacePermissionDetailsOnUserForResource() {

        val permission = createNewPermission()

        user?.replacePermission(permission.id, permission.resourceId!!, Permission.PermissionMode.All, collection!!) {
            resourceResponse = it
        }

        await().until {
            resourceResponse != null
        }

        assertResponseSuccess(resourceResponse)
        assertEquals(resourceId, resourceResponse?.resource?.id)

        assertEquals(Permission.PermissionMode.All, resourceResponse?.resource?.permissionMode)
    }

    @Test
    fun replacePermissionDetailsOnUserForResourceLink() {

        val permission = createNewPermission()

        user?.replacePermission(permission.id, permission.resourceId!!, Permission.PermissionMode.All, collection?.selfLink!!) {
            resourceResponse = it
        }

        await().until {
            resourceResponse != null
        }

        assertResponseSuccess(resourceResponse)
        assertEquals(resourceId, resourceResponse?.resource?.id)

        assertEquals(Permission.PermissionMode.All, resourceResponse?.resource?.permissionMode)
    }

    //endregion
}