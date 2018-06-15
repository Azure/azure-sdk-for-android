package com.azure.data.service

import android.content.Context
import com.azure.core.util.ContextProvider
import com.azure.data.model.Resource
import com.azure.data.model.ResourceList
import com.azure.data.model.ResourceLocation
import com.azure.data.model.ResourceType
import com.azure.data.util.ResourceOracle
import com.azure.data.util.json.gson
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

internal class ResourceCache private constructor() {

    companion object {
        var shared: ResourceCache = ResourceCache()
    }

    //region properties

    var isEnabled = true

    var resourceEncryptor: ResourceEncryptor? = null

    private var executor: ExecutorService = Executors.newCachedThreadPool()

    //endregion

    //region cache

    fun <T: Resource> cache(resource: T) {
        ResourceOracle.shared.storeLinks(resource)

        if (isEnabled) {
            executor.execute {
                ContextProvider.appContext.resourceCacheFile(resource)?.let {
                    it.bufferedWriter().use {
                        it.write(encrypt(gson.toJson(resource)))
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
                return gson.fromJson<T>(decrypt(it.readText()), resourceClass)
            }
        }

        return null
    }

    fun <T: Resource> getResourcesAt(location: ResourceLocation, resourceClass: Class<T>): ResourceList<T> {
        val resources = ResourceList<T>()

        if (isEnabled) {
            resources.items = ContextProvider.appContext.resourceCacheFiles(location).map {
                it.bufferedReader().use {
                    gson.fromJson<T>(decrypt(it.readText()), resourceClass)
                }
            }

            resources.count = resources.items.count()
        }

        return resources
    }

    //endregion

    //region remove

    fun remove(resource: Resource) {
        ResourceOracle.shared.removeLinks(resource)

        if (isEnabled) {
            executor.execute {
                ContextProvider.appContext.resourceCacheDir(resource)?.let {
                    it.deleteRecursively()
                }
            }
        }
    }

    fun remove(resourceLocation: ResourceLocation) {
        ResourceOracle.shared.removeLinks(resourceLocation)

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

    //region

    private fun encrypt(data: String): String {
        resourceEncryptor?.let { return it.encrypt(data) }
        return data
    }

    private fun decrypt(data: String): String {
        resourceEncryptor?.let { return it.decrypt(data) }
        return data
    }

    //endregion
}

//region Context

private fun Context.resourceCacheFile(resource: Resource): File? {
    ResourceOracle.shared.getFilePath(resource)?.let {
        val directory = File(azureDataCacheDir(), it.directory)

        if (!directory.exists()) {
            directory.mkdirs()
        }

        createEmptyChildDirectoriesIfNecessary(directory, ResourceType.fromType(resource::class.java))

        return File(directory, it.file)
    }

    return null
}

private fun Context.resourceCacheFile(resourceLocation: ResourceLocation): File? {
    ResourceOracle.shared.getFilePath(resourceLocation)?.let {
        val directory = File(azureDataCacheDir(), it.directory)

        if (!directory.exists()) {
            directory.mkdirs()
        }

        createEmptyChildDirectoriesIfNecessary(directory, resourceLocation.resourceType)

        return File(directory, it.file)
    }

    return null
}

private fun Context.resourceCacheFiles(resourceLocation: ResourceLocation): List<File> {
    resourceCacheDir(resourceLocation)?.let {
        return it.listFiles().map { File(it, "$it.json") }
    }

    return emptyList()
}

private fun Context.resourceCacheDir(resource: Resource): File? {
    ResourceOracle.shared.getFilePath(resource)?.let {
        val directory = File(azureDataCacheDir(), it.directory)

        if (!directory.exists()) {
            directory.mkdirs()
        }

        return directory
    }

    return null
}

private fun Context.resourceCacheDir(resourceLocation: ResourceLocation): File? {
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

private fun createEmptyChildDirectoriesIfNecessary(parent: File, resourceType: ResourceType) {
    resourceType.children.forEach {
        val childDirectory = File(parent, it.path)

        if (!childDirectory.exists()) {
            childDirectory.mkdirs()
        }
    }
}

//endregion