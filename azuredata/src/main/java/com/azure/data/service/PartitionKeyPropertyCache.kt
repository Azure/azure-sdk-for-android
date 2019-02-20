package com.azure.data.service

import com.azure.core.util.getAnnotatedProperties
import com.azure.data.model.partition.PartitionKey
import kotlin.reflect.KProperty1

/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

internal class PartitionKeyPropertyCache {

    companion object {

        private val propertyCache = mutableMapOf<Class<Any>, List<KProperty1<Any, *>>>()

        fun getPartitionKeyValues(resource: Any) : List<String> {

            var props = propertyCache[resource.javaClass]

            if (props == null) {

                props = getAnnotatedProperties<PartitionKey>(resource)

                propertyCache[resource.javaClass] = props
            }

            return props.map { prop -> prop.get(resource).toString() }
        }
    }
}