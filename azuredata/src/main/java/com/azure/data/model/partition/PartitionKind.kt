package com.azure.data.model.partition

/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

/**
 * Specifies the partition scheme for an multiple-partitioned collection in the Azure Cosmos DB database service.
 */
enum class PartitionKind {
    /**
     * The Partition of a document is calculated based on the hash value of the PartitionKey.
     */
    Hash
}