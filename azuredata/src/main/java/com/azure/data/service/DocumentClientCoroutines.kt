package com.azure.data.service

import com.azure.data.model.Resource
import java.lang.reflect.Type
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

// next
suspend fun <T : Resource> DocumentClient.next(response : ListResponse<T>, resourceType: Type?): ListResponse<T> =

        suspendCoroutine { cont ->

            this.next(response, resourceType) {

                cont.resume(it)
            }
        }