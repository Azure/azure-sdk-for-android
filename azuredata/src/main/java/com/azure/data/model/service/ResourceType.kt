package com.azure.data.model.service

import com.azure.data.model.*
import com.azure.data.model.partition.PartitionKeyRange
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

typealias Db = Database
typealias Usr = User
typealias Perm = Permission
typealias Coll = DocumentCollection
typealias Sproc = StoredProcedure
typealias Trggr = Trigger
typealias UDF = UserDefinedFunction
typealias Doc = Document
typealias Atch = Attachment
typealias Ofr = Offer
typealias Pkr = PartitionKeyRange

/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

enum class ResourceType(val path: String, fullname: String, val type: Type) {

    Database("dbs", com.azure.data.model.Database.resourceName, object : TypeToken<Db>() {}.type),
    User("users", com.azure.data.model.User.resourceName, object : TypeToken<Usr>() {}.type),
    Permission("permissions", com.azure.data.model.Permission.resourceName, object : TypeToken<Perm>() {}.type),
    Collection("colls", DocumentCollection.resourceName, object : TypeToken<Coll>() {}.type),
    StoredProcedure("sprocs", com.azure.data.model.StoredProcedure.resourceName, object : TypeToken<Sproc>() {}.type),
    Trigger("triggers", com.azure.data.model.Trigger.resourceName, object : TypeToken<Trggr>() {}.type),
    Udf("udfs", UserDefinedFunction.resourceName, object : TypeToken<UDF>() {}.type),
    Document("docs", com.azure.data.model.Document.resourceName, object : TypeToken<Doc>() {}.type),
    Attachment("attachments", com.azure.data.model.Attachment.resourceName, object : TypeToken<Atch>() {}.type),
    Offer("offers", com.azure.data.model.Offer.resourceName, object : TypeToken<Ofr>() {}.type),
    PkRanges("pkranges", Pkr.resourceName, object : TypeToken<Pkr>() {}.type);

    val listName: String = "${fullname}s"

    fun isDecendentOf(resourceType: ResourceType): Boolean {

        return when (this) {
            Database,
            Offer -> false
            User,
            Collection -> resourceType == Database
            Document,
            StoredProcedure,
            Trigger,
            Udf -> resourceType == Collection || resourceType == Database
            Permission -> resourceType == User || resourceType == Database
            Attachment -> resourceType == Document || resourceType == Collection || resourceType == Database
            PkRanges -> resourceType == Collection
        }
    }

    fun isAncestorOf(resourceType: ResourceType): Boolean = resourceType.isDecendentOf(this)

    val supportsPermissionToken: Boolean
        get() {
            return when (this) {
                Collection,
                Document,
                StoredProcedure,
                Trigger,
                Udf,
                Attachment -> true
                else -> false
            }
        }

    val children: List<ResourceType>
        get() {
            return when (this) {
                Database -> listOf(Collection, User)
                User -> listOf(Permission)
                Permission -> listOf()
                Collection -> listOf(Document, StoredProcedure, Trigger, Udf)
                Document -> listOf(Attachment)
                StoredProcedure -> listOf()
                Trigger -> listOf()
                Udf -> listOf()
                Attachment -> listOf()
                else -> listOf()
            }
        }

    companion object {

        fun fromType(type: Type): ResourceType {

            return ResourceType.values().find {
                it.type == type
            } ?: Document // if no match, then we assume we're dealing with a document
        }

        fun fromListName(name: String): ResourceType {

            return ResourceType.values().find {
                it.listName == name
            } ?: throw Exception("Unable to determine resource type requested")
        }

        val ancestors = listOf(Database, User, Collection, Document)
    }
}