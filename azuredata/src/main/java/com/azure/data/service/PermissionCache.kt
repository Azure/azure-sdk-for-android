package com.azure.data.service

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import com.azure.data.model.Permission
import com.azure.data.model.Resource
import com.azure.data.util.ResourceOracle
import com.azure.data.util.json.gson

/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

@SuppressLint("CommitPrefEdits")
class PermissionCache private constructor (host: String) {

    private val permissionCacheStorageKey = "com.azure.data.permissioncache$host"
    private val slashCharacter = '/'

    private lateinit var cache: MutableMap<String, Permission>

    private lateinit var permissionCachePrefsEditor: SharedPreferences.Editor

    var isRestored: Boolean = false

    private fun commit() {

        permissionCachePrefsEditor.apply()
    }

    fun restore(appCtx: Context) {

        val permissionCachePrefs = appCtx.getSharedPreferences(permissionCacheStorageKey, Context.MODE_PRIVATE)

        //copy prefs to cache
        cache = permissionCachePrefs.all.mapValues { gson.fromJson(it.value.toString(), Permission::class.java) }.toMutableMap()

        permissionCachePrefsEditor = permissionCachePrefs.edit()

        isRestored = true
    }

    fun purge() {

        cache = mutableMapOf()
        permissionCachePrefsEditor.clear()
        commit()
    }

    fun getPermission(resource: Resource): Permission? {

        val altLink = ResourceOracle.shared.getAltLink(resource)

        return altLink?.let {
            return cache[altLink]
        }
    }

    fun getPermission(altLink: String): Permission? {

        return if (altLink.trim(slashCharacter).isNotEmpty()) {
            cache[altLink]
        } else {
            null
        }
    }

    fun setPermission(permission: Permission, resource: Resource): Boolean {

        val altLink = ResourceOracle.shared.getAltLink(resource)

        return altLink?.let {
            setPermission(permission, it)
        } ?: false
    }

    fun setPermission(permission: Permission, altLink: String): Boolean {

        return if (altLink.trim(slashCharacter).isNotEmpty()) {
            cache[altLink] = permission
            permissionCachePrefsEditor.putString(altLink, gson.toJson(permission))
            commit()
            true
        } else {
            false
        }
    }

    companion object {

        lateinit var shared: PermissionCache

        fun init(host: String) {

            shared = PermissionCache(host)
        }
    }
}