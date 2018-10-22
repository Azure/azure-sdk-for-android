package com.azure.push

/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

import android.content.Context
import com.azure.data.util.json.gson

internal class LocalStorage(notificationHubPath: String, context: Context) {
    companion object {
        private const val version = "v1.0.0"
    }

    //region

    private val context: Context = context

    private val preferencesKey = notificationHubPath

    private val deviceTokenKey = "$notificationHubPath-deviceTokenKey"

    private val versionKey = "$notificationHubPath-versionKey"

    private val registrationsKey = "$notificationHubPath-registrations"

    private var container: MutableMap<String, Registration> = mutableMapOf()

    //endregion

    //region

    var needsRefresh = false

    var deviceToken: String? = null

    init {
        load()
    }

    internal operator fun get(key: String): Registration? {
        return container[key]
    }

    internal operator fun set(key: String, registration: Registration) {
        container[key] = registration
        sync()
    }

    internal fun refresh(deviceToken: String) {
        needsRefresh = false

        if (deviceToken != this.deviceToken) {
            this.deviceToken = deviceToken
            sync()
        }
    }

    internal fun remove(name: String): Registration? {
        return container.remove(name)
    }

    internal fun clear() {
        container.clear()
        sync()
    }

    //endregion

    //region

    private fun load() {
        val preferences = context.getSharedPreferences(preferencesKey, Context.MODE_PRIVATE)

        this.deviceToken = preferences.getString(deviceTokenKey, null)

        val version = preferences.getString(versionKey, null)
        if (version == null || version != LocalStorage.version) {
            this.needsRefresh = true
            return
        }

        val registrations = preferences.getString(registrationsKey, null)
        registrations?.split(";")?.map { gson.fromJson(it, Registration::class.java) }?.forEach {
            container[it.name] = it
        }
    }

    private fun sync() {
        val preferencesEditor = context.getSharedPreferences(preferencesKey, Context.MODE_PRIVATE).edit()
        preferencesEditor.putString(deviceTokenKey, deviceToken)
        preferencesEditor.putString(versionKey, version)
        preferencesEditor.putString(registrationsKey, container.values.joinToString(";") { gson.toJson(it) })

        preferencesEditor.apply()
    }

    //endregion
}