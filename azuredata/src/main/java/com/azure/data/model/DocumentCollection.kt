package com.azure.data.model

import com.google.gson.annotations.SerializedName
import com.azure.data.model.indexing.IndexingPolicy
import com.azure.data.model.partition.PartitionKeyDefinition
import com.azure.data.util.isValidPartitionKeyPath
import java.lang.Exception

/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

/**
 * Represents a document collection in the Azure Cosmos DB service.
 * A collection is a named logical container for documents.
 *
 * - Remark:
 *   A database may contain zero or more named collections and each collection consists of zero or more Json documents.
 *   Being schema-free, the documents in a collection do not need to share the same structure or fields.
 *   Since collections are application resources, they can be authorized using either the master key or resource keys.
 *   Refer to [collections](http://azure.microsoft.com/documentation/articles/documentdb-resources/#collections) for more details on collections.
 */
class DocumentCollection(id: String? = null, partitionKey: String? = null, policy: IndexingPolicy? = null) : Resource(id) {

    /**
     * Gets the self-link for conflicts in a collection from the Azure Cosmos DB service.
     */
    @SerializedName(conflictsLinkKey)
    var conflictsLink: String? = null

    /**
     * Gets the default time to live in seconds for documents in a collection from the Azure Cosmos DB service.
     */
    var defaultTimeToLive: Int? = null

    /**
     * Gets the self-link for documents in a collection from the Azure Cosmos DB service.
     */
    @SerializedName(documentsLinkKey)
    var documentsLink: String? = null

    /**
     * Gets the `IndexingPolicy` associated with the collection from the Azure Cosmos DB service.
     */
    var indexingPolicy: IndexingPolicy? = policy

    /**
     * Gets or sets `PartitionKeyDefinition` object in the Azure Cosmos DB service.
     */
    var partitionKey: PartitionKeyDefinition? = null

    /**
     * Gets the self-link for stored procedures in a collection from the Azure Cosmos DB service.
     */
    @SerializedName(storedProceduresLinkKey)
    var storedProceduresLink: String? = null

    /**
     * Gets the self-link for triggers in a collection from the Azure Cosmos DB service.
     */
    @SerializedName(triggersLinkKey)
    var triggersLink: String? = null

    /**
     * Gets the self-link for user defined functions in a collection from the Azure Cosmos DB service.
     */
    @SerializedName(userDefinedFunctionsLinkKey)
    var userDefinedFunctionsLink: String? = null

    init {

        partitionKey?.let {

            if (!partitionKey.isValidPartitionKeyPath()) {
                throw Exception("Invalid partition key; partition keys should be a / delimited path to a document property, e.g. /user/id")
            }

            this.partitionKey = PartitionKeyDefinition(partitionKey)
        }
    }

    fun childLink (resourceId: String? = null) : String {

        return if (resourceId == null || resourceId == "") {
            selfLink!!.split("/").last().toLowerCase()
        }
        else {
            resourceId.toLowerCase()
        }
    }

    companion object {

        const val resourceName = "DocumentCollection"
        const val listName = "DocumentCollections"

        const val conflictsLinkKey                = "_conflicts"
        const val documentsLinkKey                = "_docs"
        const val storedProceduresLinkKey         = "_sprocs"
        const val triggersLinkKey                 = "_triggers"
        const val userDefinedFunctionsLinkKey     = "_udfs"
    }
}