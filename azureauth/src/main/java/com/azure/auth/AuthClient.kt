package com.azure.auth

import android.content.Context
import com.azure.core.http.HttpScheme
import com.azure.core.log.d
import com.azure.core.log.e
import com.azure.core.util.ContextProvider.Companion.appContext
import com.azure.data.model.DataError
import com.azure.data.model.Result
import com.azure.data.service.Response
import com.azure.data.util.json.gson
import com.azure.data.util.toError
import okhttp3.*
import java.io.IOException

/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

internal class AuthClient {

    private val authClientCacheKey = "com.azure.auth.client"

    private val client = OkHttpClient()

    private var authClientCacheEditor = appContext.getSharedPreferences(authClientCacheKey, Context.MODE_PRIVATE).edit()

    internal var user: AuthUser? = null

    internal fun authHeader(): Pair<String, String> {
        user?.let { return Pair("X-ZUMO-AUTH", it.authenticationToken) }

        throw AuthClientError.noCurrentUser
    }

    internal fun login(urlString: String, provider: IdentityProvider, callback: (Response<AuthUser>) -> Unit) {
        val url = HttpUrl.Builder()
                .scheme(HttpScheme.Https.toString())
                .host(urlString)
                .addPathSegment(provider.tokenPath)
                .build()

        val body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), provider.payload)

        val request = Request.Builder()
                .url(url)
                .post(body)
                .build()

        sendRequest(request, callback)
    }

    internal fun refresh(urlString: String, callback: (Response<AuthUser>) -> Unit) {
        val url = HttpUrl.Builder()
                .scheme(HttpScheme.Https.toString())
                .host(urlString)
                .addPathSegment(IdentityProvider.refreshPath)
                .build()

        val header = authHeader()

        val request = Request.Builder()
                .url(url)
                .header(header.first, header.second)
                .build()

        sendRequest(request, callback)
    }

    private fun sendRequest(request: Request, callback: (Response<AuthUser>) -> Unit) {
        try {
            client.newCall(request)
                    .enqueue(object: Callback {
                        override fun onResponse(call: Call?, response: okhttp3.Response?) {
                            callback(processResponse(request, response))
                        }

                        override fun onFailure(call: Call?, e: IOException?) {
                            if (e == null) {
                                callback(Response(DataError(AuthClientError.unknown)))
                                return
                            }

                            callback(Response(DataError(e), request))
                        }
                    })
        } catch (ex: Exception) {
            e(ex)
            callback(Response(DataError(ex), request))
        }
    }

    private fun processResponse(request: Request, response: okhttp3.Response?): Response<AuthUser> {
        try {
            val body = response?.body() ?: return Response(DataError(AuthClientError.expectedBodyWithResponse))
            val json = body.string().also { d{ it } }

            return when {
                response.isSuccessful -> {
                    val authUser = gson.fromJson<AuthUser>(json, AuthUser::class.java)
                    this.user = authUser

                    authClientCacheEditor.putString("authuser", json)
                    authClientCacheEditor.apply()

                    Response(request, response, json, Result(authUser))
                }

                else -> Response(json.toError(), request, response, json)
            }
        } catch (ex: Exception) {
            return Response(DataError(ex), request, response)
        }
    }

    companion object {
        internal val shared = AuthClient()
    }
}