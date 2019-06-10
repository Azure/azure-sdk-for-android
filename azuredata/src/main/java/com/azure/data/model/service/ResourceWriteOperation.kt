package com.azure.data.model.service

import com.azure.data.model.Resource

/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

class ResourceWriteOperation(
        val type: ResourceWriteOperationType,
        val resource: Resource?,
        val requestDetails: RequestDetails,
        var resourceLocalContentPath: String
    ): Comparable<ResourceWriteOperation> {

    override fun compareTo(other: ResourceWriteOperation): Int {
        return resourceLocalContentPath.compareTo(other.resourceLocalContentPath)
    }
}