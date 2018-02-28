package com.azure.data.model.indexing

/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

/**
 * Represents the indexing policy configuration for a collection in the Azure Cosmos DB service.
 */
class IndexingPolicy internal constructor (

    /**
     * Gets or sets a value that indicates whether automatic indexing is enabled for a collection in
     * the Azure Cosmos DB service.
     */
    var automatic: Boolean = false,

    /**
     * Gets or sets the indexing mode (`.consistent` or `.lazy`) in the Azure Cosmos DB service.
     */
    var indexingMode: IndexingMode? = null,

    /**
     * Gets or sets the collection containing `IncludedPath` objects in the Azure Cosmos DB service.
     */
    var includedPaths: MutableList<IncludedPath>? = null,

    /**
     * Gets or sets the collection containing `ExcludedPath` objects in the Azure Cosmos DB service.
     */
    var excludedPaths: MutableList<ExcludedPath>? = null
) {

    companion object {

        fun create(block: IndexingPolicyBuilder.() -> Unit): IndexingPolicy = IndexingPolicyBuilder().apply(block).build()
    }


    /**
     * Specifies a path within a Json document to be excluded while indexing data for the Azure Cosmos DB service.
     */
    class ExcludedPath internal constructor (

        /**
         * Gets or sets the path to be excluded from indexing in the Azure Cosmos DB service.
         */
        var path: String? = null
    )


    /**
     * Specifies a path within a Json document to be included in the Azure Cosmos DB service.
     */
    class IncludedPath internal constructor (

        /**
         * Gets or sets the path to be indexed in the Azure Cosmos DB service.
         */
        var path: String? = null,

        /**
         * Gets or sets the collection of `Index` objects to be applied for this included path in
         * the Azure Cosmos DB service.
         */
        val indexes: List<Index>? = null
    )
}