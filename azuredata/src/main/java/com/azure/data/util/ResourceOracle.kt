package com.azure.data.util

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import com.azure.core.util.MapCompat
import com.azure.data.model.*

/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */
@SuppressLint("CommitPrefEdits")
internal class ResourceOracle private constructor (appContext: Context, host: String) {

    private val altLinkLookupStorageKey = "com.azure.data.lookup.altlink.$host"
    private val selfLinkLookupStorageKey = "com.azure.data.lookup.selflink.$host"

    private lateinit var altLinkPrefsEditor: SharedPreferences.Editor
    private lateinit var selfLinkPrefsEditor: SharedPreferences.Editor
    private lateinit var altLinkLookup: MutableMap<String, String>
    private lateinit var selfLinkLookup: MutableMap<String, String>

    private val slashString: String = "/"
    private val slashCharacter: Char = '/'

    init {
        restore(appContext)
    }

    fun restore(appCtx: Context) {

        val altLinkPrefs = appCtx.getSharedPreferences(altLinkLookupStorageKey, Context.MODE_PRIVATE)
        val selfLinkPrefs = appCtx.getSharedPreferences(selfLinkLookupStorageKey, Context.MODE_PRIVATE)

        //copy prefs to maps
        altLinkLookup = altLinkPrefs.all.mapValues { it.value.toString() }.toMutableMap()
        selfLinkLookup = selfLinkPrefs.all.mapValues { it.value.toString() }.toMutableMap()

        // since we're only ever going to access/write to these pref objects in this class,
        //  we'll cache the editor objects, hoping to mitigate potential threading issues with multiple editors
        //  this is nonstandard but seems appropriate here in this use case
        altLinkPrefsEditor = altLinkPrefs.edit()
        selfLinkPrefsEditor = selfLinkPrefs.edit()
    }

    fun purge() {

        altLinkLookup.clear()
        selfLinkLookup.clear()
        altLinkPrefsEditor.clear()
        selfLinkPrefsEditor.clear()

        commit()
    }

    private fun commit() {

        altLinkPrefsEditor.apply()
        selfLinkPrefsEditor.apply()
    }

    private fun doStoreLinks(resource: Resource) {

        doStoreLinks(resource.selfLink, resource.altLink)
    }

    private fun doStoreLinks(selfLink: String?, altLink: String?) {

        selfLink?.let {
            altLink?.let { _ ->

                val altLinkSubstrings = altLink.split(slashCharacter)
                val selfLinkSubstrings = selfLink.trimEnd('/').split(slashCharacter)

                if (selfLinkSubstrings.size == altLinkSubstrings.size) {

                    var i = 0

                    while (i < selfLinkSubstrings.size) {

                        val altLinkComponent = altLinkSubstrings.dropLast(i).joinToString(slashString)
                        val selfLinkComponent = selfLinkSubstrings.dropLast(i).joinToString(slashString).plus(slashCharacter)

                        altLinkLookup[selfLinkComponent] = altLinkComponent
                        selfLinkLookup[altLinkComponent] = selfLinkComponent

                        altLinkPrefsEditor.putString(selfLinkComponent, altLinkComponent)
                        selfLinkPrefsEditor.putString(altLinkComponent, selfLinkComponent) //self links come back from Cosmos DB with trailing slash so we're trying to preserve that here

                        i += 2
                    }
                }
            }
        }
    }

    fun storeLinks(resource: ResourceBase) {

        if (resource is ResourceList<*>) {

            for (resourceItem in resource.items) {

                doStoreLinks(resourceItem)
            }
        } else {

            doStoreLinks(resource as Resource)
        }

        commit()
    }

    fun storeLinks(selfLink: String, altLink: String) {
        doStoreLinks(selfLink, altLink)
    }

    private fun doRemoveLinks(resource: Resource) {

        getSelfLink(resource)?.let {

            altLinkLookup.remove(it)
            altLinkPrefsEditor.remove(it)
        }

        getAltLink(resource)?.let {

            selfLinkLookup.remove(it)
            selfLinkPrefsEditor.remove(it)
        }
    }

    private fun doRemoveLinks(resourceLocation: ResourceLocation) {

        getSelfLink(resourceLocation)?.let {

            altLinkLookup.remove(it)?.let {
                selfLinkLookup.remove(it)
                selfLinkPrefsEditor.remove(it)
            }

            altLinkPrefsEditor.remove(it)
        }
    }

    fun removeLinks(resource: Resource, commit: Boolean = true) {

        doRemoveLinks(resource)

        if (commit) {
            commit()
        }
    }

    fun removeLinks(resourceLocation: ResourceLocation, commit: Boolean = true) {

        doRemoveLinks(resourceLocation)

        if (commit) {
            commit()
        }
    }

    fun getParentAltLink(resource: Resource): String? {

        getAltLink(resource)?.let { altLink ->

            val altLinkSubstrings = altLink.split(slashCharacter)

            if (altLinkSubstrings.size > 2) {

                return altLinkSubstrings.dropLast(2).joinToString(slashString)
            }
        }

        return null
    }

    fun getParentSelfLink(resource: Resource): String? {

        getSelfLink(resource)?.let { selfLink ->

            val selfLinkSubstrings = selfLink.trimEnd(slashCharacter).split(slashCharacter)

            if (selfLinkSubstrings.size > 2) {

                return selfLinkSubstrings.dropLast(2).joinToString(slashString).plus(slashCharacter)
            }
        }

        return null
    }

    fun getAltLink(resource: Resource): String? {

        var altLink = resource.altLink?.trim(slashCharacter)

        if (altLink.isNullOrEmpty()) {
            resource.selfLink?.let {

                altLink = MapCompat.getOrDefault(altLinkLookup, it, null)
            }
        }

        return altLink
    }

    fun getSelfLink(resource: Resource): String? {

        var selfLink = resource.selfLink

        if (selfLink.isNullOrEmpty()) {
            resource.altLink?.trim(slashCharacter)?.let {

                selfLink = MapCompat.getOrDefault(selfLinkLookup, it, null)
            }
        }

        return selfLink
    }

    fun getSelfLink(resourceLocation: ResourceLocation): String? {

        val altLink = resourceLocation.link()

        selfLinkLookup[altLink]?.let {
            if (!it.isEmpty()) {
                return it
            }
        }

        return null
    }

    fun getAltLink(selfLink: String): String? {

        if (selfLink.isNotEmpty()) {

            val altLink = MapCompat.getOrDefault(altLinkLookup, selfLink, null)

            if (altLink?.isEmpty() == false) {

                return altLink
            }
        }

        return null
    }

    fun getSelfLink(altLink: String): String? {

        val trimmedLink = altLink.trim(slashCharacter)

        if (trimmedLink.isNotEmpty()) {

            val selfLink = MapCompat.getOrDefault(selfLinkLookup, trimmedLink, null)

            if (selfLink?.isEmpty() == false) {

                return selfLink
            }
        }

        return null
    }

    fun getResourceId(resource: Resource, selfLink: String? = null): String? {

        var resourceId = resource.resourceId

        if (resourceId.isNullOrEmpty()) {

            (selfLink ?: getSelfLink(resource))?.let {

                val selfLinkSubstring = it.trimEnd(slashCharacter).split(slashCharacter).last()

                resourceId = selfLinkSubstring
            }
        }

        if (resourceId?.isEmpty() == false) {

            return resourceId
        }

        return null
    }

    fun getFilePath(resource: Resource): ResourceFilePath? {

        getSelfLink(resource)?.let { selfLink ->

            getResourceId(resource, selfLink)?.let { resourceId ->

                return ResourceFilePath(directory = selfLink, file = "$resourceId.json")
            }
        }

        return null
    }

    fun getFilePath(resourceLocation: ResourceLocation): ResourceFilePath? {

        if (resourceLocation.isFeed) {
            return null
        }

        getSelfLink(resourceLocation)?.let { selfLink ->

            selfLink.extractId(resourceLocation.type())?.let { resourceId ->

                return ResourceFilePath(directory = selfLink, file = "$resourceId.json")
            }
        }

        return null
    }

    fun getDirectoryPath(resourceLocation: ResourceLocation): String? {

        val selfLink = getSelfLink(resourceLocation) ?: return resourceLocation.type()

        if (resourceLocation.isFeed) {
            return "$selfLink/${resourceLocation.type()}"
        }

        return selfLink
    }

    fun getDirectoryPath(query: Query): String {

        return "queries\\${query.hashCode()}"
    }

    companion object {

        lateinit var shared: ResourceOracle

        fun init(appContext: Context, host: String) {

            shared = ResourceOracle(appContext, host)
        }
    }
}

internal data class ResourceFilePath(val directory: String, val file: String)