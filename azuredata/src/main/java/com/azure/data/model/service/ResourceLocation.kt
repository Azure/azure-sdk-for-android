package com.azure.data.model.service

import com.azure.data.util.ResourceOracle
import com.azure.data.util.ancestorIds
import com.azure.data.util.lastPathComponent
import com.azure.data.util.lastPathComponentRemoved

/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

/**
 *  The logical location of 1) a resource or feed and the and 2) the resource for which permissions are required

- Remark:
`path` refers to the logical location of the resource or feed of a corresponding CRUD operation
`link` refers to the logical location of the the resource the operation is acting on (thus need permissions for)

- Example: Listing all documents in a collection:
`let location: ResourceLocation = .document(databaseId: "MyDatabase", collectionId: "MyCollection", id: nil)`
`location.path // "dbs/MyDatabase/colls/MyCollection/docs" (the location of the documents feed)`
`location.link // "dbs/MyDatabase/colls/MyCollection" (the location of the collection itself)`

- Example: Get a single existing document from the collection:
`let location: ResourceLocation = .document(databaseId: "MyDatabase", collectionId: "MyCollection", id: "MyDocument")`
`location.path // "dbs/MyDatabase/colls/MyCollection/docs/MyDocument" (the location of the document)`
`location.link // "dbs/MyDatabase/colls/MyCollection/docs/MyDocument" (the location of the document)`
 */
sealed class ResourceLocation(val resourceType: ResourceType, val id: String? = null) {

    class Database(id: String? = null) : ResourceLocation(ResourceType.Database, id)
    class User(val databaseId: String, id: String? = null) : ResourceLocation(ResourceType.User, id)
    class Permission(val databaseId: String, val userId: String, id: String? = null) : ResourceLocation(ResourceType.Permission, id)
    class Collection(val databaseId: String, id: String? = null) : ResourceLocation(ResourceType.Collection, id)
    class StoredProcedure(val databaseId: String, val collectionId: String, id: String? = null) : ResourceLocation(ResourceType.StoredProcedure, id)
    class Trigger(val databaseId: String, val collectionId: String, id: String? = null) : ResourceLocation(ResourceType.Trigger, id)
    class Udf(val databaseId: String, val collectionId: String, id: String? = null) : ResourceLocation(ResourceType.Udf, id)
    class Document(val databaseId: String, val collectionId: String, id: String? = null) : ResourceLocation(ResourceType.Document, id)
    class Attachment(val databaseId: String, val collectionId: String, val documentId: String, id: String? = null) : ResourceLocation(ResourceType.Attachment, id)
    class Offer(id: String? = null) : ResourceLocation(ResourceType.Offer, id)
    class Resource(val resource: com.azure.data.model.Resource) : ResourceLocation(ResourceType.fromType(resource::class.java), resource.id)
    class Child(resourceType: ResourceType, val resource: com.azure.data.model.Resource, id: String? = null) : ResourceLocation(resourceType, id)
    class PkRanges(val databaseId: String, collectionId: String? = null) : ResourceLocation(ResourceType.PkRanges, collectionId)

    fun path() : String = when (this) {

        is Database ->          "dbs${id.path()}"
        is User ->              "dbs/$databaseId/users${id.path()}"
        is Permission ->        "dbs/$databaseId/users/$userId/permissions${id.path()}"
        is Collection ->        "dbs/$databaseId/colls${id.path()}"
        is StoredProcedure ->   "dbs/$databaseId/colls/$collectionId/sprocs${id.path()}"
        is Trigger ->           "dbs/$databaseId/colls/$collectionId/triggers${id.path()}"
        is Udf ->               "dbs/$databaseId/colls/$collectionId/udfs${id.path()}"
        is Document ->          "dbs/$databaseId/colls/$collectionId/docs${id.path()}"
        is Attachment ->        "dbs/$databaseId/colls/$collectionId/docs/$documentId/attachments${id.path()}"
        is Offer ->             "offers${id.path()}"
        is Resource ->          ResourceOracle.shared.getAltLink(resource)!!
        is Child ->             ResourceOracle.shared.getAltLink(resource) + "/${resourceType.path}${id.path()}"
        is PkRanges ->          "dbs/$databaseId/colls${id.path()}/pkranges"
    }

    fun link() : String = when (this) {

        is Database ->          id.pathIn("dbs")
        is User ->              "dbs/$databaseId${id.pathIn("/users")}"
        is Permission ->        "dbs/$databaseId/users/$userId${id.pathIn("/permissions")}"
        is Collection ->        "dbs/$databaseId${id.pathIn("/colls")}"
        is StoredProcedure ->   "dbs/$databaseId/colls/$collectionId${id.pathIn("/sprocs")}"
        is Trigger ->           "dbs/$databaseId/colls/$collectionId${id.pathIn("/triggers")}"
        is Udf ->               "dbs/$databaseId/colls/$collectionId${id.pathIn("/udfs")}"
        is Document ->          "dbs/$databaseId/colls/$collectionId${id.pathIn("/docs")}"
        is Attachment ->        "dbs/$databaseId/colls/$collectionId/docs/$documentId${id.pathIn("/attachments")}"
        is Offer ->             id?.toLowerCase() ?: ""
        is Resource ->          ResourceOracle.shared.getAltLink(resource)!!
        is Child ->             ResourceOracle.shared.getAltLink(resource) + id.pathIn("/${resourceType.path}")
        is PkRanges ->          "dbs/$databaseId${id.pathIn("/colls")}"
    }

    fun type() : String = resourceType.path

    fun id() : String? = when(this) {

        is Resource -> resource.id
        else -> id
    }

    fun ancestorIds() : Map<ResourceType, String> = when (this) {

        is User ->              mapOf(ResourceType.Database to databaseId)
        is Permission ->        mapOf(ResourceType.Database to databaseId, ResourceType.User to userId)
        is Collection ->        mapOf(ResourceType.Database to databaseId)
        is StoredProcedure ->   mapOf(ResourceType.Database to databaseId, ResourceType.Collection to collectionId)
        is Trigger ->           mapOf(ResourceType.Database to databaseId, ResourceType.Collection to collectionId)
        is Udf ->               mapOf(ResourceType.Database to databaseId, ResourceType.Collection to collectionId)
        is Document ->          mapOf(ResourceType.Database to databaseId, ResourceType.Collection to collectionId)
        is Attachment ->        mapOf(ResourceType.Database to databaseId, ResourceType.Collection to collectionId, ResourceType.Document to documentId)
        is Resource ->          resource.ancestorIds()
        is Child ->             resource.ancestorIds(true)
        else ->                 mapOf()
    }

    fun altLink(id: String): String = if (path().lastPathComponent().equals(id, ignoreCase = true)) path() else "${path()}/$id"

    fun selfLink(resourceId: String): String? {

        directory()?.let {
            return "$it/$resourceId"
        }

        return null
    }

    val supportsPermissionToken: Boolean
        get() = resourceType.supportsPermissionToken


    val isFeed: Boolean
        get() = id.isNullOrEmpty()

    fun String?.path(): String {

        if (!this.isNullOrEmpty()) {

            return "/$this"
        }

        return ""
    }

    private fun String?.pathIn(parent: String): String {

        if (!this.isNullOrEmpty() && !parent.isEmpty()) {

            return "$parent/$this"
        }

        return ""
    }

    private fun directory(): String? {

        parentSelfLink()?.let {
            return when (this) {
                is Database -> it
                is User -> "${it}users"
                is Collection -> "${it}colls"
                is StoredProcedure -> "${it}sprocs"
                is Trigger -> "${it}triggers"
                is Udf -> "${it}udfs"
                is Document -> "${it}docs"
                is Attachment -> "${it}attachments"
                is Permission -> "${it}permissions"
                is Offer -> it
                is Child -> "$it${this.resourceType.path}"
                is Resource -> ResourceOracle.shared.getSelfLink(this.resource)?.lastPathComponentRemoved()
                is PkRanges -> "${it}pkranges"
            }
        }

        return null
    }

    private fun parentSelfLink(): String? {

        return when (this) {
            is Database -> "dbs"
            is User -> ResourceOracle.shared.getSelfLink(Database(this.databaseId))
            is Collection -> ResourceOracle.shared.getSelfLink(Database(this.databaseId))
            is StoredProcedure -> ResourceOracle.shared.getSelfLink(Collection(this.databaseId, this.collectionId))
            is Trigger -> ResourceOracle.shared.getSelfLink(Collection(this.databaseId, this.collectionId))
            is Udf -> ResourceOracle.shared.getSelfLink(Collection(this.databaseId, this.collectionId))
            is Document -> ResourceOracle.shared.getSelfLink(Collection(this.databaseId, this.collectionId))
            is Attachment -> ResourceOracle.shared.getSelfLink(Document(this.databaseId, this.collectionId, this.documentId))
            is Permission -> ResourceOracle.shared.getSelfLink(User(this.databaseId, this.userId))
            is Offer -> "offers"
            is Child -> ResourceOracle.shared.getSelfLink(this.resource)
            is Resource -> ResourceOracle.shared.getSelfLink(this.resource)?.lastPathComponentRemoved()?.lastPathComponentRemoved()
            is PkRanges -> ResourceOracle.shared.getSelfLink(Collection(this.databaseId, this.id))
        }
    }
}