package com.azure.data.service

import com.azure.data.model.*
import com.azure.data.util.ContextProvider

/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

interface PermissionProvider {

    var configuration: PermissionProviderConfiguration?

    fun getPermissionForCollection(collectionId: String, databaseId: String, permissionMode: PermissionMode, completion: (Response<Permission>) -> Unit)

    fun getPermissionForDocument(documentId: String, collectionId: String, databaseId: String, permissionMode: PermissionMode, completion: (Response<Permission>) -> Unit)

    fun getPermissionForAttachment(attachmentId: String, documentId: String, collectionId: String, databaseId: String, permissionMode: PermissionMode, completion: (Response<Permission>) -> Unit)

    fun getPermissionForStoredProcedure(storedProcedureId: String, collectionId: String, databaseId: String, permissionMode: PermissionMode, completion: (Response<Permission>) -> Unit)

    fun getPermissionForUserDefinedFunction(functionId: String, collectionId: String, databaseId: String, permissionMode: PermissionMode, completion: (Response<Permission>) -> Unit)

    fun getPermissionForTrigger(triggerId: String, collectionId: String, databaseId: String, permissionMode: PermissionMode, completion: (Response<Permission>) -> Unit)
}

inline fun PermissionProvider.getPermission(resourceLocation: ResourceLocation, permissionMode: PermissionMode, completion: (Response<Permission>) -> Unit) {

    if (!PermissionCache.shared.isRestored) {
        PermissionCache.shared.restore(ContextProvider.appContext)
    }

    if (configuration == null) {
        configuration = PermissionProviderConfiguration.default
    }

    val mode = if (configuration!!.defaultPermissionMode == PermissionMode.All) PermissionMode.All else permissionMode

    if (!resourceLocation.resourceType.supportsPermissionToken) {
        completion(Response(DataError(DocumentClientError.PermissionError)))
    }
}