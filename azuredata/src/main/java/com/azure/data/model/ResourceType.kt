package com.azure.data.model

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

/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

enum class ResourceType(val path: String, fullname: String, val type: Type, val listType: Type) {

    Database("dbs",             Db.resourceName,    object : TypeToken<Db>() {}.type,       object : TypeToken<ResourceList<Db>>() {}.type),
    User("users",               Usr.resourceName,   object : TypeToken<Usr>() {}.type,      object : TypeToken<ResourceList<Usr>>() {}.type),
    Permission("permissions",   Perm.resourceName,  object : TypeToken<Perm>() {}.type,     object : TypeToken<ResourceList<Perm>>() {}.type),
    Collection("colls",         Coll.resourceName,  object : TypeToken<Coll>() {}.type,     object : TypeToken<ResourceList<Coll>>() {}.type),
    StoredProcedure("sprocs",   Sproc.resourceName, object : TypeToken<Sproc>() {}.type,    object : TypeToken<ResourceList<Sproc>>() {}.type),
    Trigger("triggers",         Trggr.resourceName, object : TypeToken<Trggr>() {}.type,    object : TypeToken<ResourceList<Trggr>>() {}.type),
    Udf("udfs",                 UDF.resourceName,   object : TypeToken<UDF>() {}.type,      object : TypeToken<ResourceList<UDF>>() {}.type),
    Document("docs",            Doc.resourceName,   object : TypeToken<Doc>() {}.type,      object : TypeToken<ResourceList<Doc>>() {}.type),
    Attachment("attachments",   Atch.resourceName,  object : TypeToken<Atch>() {}.type,     object : TypeToken<ResourceList<Atch>>() {}.type),
    Offer("offers",             Ofr.resourceName,   object : TypeToken<Ofr>() {}.type,      object : TypeToken<ResourceList<Ofr>>() {}.type);

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

    companion object {

        fun<T: Resource> fromType(clazz: Class<T>) : ResourceType {

            //is this a Document?
            if (Doc::class.java.isAssignableFrom(clazz)) {
                return Document
            }

            return ResourceType.values().find {
                it.type == clazz
            } ?: throw Exception("Unable to determine resource type requested")
        }

        fun fromListName(name: String) : ResourceType {

            return ResourceType.values().find {
                it.listName == name
            } ?: throw Exception("Unable to determine resource type requested")
        }

        val ancestors = listOf(Database, User, Collection, Document)
    }
}