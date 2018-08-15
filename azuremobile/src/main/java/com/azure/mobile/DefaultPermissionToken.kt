package com.azure.mobile

import com.azure.data.model.DataError
import com.azure.data.model.Permission
import com.azure.data.model.PermissionMode
import com.azure.data.service.PermissionProvider
import com.azure.data.service.PermissionProviderConfiguration
import com.azure.data.service.PermissionProviderError
import com.azure.data.service.Response
import com.azure.data.util.json.gson
import okhttp3.*
import java.io.IOException
import java.net.URL

/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

data class PermissionRequest(val databaseId: String, val collectionId: String, val documentId: String?, val tokenDuration: Int, val permissionMode: PermissionMode)

class DefaultPermissionProvider(private val baseUrl: HttpUrl, override var configuration: PermissionProviderConfiguration? = PermissionProviderConfiguration.default, private val client: OkHttpClient = OkHttpClient()) : PermissionProvider {

    constructor(baseUrl: URL, configuration: PermissionProviderConfiguration? = PermissionProviderConfiguration.default, client: OkHttpClient = OkHttpClient.Builder().build())
            : this(HttpUrl.get(baseUrl)!!, configuration, client)

    override fun getPermissionForCollection(collectionId: String, databaseId: String, permissionMode: PermissionMode, completion: (Response<Permission>) -> Unit) {

        try {

            val permissionRequest = PermissionRequest(databaseId, collectionId, null, configuration!!.defaultTokenDuration.toInt(), permissionMode)

            val url = baseUrl
                    .newBuilder()
                    .addPathSegment("api/data/permission")
                    .build()

            val request = Request.Builder()
                    .url(url)
                    .addHeader("Content-Type", "application/json")
                    .post(RequestBody.create(MediaType.parse("application/json"), gson.toJson(permissionRequest)))
                    .build()

            client.newCall(request)
                    .enqueue(object : Callback {

                        override fun onFailure(call: Call, ex: IOException) {

                            return completion(Response(DataError(ex), request))
                        }

                        @Throws(IOException::class)
                        override fun onResponse(call: Call, response: okhttp3.Response) {

                            try {
                                val body = response.body()
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

    override fun getPermissionForDocument(documentId: String, collectionId: String, databaseId: String, permissionMode: PermissionMode, completion: (Response<Permission>) -> Unit) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getPermissionForAttachment(attachmentId: String, documentId: String, collectionId: String, databaseId: String, permissionMode: PermissionMode, completion: (Response<Permission>) -> Unit) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getPermissionForStoredProcedure(storedProcedureId: String, collectionId: String, databaseId: String, permissionMode: PermissionMode, completion: (Response<Permission>) -> Unit) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getPermissionForUserDefinedFunction(functionId: String, collectionId: String, databaseId: String, permissionMode: PermissionMode, completion: (Response<Permission>) -> Unit) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getPermissionForTrigger(triggerId: String, collectionId: String, databaseId: String, permissionMode: PermissionMode, completion: (Response<Permission>) -> Unit) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}