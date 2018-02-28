package com.azure.data.model.indexing

/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

/**
 * Represents an index on an `IndexingPolicy` in the Azure Cosmos DB service.
 */
class Index internal constructor (

        /**
         * Gets or sets the kind of indexing to be applied in the Azure Cosmos DB service.
         */
        var kind: IndexKind? = null,

        /**
         * Specifies the target data type for the index path specification.
         */
        var dataType: DataType? = null,

        /**
         * Specifies the precision to be used for the data type associated with this index.
         */
        var precision: Short? = null
) {

    companion object {

        /**
         * Returns an instance of a hash index with specified `DataType` (and precision) for
         * the Azure Cosmos DB service.
         */
        fun hash(dataType: DataType, precision: Short? = null) : Index =
                Index(IndexKind.Hash, dataType, precision)

        /**
         * Returns an instance of a ranged index with specified `DataType` (and precision) for
         * the Azure Cosmos DB service.
         */
        fun range(dataType: DataType, precision: Short? = null) : Index =
                Index(IndexKind.Range, dataType, precision)

        /**
         * Returns an instance of a spatial index with specified `DataType` for
         * the Azure Cosmos DB service.
         */
        fun spatial(dataType: DataType) : Index =
                Index(IndexKind.Spatial, dataType)
    }
}