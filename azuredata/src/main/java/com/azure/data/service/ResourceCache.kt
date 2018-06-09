package com.azure.data.service

import android.content.Context
import com.azure.core.util.ContextProvider
import com.azure.data.model.Resource
import com.azure.data.model.ResourceList
import com.azure.data.model.ResourceLocation
import com.azure.data.util.ResourceOracle
import com.azure.data.util.json.gson
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

class ResourceCache private constructor() {

    companion object {
        var shared: ResourceCache = ResourceCache()
    }

    //region properties

    var isEnabled = true

    private var executor: ExecutorService = Executors.newCachedThreadPool()

    //endregion

    //region cache

    fun <T: Resource> cache(resource: T) {
        ResourceOracle.shared.storeLinks(resource)

        if (isEnabled) {
            executor.execute {
                ContextProvider.appContext.resourceCacheFile(resource)?.let {
                    it.bufferedWriter().use {
                        it.write(gson.toJson(resource))
                    }
                }
            }
        }
    }

    fun <T: Resource> cache(resources: ResourceList<T>) {
        ResourceOracle.shared.storeLinks(resources)

        resources.items.forEach { cache(it) }
    }

    //endregion


    //region replace

    fun <T: Resource> replace(resource: T) {
        remove(resource)

        cache(resource)
    }

    //endregion

    //region get

    fun <T: Resource> getResourceAt(location: ResourceLocation, resourceClass: Class<T>): T? {
        if (!isEnabled) { return null }

        ContextProvider.appContext.resourceCacheFile(location)?.let {
            it.bufferedReader().use {
                return gson.fromJson<T>(it, resourceClass)
            }
        }

        return null
    }

    fun <T: Resource> getResourcesAt(location: ResourceLocation, resourceClass: Class<T>): ResourceList<T> {
        val resources = ResourceList<T>()

        if (isEnabled) {
            resources.items = ContextProvider.appContext.resourceCacheFiles(location).map {
                it.bufferedReader().use {
                    gson.fromJson<T>(it, resourceClass)
                }
            }

            resources.count = resources.items.count()
        }

        return resources
    }

    //endregion

    //region remove

    fun remove(resource: Resource) {
        if (isEnabled) {
            executor.execute {
                ContextProvider.appContext.resourceCacheDir(resource)?.let {
                    it.deleteRecursively()
                }
            }
        }
    }

    fun remove(resourceLocation: ResourceLocation) {
        if (isEnabled) {
            executor.execute {
                ContextProvider.appContext.resourceCacheDir(resourceLocation)?.let {
                    it.deleteRecursively()
                }
            }
        }
    }

    //endregion

    //region purge

    fun purge() {
        val databasesDir = File(ContextProvider.appContext.azureDataCacheDir(), "dbs")
        val offersDir = File(ContextProvider.appContext.azureDataCacheDir(), "offers")

        if (databasesDir.exists() && databasesDir.isDirectory) {
            databasesDir.deleteRecursively()
        }

        if (offersDir.exists() && offersDir.isDirectory) {
            offersDir.deleteRecursively()
        }
    }

    //endregion
}

//region Context

internal fun Context.resourceCacheFile(resource: Resource): File? {
    ResourceOracle.shared.getFilePath(resource)?.let {
        val directory = File(azureDataCacheDir(), it.directory)

        if (!directory.exists()) {
            directory.mkdirs()
        }

        return File(directory, it.file)
    }

    return null
}

internal fun Context.resourceCacheFile(resourceLocation: ResourceLocation): File? {
    ResourceOracle.shared.getFilePath(resourceLocation)?.let {
        val directory = File(azureDataCacheDir(), it.directory)

        if (!directory.exists()) {
            directory.mkdirs()
        }

        return File(directory, it.file)
    }

    return null
}

internal fun Context.resourceCacheFiles(resourceLocation: ResourceLocation): List<File> {
    resourceCacheDir(resourceLocation)?.let {
        return it.listFiles().map { File(it, "$it.json") }
    }

    return emptyList()
}

internal fun Context.resourceCacheDir(resource: Resource): File? {
    ResourceOracle.shared.getFilePath(resource)?.let {
        val directory = File(azureDataCacheDir(), it.directory)

        if (!directory.exists()) {
            directory.mkdirs()
        }

        return directory
    }

    return null
}

internal fun Context.resourceCacheDir(resourceLocation: ResourceLocation): File? {
    ResourceOracle.shared.getDirectoryPath(resourceLocation)?.let {
        val directory = File(azureDataCacheDir(), it)

        if (!directory.exists()) {
            directory.mkdirs()
        }

        return directory
    }

    return null
}

internal fun Context.azureDataCacheDir(): File {
    val directory = File(cacheDir, "com.azuredata.data")

    if (!directory.exists()) {
        directory.mkdir()
    }

    return directory
}