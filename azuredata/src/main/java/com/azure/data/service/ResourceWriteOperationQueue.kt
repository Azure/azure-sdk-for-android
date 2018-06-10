package com.azure.data.service

import com.azure.data.model.Resource
import com.azure.data.model.ResourceLocation
import okhttp3.Headers
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

class ResourceWriteOperationQueue private constructor() {

    companion object {
        var shared = ResourceWriteOperationQueue()
    }

    //region Properties

    private var writes: List<ResourceWriteOperation> = arrayListOf()

    private var processedWrites: List<ResourceWriteOperation> = arrayListOf()

    private var isSyncing = false

    private val executor: ExecutorService = Executors.newCachedThreadPool()

    //endregion

    //region Public API

    fun <T: Resource> addCreateOrReplace(resource: T, location: ResourceLocation, headers: Headers? = null, replace: Boolean = false, callback: (Response<T>) -> Unit) {
        // TODO
    }

    fun addDelete(resourceLocation: ResourceLocation, headers: Headers? = null, callback: (DataResponse) -> Unit) {
        // TODO
    }

    //endregion
}