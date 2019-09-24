package com.azure.mobile

import com.azure.data.model.service.DataError
import com.azure.data.model.Permission
import com.azure.data.model.PermissionMode
import com.azure.data.model.service.ResourceType
import com.azure.data.service.getSelfLink
import com.azure.data.service.PermissionProvider
import com.azure.data.service.PermissionProviderConfiguration
import com.azure.data.model.service.PermissionProviderError
import com.azure.data.model.service.Response
import com.azure.data.util.json.gson
import okhttp3.*
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.net.URL

/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

data class PermissionRequest(
        val databaseId: String,
        val collectionId: String,
        val documentId: String?,
        val resourceLink: String?,
        val tokenDuration: Int,
        val permissionMode: PermissionMode
)

class DefaultPermissionProvider(private val baseUrl: HttpUrl, override var configuration: PermissionProviderConfiguration? = PermissionProviderConfiguration.default, private val client: OkHttpClient = OkHttpClient()) : PermissionProvider {

    constructor(baseUrl: URL, configuration: PermissionProviderConfiguration? = PermissionProviderConfiguration.default, client: OkHttpClient = OkHttpClient.Builder().build())
            : this(baseUrl.toHttpUrlOrNull()!!, configuration, client)

    override fun getPermissionForCollection(collectionId: String, databaseId: String, permissionMode: PermissionMode, completion: (Response<Permission>) -> Unit) {
        val permissionRequest = PermissionRequest(databaseId, collectionId, null, null, configuration!!.defaultTokenDuration.toInt(), permissionMode)
        getPermission(permissionRequest, completion)
    }

    override fun getPermissionForDocument(documentId: String, collectionId: String, databaseId: String, permissionMode: PermissionMode, completion: (Response<Permission>) -> Unit) {
        val altLink = "${ResourceType.Database.path}/$databaseId/${ResourceType.Collection.path}/$collectionId/${ResourceType.Document.path}/$documentId"
        getPermissionForResource(databaseId, collectionId, documentId, altLink, permissionMode, completion)
    }

    override fun getPermissionForAttachment(attachmentId: String, documentId: String, collectionId: String, databaseId: String, permissionMode: PermissionMode, completion: (Response<Permission>) -> Unit) {
        val altLink = "${ResourceType.Database.path}/$databaseId/${ResourceType.Collection.path}/$collectionId/${ResourceType.Document.path}/$documentId/${ResourceType.Attachment.path}/$attachmentId"
        getPermissionForResource(databaseId, collectionId, documentId, altLink, permissionMode, completion)
    }

    override fun getPermissionForStoredProcedure(storedProcedureId: String, collectionId: String, databaseId: String, permissionMode: PermissionMode, completion: (Response<Permission>) -> Unit) {
        val altLink = "${ResourceType.Database.path}/$databaseId/${ResourceType.Collection.path}/$collectionId/${ResourceType.StoredProcedure.path}/$storedProcedureId"
        getPermissionForResource(databaseId, collectionId, null, altLink, permissionMode, completion)
    }

    override fun getPermissionForUserDefinedFunction(functionId: String, collectionId: String, databaseId: String, permissionMode: PermissionMode, completion: (Response<Permission>) -> Unit) {
        val altLink = "${ResourceType.Database.path}/$databaseId/${ResourceType.Collection.path}/$collectionId/${ResourceType.Udf.path}/$functionId"
        getPermissionForResource(databaseId, collectionId, null, altLink, permissionMode, completion)
    }

    override fun getPermissionForTrigger(triggerId: String, collectionId: String, databaseId: String, permissionMode: PermissionMode, completion: (Response<Permission>) -> Unit) {
        val altLink = "${ResourceType.Database.path}/$databaseId/${ResourceType.Collection.path}/$collectionId/${ResourceType.Trigger.path}/$triggerId"
        getPermissionForResource(databaseId, collectionId, null, altLink, permissionMode, completion)
    }

    private fun getPermissionForResource(databaseId: String, collectionId: String, documentId: String? = null, altLink: String, permissionMode: PermissionMode, completion: (Response<Permission>) -> Unit) {
        val resourceLink = getSelfLink(altLink) ?: return completion(Response(DataError(PermissionProviderError.GetPermissionFailed)))
        val permissionRequest = PermissionRequest(databaseId, collectionId, documentId, resourceLink, configuration!!.defaultTokenDuration.toInt(), permissionMode)

        getPermission(permissionRequest, completion)
    }

    private fun getPermission(permissionRequest: PermissionRequest, completion: (Response<Permission>) -> Unit) {
        try {
            val url = baseUrl
                    .newBuilder()
                    .addPathSegment("api/data/permission")
                    .build()

            val request = Request.Builder()
                    .url(url)
                    .addHeader("Content-Type", "application/json")
                    .post(gson.toJson(permissionRequest).toRequestBody("application/json".toMediaTypeOrNull()))
                    .build()

            client.newCall(request)
                    .enqueue(object : Callback {

                        override fun onFailure(call: Call, ex: IOException) {

                            return completion(Response(DataError(ex), request))
                        }

                        @Throws(IOException::class)
                        override fun onResponse(call: Call, response: okhttp3.Response) {

                            try {
                                val body = response.body
                                        ?: return completion(Response(DataError(PermissionProviderError.GetPermissionFailed)))

                                val json = body.string()
                                val permission = gson.fromJson(json, Permission::class.java)

                                return completion(Response(permission))

                            } catch (ex: Exception) {

                                return completion(Response(DataError(ex), request, response))
                            }
                        }
                    })

        } catch (ex: Exception) {
            return completion(Response(DataError(PermissionProviderError.GetPermissionFailed)))
        }
    }
}