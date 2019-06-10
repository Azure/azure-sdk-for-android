package com.azure.data.service

import android.content.Context
import android.content.Intent
import com.azure.core.http.HttpStatusCode
import com.azure.core.util.ContextProvider
import com.azure.data.constants.MSHttpHeader
import com.azure.data.model.Resource
import com.azure.data.model.service.*
import com.azure.data.util.ResourceOracle
import com.azure.data.util.ancestorPath
import com.azure.data.util.isValidIdForResource
import com.azure.data.util.json.gson
import okhttp3.Protocol
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

        val shared by lazy {

            val queue = ResourceWriteOperationQueue()
            queue.load()
            return@lazy queue
        }
    }

    //region Properties

    private var writes: MutableList<ResourceWriteOperation> = mutableListOf()

    private var processedWrites: MutableList<ResourceWriteOperation> = mutableListOf()

    private var isSyncing = false

    private val executor: ExecutorService = Executors.newSingleThreadExecutor()

    //endregion

    //region Public API

    fun <T: Resource> addCreateOrReplace(resource: T, requestDetails: RequestDetails, replace: Boolean = false, callback: (Response<T>) -> Unit) {

        createOrReplaceOffline(resource, requestDetails, replace) { response ->

            callback(response)

            if (response.isSuccessful) {

                enqueueWrite(ResourceWriteOperation(

                        type = if (replace) ResourceWriteOperationType.Replace else ResourceWriteOperationType.Create,
                        resource = resource,
                        requestDetails = requestDetails,
                        resourceLocalContentPath = response.response?.header(MSHttpHeader.MSContentPath.value) ?: ""
                ))
            }
        }
    }

    fun addDelete(requestDetails: RequestDetails, callback: (DataResponse) -> Unit) {

        deleteOffline(requestDetails) { response ->

            callback(response)

            if (response.isSuccessful) {

                enqueueWrite(ResourceWriteOperation(

                        type = ResourceWriteOperationType.Delete,
                        resource = null,
                        requestDetails = requestDetails,
                        resourceLocalContentPath = response.response?.header(MSHttpHeader.MSContentPath.value) ?: ""
                ))
            }
        }
    }

    fun sync() {

        executor.execute {

            if (isSyncing || writes.isEmpty()) {
                return@execute
            }

            val writes = this.writes.sortedByResourceType()

            isSyncing = true

            performWrites(writes) { isSuccess ->

                if (isSuccess) {
                    sendOfflineWriteQueueProcessedBroadcast()
                }

                removeCachedResources()
                isSyncing = false
            }
        }
    }

    fun purge() {

        safeExecute {

            ContextProvider.appContext.pendingWritesDir().deleteRecursively()
        }
    }

    //endregion

    //region

    private fun load() {

        safeExecute {

            executor.execute {

                writes = ContextProvider.appContext.pendingWritesFiles()
                        .map { it.bufferedReader().use { reader -> gson.fromJson(reader.readText(), ResourceWriteOperation::class.java) } }
                        .toMutableList()
            }
        }
    }

    private fun performWrites(writes: MutableList<ResourceWriteOperation>, callback: (Boolean) -> Unit) {

        if (writes.isEmpty()) {

            callback(true)
            return
        }

        val write = writes.removeAt(0)

        performWrite(write) { response ->

            if (!response.fromCache) {

                processedWrites.add(write)
                sendBroadcast(response)
                removeWrite(write)
            }

            performWrites(writes, callback)
        }
    }

    private fun performWrite(write: ResourceWriteOperation, callback: (Response<Unit>) -> Unit) {

        when (write.type) {

            ResourceWriteOperationType.Create  -> DocumentClient.shared.createOrReplace(write.resource!!, write.requestDetails, false) { callback(it.map { Unit }) }
            ResourceWriteOperationType.Replace -> DocumentClient.shared.createOrReplace(write.resource!!, write.requestDetails, true) { callback(it.map { Unit }) }
            ResourceWriteOperationType.Delete  -> DocumentClient.shared.delete(write.resource!!) { it.map { Unit } }
        }
    }

    private fun enqueueWrite(write: ResourceWriteOperation) {

        executor.execute {

            val index = writes.indexOf(write)

            if (index < 0) {
                writes.add(write)
                persistWriteOnDisk(write)
                return@execute
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
    }

    private fun removeWrite(write: ResourceWriteOperation) {

        safeExecute {

            val index = writes.indexOf(write)

            if (index < 0) {

                writes.removeAt(index)
                removeWriteFromDisk(write)
            }
        }
    }

    private fun removeCachedResources() {

        safeExecute {

            while (!processedWrites.isEmpty()) {

                val write = processedWrites.removeAt(processedWrites.count() - 1)
                File(URI("${ContextProvider.appContext.azureDataCacheDir().absolutePath}/${write.resourceLocalContentPath}")).deleteRecursively()
            }
        }
    }

    //endregion

    //region Offline Requests

    private fun <T: Resource> createOrReplaceOffline(resource: T, requestDetails: RequestDetails, replace: Boolean = false, callback: (Response<T>) -> Unit) {

        if (!resource.id.isValidIdForResource()) {

            callback(Response(DataError(DocumentClientError.InvalidId)))
            return
        }

        val altLink = requestDetails.resourceLocation.altLink(resource.id)
        val knownSelfLink = ResourceOracle.shared.getSelfLink(altLink)

        if (replace && knownSelfLink.isNullOrEmpty()) {

            val request = okhttp3.Request.Builder()
                    .url("https://localhost/$altLink")
                    .build()

            val response = okhttp3.Response.Builder()
                    .request(request)
                    .protocol(Protocol.HTTP_1_1)
                    .code(HttpStatusCode.NotFound.code)
                    .message(DocumentClientError.NotFound.message!!)
                    .build()

            callback(Response(DataError(DocumentClientError.NotFound), request, response, null, true))
            return
        }

        if (!(replace || knownSelfLink.isNullOrEmpty())) {

            val request = okhttp3.Request.Builder()
                    .url("https://localhost/$altLink")
                    .build()

            val response = okhttp3.Response.Builder()
                    .request(request)
                    .protocol(Protocol.HTTP_1_1)
                    .code(HttpStatusCode.Conflict.code)
                    .message(DocumentClientError.Conflict.message!!)
                    .build()

            callback(Response(DataError(DocumentClientError.Conflict), request, response, null, true))
            return
        }

        (knownSelfLink ?: requestDetails.resourceLocation.selfLink(UUID.randomUUID().toString()))?.let { selfLink ->

            ResourceOracle.shared.storeLinks(selfLink, altLink)
            resource.altLink = altLink
            ResourceCache.shared.cache(resource)

            val request = okhttp3.Request.Builder()
                    .url("https://localhost/$selfLink")
                    .build()

            val response = okhttp3.Response.Builder()
                    .request(request)
                    .protocol(Protocol.HTTP_1_1)
                    .code(if (replace) HttpStatusCode.Ok.code else HttpStatusCode.Created.code)
                    .addHeader(MSHttpHeader.MSContentPath.value, selfLink)
                    .addHeader(MSHttpHeader.MSAltContentPath.value, altLink.ancestorPath())
                    .message(gson.toJson(resource))
                    .build()

            return callback(Response(request, response, null, Result(resource), requestDetails.resourceLocation, requestDetails.resourceLocation.resourceType.type, true))
        }

        callback(Response(DataError(DocumentClientError.InternalError)))
    }

    private fun deleteOffline(requestDetails: RequestDetails, callback: (DataResponse) -> Unit) {

        ResourceOracle.shared.getSelfLink(requestDetails.resourceLocation.link())?.let { selfLink ->

            ResourceCache.shared.remove(requestDetails.resourceLocation)

            val request = okhttp3.Request.Builder()
                    .url("https://com.azuredata.cache/$selfLink")
                    .build()

            val response = okhttp3.Response.Builder()
                    .request(request)
                    .protocol(Protocol.HTTP_1_1)
                    .code(HttpStatusCode.NoContent.code)
                    .addHeader(MSHttpHeader.MSContentPath.value, selfLink)
                    .addHeader(MSHttpHeader.MSAltContentPath.value, requestDetails.resourceLocation.link().ancestorPath())
                    .message("")
                    .build()

            return callback(Response(request, response, null, Result(""), requestDetails.resourceLocation, requestDetails.resourceLocation.resourceType.type, true))
        }

        val request = okhttp3.Request.Builder()
                .url("https://localhost/${requestDetails.resourceLocation.link()}")
                .build()

        val response = okhttp3.Response.Builder()
                .request(request)
                .protocol(Protocol.HTTP_1_1)
                .code(HttpStatusCode.NotFound.code)
                .message(DocumentClientError.NotFound.message!!)
                .build()

        callback(Response(DataError(DocumentClientError.NotFound), request, response, null, true))
    }

    //endregion

    //region Disk Persistence

    private fun persistWriteOnDisk(write: ResourceWriteOperation) {

        safeExecute {
            ContextProvider.appContext
                    .resourceWriteOperationFile(write)
                    .bufferedWriter().use { it.write(gson.toJson(write)) }
        }
    }

    private fun removeWriteFromDisk(write: ResourceWriteOperation) {

        safeExecute {
            ContextProvider.appContext
                    .resourceWriteOperationFile(write)
                    .delete()
        }
    }

    //endregion

    //region Broadcasting

    private fun <T> sendBroadcast(response: Response<T>) {

        val intent = Intent()

        if (response.isSuccessful) {

            intent.action = "com.azuredata.data.OFFLINE_RESOURCE_SYNC_SUCCEEDED"
            intent.putExtra("data", response.jsonData)
        } else {

            intent.action = "com.azuredata.data.OFFLINE_RESOURCE_SYNC_FAILED"
            intent.putExtra("error", response.jsonData)
        }

        ContextProvider.appContext.sendBroadcast(intent)
    }

    private fun sendOfflineWriteQueueProcessedBroadcast() {

        ContextProvider.appContext.sendBroadcast(Intent("com.azuredata.data.OFFLINE_WRITE_OPERATION_QUEUE.PROCESSED"))
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
    return ResourceWriteOperation(type, this.resource, this.requestDetails, this.resourceLocalContentPath)
}

private fun MutableList<ResourceWriteOperation>.sortedByResourceType(): MutableList<ResourceWriteOperation> {
    return sortedWith(ResourceWriteOperationHierarchicalComparator()).toMutableList()
}

private class ResourceWriteOperationHierarchicalComparator: Comparator<ResourceWriteOperation> {

    override fun compare(lhs: ResourceWriteOperation?, rhs: ResourceWriteOperation?): Int {

        lhs?.let { l ->
            rhs?.let { r ->

                if (l.requestDetails.resourceLocation.resourceType.isAncestorOf(r.requestDetails.resourceLocation.resourceType)) {
                    return 1
                }

                if (r.requestDetails.resourceLocation.resourceType.isAncestorOf(l.requestDetails.resourceLocation.resourceType)) {
                    return -1
                }
            }
        }

        return 0
    }
}

//endregion