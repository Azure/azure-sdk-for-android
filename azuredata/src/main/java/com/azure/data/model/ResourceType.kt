package com.azure.data.model

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

    Database("dbs",             Db.resourceName,    object : TypeToken<Db>() {}.type),
    User("users",               Usr.resourceName,   object : TypeToken<Usr>() {}.type),
    Permission("permissions",   Perm.resourceName,  object : TypeToken<Perm>() {}.type),
    Collection("colls",         Coll.resourceName,  object : TypeToken<Coll>() {}.type),
    StoredProcedure("sprocs",   Sproc.resourceName, object : TypeToken<Sproc>() {}.type),
    Trigger("triggers",         Trggr.resourceName, object : TypeToken<Trggr>() {}.type),
    Udf("udfs",                 UDF.resourceName,   object : TypeToken<UDF>() {}.type),
    Document("docs",            Doc.resourceName,   object : TypeToken<Doc>() {}.type),
    Attachment("attachments",   Atch.resourceName,  object : TypeToken<Atch>() {}.type),
    Offer("offers",             Ofr.resourceName,   object : TypeToken<Ofr>() {}.type),
    PkRanges("pkranges",        Pkr.resourceName,   object : TypeToken<Pkr>() {}.type);

    val listName: String = "${fullname}s"

    fun isDecendentOf(resourceType: ResourceType) : Boolean {

        return when (this) {
            Database,
            Offer               -> false
            User,
            Collection          -> resourceType == Database
            Document,
            StoredProcedure,
            Trigger,
            Udf                 -> resourceType == Collection || resourceType == Database
            Permission          -> resourceType == User || resourceType == Database
            Attachment          -> resourceType == Document || resourceType == Collection || resourceType == Database
            PkRanges            -> resourceType == Collection
        }
    }

    fun isAncestorOf(resourceType: ResourceType) : Boolean
            = resourceType.isDecendentOf(this)

    val supportsPermissionToken : Boolean
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
                Database        -> listOf(ResourceType.Collection, ResourceType.User)
                User            -> listOf(ResourceType.Permission)
                Permission      -> listOf()
                Collection      -> listOf(ResourceType.Document, ResourceType.StoredProcedure, ResourceType.Trigger, ResourceType.Udf)
                Document        -> listOf(ResourceType.Attachment)
                StoredProcedure -> listOf()
                Trigger         -> listOf()
                Udf             -> listOf()
                Attachment      -> listOf()
                else            -> listOf()
            }
        }

    companion object {

        fun fromType(type: Type) : ResourceType {

            return ResourceType.values().find {
                it.type == type
            } ?: Document // if no match, then we assume we're dealing with a document
        }

        fun fromListName(name: String) : ResourceType {

            return ResourceType.values().find {
                it.listName == name
            } ?: throw Exception("Unable to determine resource type requested")
        }

        val ancestors = listOf(Database, User, Collection, Document)
    }
}