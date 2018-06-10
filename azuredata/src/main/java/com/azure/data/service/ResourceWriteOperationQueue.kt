package com.azure.data.service

import android.content.Context
import com.azure.core.util.ContextProvider
import com.azure.data.constants.MSHttpHeader
import com.azure.data.model.DataError
import com.azure.data.model.DocumentClientError
import com.azure.data.model.Resource
import com.azure.data.model.ResourceLocation
import com.azure.data.model.Result
import com.azure.data.util.ResourceOracle
import com.azure.data.util.ancestorPath
import com.azure.data.util.isValidIdForResource
import com.azure.data.util.json.gson
import okhttp3.Headers
import java.io.File
import java.net.URI
import java.util.UUID
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

class ResourceWriteOperationQueue {

    companion object {
        var shared = ResourceWriteOperationQueue()
    }

    private constructor() {
        load()
    }

    //region Properties

    private var writes: MutableList<ResourceWriteOperation> = mutableListOf()

    private var processedWrites: MutableList<ResourceWriteOperation> = mutableListOf()

    private var isSyncing = false

    private val executor: ExecutorService = Executors.newCachedThreadPool()

    //endregion

    //region Public API

    fun <T: Resource> addCreateOrReplace(resource: T, location: ResourceLocation, headers: Headers? = null, replace: Boolean = false, callback: (Response<T>) -> Unit) {
        createOrReplaceOffline(resource, location, replace, { response ->
            callback(response)

            if (response.isSuccessful) {
                enqueueWrite(ResourceWriteOperation(
                        type = if (replace) ResourceWriteOperationType.Replace else ResourceWriteOperationType.Create,
                        resource = resource,
                        resourceLocation = location,
                        resourceLocalContentPath = response.response?.header(MSHttpHeader.MSContentPath.name) ?: "",
                        httpHeaders = headers ?: Headers.of(mutableMapOf())
                ))
            }
        })
    }

    fun addDelete(resourceLocation: ResourceLocation, headers: Headers? = null, callback: (DataResponse) -> Unit) {
        deleteOffline(resourceLocation, { response ->
            callback(response)

            if (response.isSuccessful) {
                enqueueWrite(ResourceWriteOperation(
                        type = ResourceWriteOperationType.Delete,
                        resource = null,
                        resourceLocation = resourceLocation,
                        resourceLocalContentPath = response.response?.header(MSHttpHeader.MSContentPath.name) ?: "",
                        httpHeaders = headers ?: Headers.of(mutableMapOf())
                ))
            }
        })
    }

    fun sync() {
        if (isSyncing || writes.isEmpty()) {
            return
        }

        val writes = this.writes.sortedByResourceType()

        isSyncing = true

        performWrites(writes, {
            removeCachedResources()
            isSyncing = false
        })
    }

    //endregion

    //region

    private fun load() {
        writes = ContextProvider.appContext.pendingWritesFiles()
                    .map { it.bufferedReader().use { gson.fromJson(it.readText(), ResourceWriteOperation::class.java) } }
                    .toMutableList()
    }

    private fun performWrites(writes: MutableList<ResourceWriteOperation>, callback: (Boolean) -> Unit) {
        if (writes.isEmpty()) {
            callback(true)
            return
        }

        val write = writes.removeAt(0)

        performWrite(write, { response ->
            if (!response.fromCache) {
                processedWrites.add(write)
                removeWrite(write)
            }

            performWrites(writes, callback)
        })
    }

    private fun performWrite(write: ResourceWriteOperation, callback: (Response<Unit>) -> Unit) {
        when (write.type) {
            ResourceWriteOperationType.Create  -> DocumentClient.shared.createOrReplace(write.resource!!, write.resourceLocation, false, write.httpHeaders, { callback(it.map { Unit }) })
            ResourceWriteOperationType.Replace -> DocumentClient.shared.createOrReplace(write.resource!!, write.resourceLocation, true, write.httpHeaders, { callback(it.map { Unit }) })
            ResourceWriteOperationType.Delete  -> DocumentClient.shared.delete(write.resourceLocation, { it.map { Unit } })
        }
    }

    private fun enqueueWrite(write: ResourceWriteOperation) {
        val index = writes.indexOf(write)

        if (index < 0) {
            writes.add(write)
            persistWriteOnDisk(write)
            return
        }

        val existingWrite = writes[index]

        when (Pair(existingWrite.type, write.type)) {
            Pair(ResourceWriteOperationType.Create, ResourceWriteOperationType.Replace) -> {
                writes[index] = write.withType(ResourceWriteOperationType.Create)
                removeWriteFromDisk(existingWrite)
                persistWriteOnDisk(write)
            }

            Pair(ResourceWriteOperationType.Create, ResourceWriteOperationType.Delete) -> {
                writes.removeAt(index)
                removeWriteFromDisk(existingWrite)
            }

            Pair(ResourceWriteOperationType.Replace, ResourceWriteOperationType.Delete) -> {
                writes[index] = write
                removeWriteFromDisk(existingWrite)
                persistWriteOnDisk(write)
            }

            Pair(ResourceWriteOperationType.Replace, ResourceWriteOperationType.Replace) -> {
                writes[index] = write
                removeWriteFromDisk(existingWrite)
                persistWriteOnDisk(write)
            }

            else -> { }
        }
    }

    private fun removeWrite(write: ResourceWriteOperation) {
        val index = writes.indexOf(write)

        if (index < 0) {
            writes.removeAt(index)
            removeWriteFromDisk(write)
        }
    }

    private fun removeCachedResources() {
        while (!processedWrites.isEmpty()) {
            val write = processedWrites.removeAt(processedWrites.count() - 1)
            File(URI("${ContextProvider.appContext.azureDataCacheDir().absolutePath}/${write.resourceLocalContentPath}")).deleteRecursively()
        }
    }

    //endregion

    //region Offline Requests

    private fun <T: Resource> createOrReplaceOffline(resource: T, location: ResourceLocation, replace: Boolean = false, callback: (Response<T>) -> Unit) {
        if (!resource.id.isValidIdForResource()) {
            callback(Response(DataError(DocumentClientError.InvalidId)))
            return
        }

        val altLink = location.altLink(resource.id)
        val knownSelfLink = ResourceOracle.shared.getSelfLink(altLink)

        if (replace && knownSelfLink.isNullOrEmpty()) {
            callback(Response(DataError(DocumentClientError.NotFound)))
            return
        }

        if (!(replace || knownSelfLink.isNullOrEmpty())) {
            callback(Response(DataError(DocumentClientError.Conflict)))
        }

        (knownSelfLink ?: location.selfLink(UUID.randomUUID().toString()))?.let { selfLink ->
            resource.selfLink = selfLink
            ResourceCache.shared.cache(resource)

            val response = okhttp3.Response.Builder()
                    .code(if (replace) 200 else 201)
                    .addHeader(MSHttpHeader.MSContentPath.name, selfLink)
                    .addHeader(MSHttpHeader.MSAltContentPath.name, altLink.ancestorPath())
                    .build()

            return callback(Response(request = null, response = response, result = Result(resource), resourceLocation = location, fromCache = true))
        }

        callback(Response(DataError(DocumentClientError.InternalError)))
    }

    private fun deleteOffline(resourceLocation: ResourceLocation, callback: (DataResponse) -> Unit) {
        ResourceOracle.shared.getSelfLink(resourceLocation.link())?.let { selfLink ->
            ResourceCache.shared.remove(resourceLocation)

            val response = okhttp3.Response.Builder()
                    .code(204)
                    .addHeader(MSHttpHeader.MSContentPath.name, selfLink)
                    .addHeader(MSHttpHeader.MSAltContentPath.name, resourceLocation.link().ancestorPath())
                    .build()

            return callback(Response(request = null, response = response, result = Result(""), resourceLocation = resourceLocation, fromCache = true))
        }

        callback(Response(DataError(DocumentClientError.NotFound)))
    }

    //endregion

    //region Disk Persistence

    private fun persistWriteOnDisk(write: ResourceWriteOperation) {
        ContextProvider.appContext
                .resourceWriteOperationFile(write)
                .bufferedWriter().use { it.write(gson.toJson(write)) }
    }

    private fun removeWriteFromDisk(write: ResourceWriteOperation) {
        ContextProvider.appContext
                .resourceWriteOperationFile(write)
                .delete()
    }

    //endregion
}

//region Context

private fun Context.resourceWriteOperationFile(write: ResourceWriteOperation): File {
    return File(pendingWritesDir(), "${write.resourceLocalContentPath.hashCode()}.json")
}

private fun Context.pendingWritesFiles(): List<File> {
    return pendingWritesDir().listFiles().asList()
}

private fun Context.pendingWritesDir(): File {
    val directory = File(azureDataCacheDir(), "writes")

    if (!directory.exists()) {
        directory.mkdirs()
    }

    return directory
}

//endregion

//region

private fun ResourceWriteOperation.withType(type: ResourceWriteOperationType): ResourceWriteOperation {
    return ResourceWriteOperation(type, this.resource, this.resourceLocation, this.resourceLocalContentPath, this.httpHeaders)
}

private fun MutableList<ResourceWriteOperation>.sortedByResourceType(): MutableList<ResourceWriteOperation> {
    return sortedWith(ResourceWriteOperationHierarchicalComparator()).toMutableList()
}

private class ResourceWriteOperationHierarchicalComparator: Comparator<ResourceWriteOperation> {
    override fun compare(lhs: ResourceWriteOperation?, rhs: ResourceWriteOperation?): Int {
        lhs?.let { l ->
            rhs?.let { r ->
                if (l.resourceLocation.resourceType.isAncestorOf(r.resourceLocation.resourceType)) {
                    return 1
                }

                if (r.resourceLocation.resourceType.isAncestorOf(l.resourceLocation.resourceType)) {
                    return -1
                }
            }
        }

        return 0
    }
}

//endregion