package com.azure.data.service

import com.azure.data.model.*
import com.azure.core.util.ContextProvider
import com.azure.data.model.service.DataError
import com.azure.data.model.service.ResourceLocation
import com.azure.data.util.ResourceOracle
import com.azure.data.util.ancestorIds
import java.util.*

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

internal fun PermissionProvider.getPermission(resourceLocation: ResourceLocation, permissionMode: PermissionMode, completion: (Response<Permission>) -> Unit) {

    if (!PermissionCache.shared.isRestored) {
        PermissionCache.shared.restore(ContextProvider.appContext)
    }

    var location = resourceLocation

    if (configuration == null) {
        configuration = PermissionProviderConfiguration.default
    }

    val config = configuration!!

    val mode = if (configuration!!.defaultPermissionMode == PermissionMode.All) PermissionMode.All else permissionMode

    val resourceType = location.resourceType

    if (!resourceType.supportsPermissionToken) {
        completion(Response(DataError(PermissionProviderError.InvalidResourceType)))
    }

    config.defaultResourceType?.let { defaultResourceType ->

        if (resourceType != defaultResourceType && resourceType.isDecendentOf(defaultResourceType)) {

            val ancestorIds = resourceLocation.ancestorIds()

            location = when (defaultResourceType) {

                ResourceType.Collection -> ResourceLocation.Collection(ancestorIds[ResourceType.Database]!!, ancestorIds[ResourceType.Collection]!!)
                ResourceType.Document -> ResourceLocation.Document(ancestorIds[ResourceType.Database]!!, ancestorIds[ResourceType.Collection]!!, ancestorIds[ResourceType.Document]!!)
                else -> return completion(Response(DataError(PermissionProviderError.InvalidDefaultResourceType)))
            }
        }
    }

    // does a cached permission exist / is it valid?
    PermissionCache.shared.getPermission(location.link())?.let { permission ->

        if (permission.permissionMode == PermissionMode.All || permission.permissionMode == permissionMode) {

            permission.timestamp?.let { timestamp ->

                if (config.defaultTokenDuration - ((Date().time - timestamp.time) / 1000) > config.tokenRefreshThreshold) {

                    return completion(Response(permission))
                }
            }
        }
    }

    doGetPermission(location, mode) {

        it.resource?.let { permission ->

            if (PermissionCache.shared.setPermission(permission, location.link())) {
                completion(Response(permission))
            } else {
                completion(Response(DataError(PermissionProviderError.PermissionCacheFailed)))
            }
        } ?: completion(Response(DataError(PermissionProviderError.GetPermissionFailed)))
    }
}

private fun PermissionProvider.doGetPermission(location: ResourceLocation, permissionMode: PermissionMode, completion: (Response<Permission>) -> Unit) {

    return when (location) {

        is ResourceLocation.Collection -> location.id?.let { id ->
            this.getPermissionForCollection(id, location.databaseId, permissionMode, completion)
        } ?: completion(Response(DataError(PermissionProviderError.InvalidResourceType)))

        is ResourceLocation.StoredProcedure -> location.id?.let {
            this.getPermissionForStoredProcedure(it, location.collectionId, location.databaseId, permissionMode, completion)
        } ?: this.getPermissionForCollection(location.collectionId, location.databaseId, permissionMode, completion)

        is ResourceLocation.Trigger -> location.id?.let {
            this.getPermissionForTrigger(it, location.collectionId, location.databaseId, permissionMode, completion)
        } ?: this.getPermissionForCollection(location.collectionId, location.databaseId, permissionMode, completion)

        is ResourceLocation.Udf -> location.id?.let {
            this.getPermissionForUserDefinedFunction(it, location.collectionId, location.databaseId, permissionMode, completion)
        } ?: this.getPermissionForCollection(location.collectionId, location.databaseId, permissionMode, completion)

        is ResourceLocation.Document -> location.id?.let {
            this.getPermissionForDocument(it, location.collectionId, location.databaseId, permissionMode, completion)
        } ?: this.getPermissionForCollection(location.collectionId, location.databaseId, permissionMode, completion)

        is ResourceLocation.Attachment -> location.id?.let {
            this.getPermissionForAttachment(it, location.documentId, location.collectionId, location.databaseId, permissionMode, completion)
        } ?: this.getPermissionForDocument(location.documentId, location.collectionId, location.databaseId, permissionMode, completion)

        is ResourceLocation.Resource -> {

            when (location.resourceType) {

                ResourceType.Database,
                ResourceType.User,
                ResourceType.Permission,
                ResourceType.Offer -> completion(Response(DataError(PermissionProviderError.InvalidResourceType)))

                ResourceType.Collection -> location.resource.ancestorIds()[ResourceType.Database]?.let {
                    this.getPermissionForCollection(location.resource.id, it, permissionMode, completion)
                } ?: completion(Response(DataError(PermissionProviderError.GetPermissionFailed)))

                else -> {

                    val ancestorIds = location.resource.ancestorIds()

                    ancestorIds[ResourceType.Database]?.let { dbId ->
                        ancestorIds[ResourceType.Collection]?.let { collId ->

                            when (location.resourceType) {

                                ResourceType.StoredProcedure -> this.getPermissionForStoredProcedure(location.resource.id, collId, dbId, permissionMode, completion)

                                ResourceType.Trigger ->         this.getPermissionForTrigger(location.resource.id, collId, dbId, permissionMode, completion)

                                ResourceType.Udf ->             this.getPermissionForUserDefinedFunction(location.resource.id, collId, dbId, permissionMode, completion)

                                ResourceType.Document ->        this.getPermissionForDocument(location.resource.id, collId, dbId, permissionMode, completion)

                                ResourceType.Attachment -> {

                                    ancestorIds[ResourceType.Document]?.let { docId ->

                                        this.getPermissionForAttachment(location.resource.id, docId, collId, dbId, permissionMode, completion)
                                    }
                                }

                                else -> completion(Response(DataError(PermissionProviderError.GetPermissionFailed)))
                            }
                        }
                    } ?: completion(Response(DataError(PermissionProviderError.GetPermissionFailed)))
                }
            }
        }

        is ResourceLocation.Child -> {

            when (location.resourceType) {

                ResourceType.Database,
                ResourceType.User,
                ResourceType.Permission,
                ResourceType.Offer -> completion(Response(DataError(PermissionProviderError.InvalidResourceType)))

                ResourceType.Collection -> this.getPermissionForCollection(location.id!!, location.resource.id, permissionMode, completion)

                else -> {

                    val ancestorIds = location.resource.ancestorIds()

                    ancestorIds[ResourceType.Database]?.let { dbId ->

                        when (location.resourceType) {

                            ResourceType.StoredProcedure -> this.getPermissionForStoredProcedure(location.id!!, location.resource.id, dbId, permissionMode, completion)

                            ResourceType.Trigger -> this.getPermissionForTrigger(location.id!!, location.resource.id, dbId, permissionMode, completion)

                            ResourceType.Udf -> this.getPermissionForUserDefinedFunction(location.id!!, location.resource.id, dbId, permissionMode, completion)

                            ResourceType.Document -> this.getPermissionForDocument(location.id!!, location.resource.id, dbId, permissionMode, completion)

                            ResourceType.Attachment -> {

                                ancestorIds[ResourceType.Collection]?.let { collId ->

                                    location.id?.let { //do we have an attachment id?
                                        this.getPermissionForAttachment(it, location.resource.id, collId, dbId, permissionMode, completion)
                                    } ?: this.getPermissionForDocument(location.resource.id, collId, dbId, permissionMode, completion)

                                } ?: completion(Response(DataError(PermissionProviderError.GetPermissionFailed)))
                            }

                            else -> completion(Response(DataError(PermissionProviderError.GetPermissionFailed)))
                        }
                    } ?: completion(Response(DataError(PermissionProviderError.GetPermissionFailed)))
                }
            }
        }

        else -> completion(Response(DataError(PermissionProviderError.InvalidResourceType)))
    }
}

fun PermissionProvider.getSelfLink(altLink: String): String? {
    return ResourceOracle.shared.getSelfLink(altLink)
}