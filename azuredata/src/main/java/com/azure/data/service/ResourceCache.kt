package com.azure.data.service

import android.content.Context
import com.azure.core.log.e
import com.azure.core.util.ContextProvider
import com.azure.data.model.*
import com.azure.data.model.service.ResourceLocation
import com.azure.data.util.ResourceOracle
import com.azure.data.util.json.gson
import com.azure.data.util.lastPathComponent
import java.io.File
import java.lang.Exception
import java.lang.reflect.Type
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

    fun <T : Resource> cache(resource: T) {

        ResourceOracle.shared.storeLinks(resource)

        if (isEnabled) {

            executor.execute {
                safeExecute {

                    ContextProvider.appContext.resourceCacheFile(resource)?.let {

                        it.bufferedWriter().use { writer ->
                            writer.write(encrypt(gson.toJson(resource)))
                        }
                    }
                }
            }
        }
    }

    fun <T : Resource> cache(resources: ResourceList<T>) {

        ResourceOracle.shared.storeLinks(resources)

        resources.items.forEach { cache(it) }
    }

    fun <T : Resource> cache(resources: ResourceList<T>, query: Query, contentPath: String) {

        if (resources.count == 0) {
            return
        }

        ResourceOracle.shared.storeLinks(resources)

        if (isEnabled) {

            executor.execute {
                safeExecute {

                    val metadata = ResourcesMetadata(resources.resourceId!!, contentPath)
                    val metadataPath = ContextProvider.appContext.metadatafileUrl(query)

                    metadataPath.bufferedWriter().use { writer ->
                        writer.write(encrypt(gson.toJson(metadata)))
                    }

                    resources.items.forEach { resource ->

                        val file = ContextProvider.appContext.resourceCacheFile(resource, query)

                        file.bufferedWriter().use { writer ->
                            writer.write(encrypt(gson.toJson(resource)))
                        }
                    }
                }
            }
        }
    }

    //endregion


    //region replace

    fun <T : Resource> replace(resource: T) {

        remove(resource)

        cache(resource)
    }

    //endregion

    //region get

    fun <T : Resource> getResourceAt(location: ResourceLocation, resourceType: Type): T? {

        if (!isEnabled) {
            return null
        }

        return safe {

            ContextProvider.appContext.resourceCacheFile(location)?.let { file ->

                file.bufferedReader().use { gson.fromJson<T>(decrypt(it.readText()), resourceType) }
            }
        }
    }

    fun <T : Resource> getResourcesAt(location: ResourceLocation, resourceType: Type): ResourceList<T>? {

        if (isEnabled) {
            safeExecute {

                val resources = ResourceList<T>()

                resources.items = ContextProvider.appContext.resourceCacheFiles(location).map { file ->

                    file.bufferedReader().use {
                        gson.fromJson<T>(decrypt(it.readText()), resourceType)
                    }
                }

                resources.count = resources.items.count()

                return resources
            }
        }

        return null
    }

    fun <T : Resource> getResourcesForQuery(query: Query, resourceType: Type): ResourceList<T>? {

        if (isEnabled) {
            safeExecute {

                val resources = ResourceList<T>()
                val metadataPath = ContextProvider.appContext.metadatafileUrl(query)

                val metadata = metadataPath.bufferedReader().use {
                    gson.fromJson<ResourcesMetadata>(decrypt(it.readText()), ResourcesMetadata::class.java)
                }

                resources.resourceId = metadata.resourceId

                resources.items = ContextProvider.appContext.resourceCacheFiles(query).map { file ->

                    file.bufferedReader().use {
                        gson.fromJson<T>(decrypt(it.readText()), resourceType)
                    }
                }

                resources.count = resources.items.count()
                resources.setAltContentLink(ResourceType.fromType(resourceType).path, metadata.contentPath)

                return resources
            }
        }

        return null
    }

    //endregion

    //region remove

    fun remove(resource: Resource) {

        ResourceOracle.shared.removeLinks(resource)

        if (isEnabled) {

            safeExecute {
                executor.execute {
                    ContextProvider.appContext.resourceCacheDir(resource)?.deleteRecursively()
                }
            }
        }
    }

    fun remove(resourceLocation: ResourceLocation) {

        ResourceOracle.shared.removeLinks(resourceLocation)

        if (isEnabled && !resourceLocation.isFeed) {

            safeExecute {
                executor.execute {
                    ContextProvider.appContext.resourceCacheDir(resourceLocation)?.deleteRecursively()
                }
            }
        }
    }

    //endregion

    //region purge

    fun purge() {

        safeExecute {

            ResourceOracle.shared.purge()

            val databasesDir = File(ContextProvider.appContext.azureDataCacheDir(), "dbs")
            val offersDir = File(ContextProvider.appContext.azureDataCacheDir(), "offers")
            val queriesDir = File(ContextProvider.appContext.azureDataCacheDir(), "queries")

            if (databasesDir.exists() && databasesDir.isDirectory) {
                databasesDir.deleteRecursively()
            }

            if (offersDir.exists() && offersDir.isDirectory) {
                offersDir.deleteRecursively()
            }

            if (queriesDir.exists() && queriesDir.isDirectory) {
                queriesDir.deleteRecursively()
            }
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
    private data class ResourcesMetadata(
            val resourceId: String,
            val contentPath: String
    )
}

//region Context

internal fun Context.resourceCacheFile(resource: Resource): File? {

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

internal fun Context.resourceCacheFile(resourceLocation: ResourceLocation): File? {

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

internal fun Context.resourceCacheFiles(resourceLocation: ResourceLocation): List<File> {

    resourceCacheDir(resourceLocation)?.let { file ->

        return file.listFiles().map { File(it, "${it.name}.json") }
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

internal fun createEmptyChildDirectoriesIfNecessary(parent: File, resourceType: ResourceType) {

    resourceType.children.forEach {

        val childDirectory = File(parent, it.path)

        if (!childDirectory.exists()) {
            childDirectory.mkdirs()
        }
    }
}

internal fun Context.resourceCacheDir(query: Query) : File {

    val queryPath = ResourceOracle.shared.getDirectoryPath(query)
    val directory = File(azureDataCacheDir(), queryPath)

    if (!directory.exists()) {
        directory.mkdirs()
    }

    return directory
}

internal fun Context.metadatafileUrl(query: Query) : File {

    val dir = this.resourceCacheDir(query)

    return File(dir, "metadata.json")
}

internal fun Context.resultsCacheDir(query: Query) : File {

    val dir = this.resourceCacheDir(query)
    val resultsDir = File(dir, "results")

    if (!resultsDir.exists()) {
        resultsDir.mkdirs()
    }

    return resultsDir
}

internal fun Context.resourceCacheFile(resource: Resource, query: Query) : File {

    val filename = "${resource.selfLink?.lastPathComponent() ?: resource.resourceId}.json"
    val resultsPath = this.resultsCacheDir(query)

    return File(resultsPath, filename)
}

internal fun Context.resourceCacheFiles(query: Query) : Array<File> {

    val resultsPath = this.resultsCacheDir(query)

    return resultsPath.listFiles()
}

//endregion


//region

internal inline fun <T> safe(block: () -> T): T? {
    return try { block() } catch (ex: Exception) { e(ex) ; null }
}

internal inline fun safeExecute(block: () -> Unit) {
    return try { block() } catch (ex: Exception) { e(ex) }
}

//endregion