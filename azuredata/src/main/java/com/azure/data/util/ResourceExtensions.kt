package com.azure.data.util

import com.azure.data.model.Resource
import com.azure.data.model.ResourceType

/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

@JvmOverloads
fun Resource.ancestorIds(includingSelf: Boolean = false) : Map<ResourceType, String> {

    val ancestors: MutableMap<ResourceType, String> = mutableMapOf()

    this.altLink?.split('/')?.let {

        for (ancestor in ResourceType.ancestors) {

            val ancestorPath = ancestor.path

            val index = it.indexOf(ancestorPath)

            if (index > -1 && index < it.size) {

                ancestors[ancestor] = it[index + 1]
            }
        }
    }

    if (includingSelf) {
        ancestors[ResourceType.fromType(this::class.java)] = this.id
    }

    return ancestors
}