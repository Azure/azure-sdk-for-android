package com.azure.data.model.partition

/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

/**
 * Partitioning version.
 */
enum class PartitionKeyDefinitionVersion constructor(val version: Int) {

    /**
     * Original version of hash partitioning.
     */
    V1(1),

    /**
     * Enhanced version of hash partitioning - offers better distribution of long partition keys and uses less storage.
     *
     * This version should be used for any practical purpose, but it is available in newer SDKs only.
     */
    V2(2)
}