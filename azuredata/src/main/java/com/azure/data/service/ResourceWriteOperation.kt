package com.azure.data.service

import com.azure.data.model.Resource
import com.azure.data.model.ResourceLocation

/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

class ResourceWriteOperation(
        val type: ResourceWriteOperationType,
        val resource: Resource?,
        val resourceLocation: ResourceLocation,
        var resourceLocalContentPath: String,
        val httpHeaders: MutableMap<String, String>
    ): Comparable<ResourceWriteOperation> {

    override fun compareTo(other: ResourceWriteOperation): Int {
        return resourceLocalContentPath.compareTo(other.resourceLocalContentPath)
    }
}