package com.azure.data.model.partition

/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

/**
 * Specifies a partition key definition for a particular path in the Azure Cosmos DB service.
 */
class PartitionKeyDefinition(keyPath: String? = null) {

    /**
     * Gets or sets the paths to be partitioned in the Azure Cosmos DB service.
     */
    var paths: List<String>? = null

    /**
     * The algorithm used for partitioning. Only Hash is supported.
     */
    var kind: PartitionKind = PartitionKind.Hash

    var version: PartitionKeyDefinitionVersion? = PartitionKeyDefinitionVersion.V2

    init {
        keyPath?.let {
            paths = arrayListOf(keyPath)
        }
    }
}