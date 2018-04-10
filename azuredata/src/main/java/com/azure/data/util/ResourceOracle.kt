package com.azure.data.util

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import com.azure.data.model.Resource
import com.azure.data.model.ResourceBase
import com.azure.data.model.ResourceList

/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */
@SuppressLint("CommitPrefEdits")
internal class ResourceOracle private constructor (appContext: Context) {

    private val altLinkLookupStorageKey = "com.azure.data.lookup.altlink"
    private val selfLinkLookupStorageKey = "com.azure.data.lookup.selflink"

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

        resource.selfLink?.let { selfLink ->
            resource.altLink?.let { altLink ->

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

    private fun doRemoveLinks(resource: Resource) {

        getSelfLink(resource)?.let {

            altLinkLookup.remove(it)
            altLinkPrefsEditor.remove(it)
        }

        getAltLink(resource)?.let {

            selfLinkLookup.remove(it)
            altLinkPrefsEditor.remove(it)
        }
    }

    fun removeLinks(resource: Resource, commit: Boolean = true) {

        doRemoveLinks(resource)

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

                altLink = altLinkLookup.getOrDefault(it, null)
            }
        }

        return altLink
    }

    fun getSelfLink(resource: Resource): String? {

        var selfLink = resource.selfLink

        if (selfLink.isNullOrEmpty()) {
            resource.altLink?.trim(slashCharacter)?.let {

                selfLink = selfLinkLookup.getOrDefault(it, null)
            }
        }

        return selfLink
    }

    fun getAltLink(selfLink: String): String? {

        if (selfLink.isNotEmpty()) {

            val altLink = altLinkLookup.getOrDefault(selfLink, null)

            if (altLink?.isEmpty() == false) {

                return altLink
            }
        }

        return null
    }

    fun getSelfLink(altLink: String): String? {

        val trimmedLink = altLink.trim(slashCharacter)

        if (trimmedLink.isNotEmpty()) {

            val selfLink = selfLinkLookup.getOrDefault(trimmedLink, null)

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

    fun getFilePath(resource: Resource): String? {

        val selfLink = getSelfLink(resource)
        val resourceId = getResourceId(resource, selfLink)

        resourceId?.let {

            return "$selfLink/$it.json"
        }

        return null
    }

    companion object {

        val shared: ResourceOracle = ResourceOracle(ContextProvider.appContext)
    }
}