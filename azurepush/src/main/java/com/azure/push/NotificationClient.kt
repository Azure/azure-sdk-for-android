package com.azure.push

import android.content.Context
import com.azure.core.http.HttpHeader
import com.azure.core.http.HttpMethod
import com.azure.data.model.DataError
import com.azure.data.model.DocumentClientError
import com.azure.data.model.Result
import com.azure.data.service.Response
import com.azure.data.service.map
import com.azure.data.util.lastPathComponent
import okhttp3.*
import java.io.IOException
import java.net.URL

/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

internal class NotificationClient {

    //region

    companion object {
        private const val apiVersion = "2013-04"
        private const val userAgent = ""

        val shared = NotificationClient()
    }

    //endregion

    //region

    private var isConfigured = false

    private lateinit var path: String

    private lateinit var endpoint: URL

    private lateinit var tokenProvider: TokenProvider

    private lateinit var localStorage: LocalStorage

    //endregion

    //region

    private val client = OkHttpClient()

    private val registrationParser = RegistrationParser()

    //endregion

    //region

    @Throws(AzurePushError::class)
    internal fun configure(context: Context, notificationHubName: String, connectionString: String) {
        val params = ConnectionParams(connectionString)

        this.endpoint = params.endpoint
        this.path = notificationHubName
        this.tokenProvider = TokenProvider(params)
        this.localStorage = LocalStorage(notificationHubName, context)
        this.isConfigured = true
    }

    internal fun registerForRemoteNotifications(deviceToken: String, tags: List<String> = listOf(), completion: (Response<Registration>) -> Unit) {
        if (!isConfigured) {
            completion(Response(DataError(AzurePushError.notConfigured)))
            return
        }

        registerForRemoteNotifications(deviceToken, Registration.defaultName, Registration.payload(deviceToken, tags), completion)
    }

    internal fun registerFormRemoteNotifications(deviceToken: String, template: Registration.Template, priority: String? = null, tags: List<String> = listOf(), completion: (Response<Registration>) -> Unit) {
        if (!isConfigured) {
            completion(Response(DataError(AzurePushError.notConfigured)))
            return
        }

        val error = Registration.validateTemplateName(template.name)
        if (error != null) {
            completion(Response(DataError(error)))
            return
        }

        registerForRemoteNotifications(deviceToken, template.name, Registration.payload(deviceToken, template, priority, tags), completion)
    }

    internal fun cancelRegistration(registration: Registration, completion: (Response<String>) -> Unit) {
        if (!isConfigured) {
            completion(Response(DataError(AzurePushError.notConfigured)))
            return
        }

        delete(registration, completion)
    }

    internal fun cancelAllRegistrations(deviceToken: String, completion: (Response<String>) -> Unit) {
        if (!isConfigured) {
            completion(Response(DataError(AzurePushError.notConfigured)))
            return
        }

        registrations(deviceToken) {
            when {
                it.isErrored -> completion(it.map { "" })
                it.isSuccessful -> {
                    val registrations = it.resource!!.toMutableList()
                    delete(registrations, completion)
                }
            }
        }
    }

    //endregion

    //region

    private fun registerForRemoteNotifications(deviceToken: String, name: String, payload: String, completion: (Response<Registration>) -> Unit) {
        if (!localStorage.needsRefresh) {
            createOrUpdate(name, payload, completion)
            return
        }

        val refreshedDeviceToken = this.refreshedDeviceToken(newDeviceToken = deviceToken)
        registrations(refreshedDeviceToken) {
            when {
                it.isSuccessful -> {
                    localStorage.refresh(deviceToken)
                    createOrUpdate(name, payload, completion)
                }

                it.isErrored -> {
                    it.error?.let { completion(Response(it)) }
                }

                else -> {
                    completion(Response(DataError(AzurePushError.unexpected)))
                }
            }
        }
    }

    private fun createOrUpdate(registrationName: String, payload: String, completion: (Response<Registration>) -> Unit) {
        val registration = localStorage[registrationName]

        if (registration == null) {
            createAndUpsert(registrationName, payload, completion)
            return
        }

        upsert(registration.id, registrationName, payload) {
            when {
                it.response?.code() == 410 -> { // GONE
                    createAndUpsert(registrationName, payload, completion)
                }

                else -> {
                    completion(it)
                }
            }
        }
    }

    private fun createAndUpsert(registrationName: String, payload: String, completion: (Response<Registration>) -> Unit) {
        val url = URL("$endpoint$path/registrationids/?api-version=${NotificationClient.apiVersion}")

        sendRequest(url, HttpMethod.Post, payload) {
            val registrationId = it.response?.header(HttpHeader.Location.name)?.lastPathComponent()

            if (registrationId == null) {
                completion(Response(DataError(AzurePushError.unexpected)))
                return@sendRequest
            }

            upsert(registrationId, registrationName, payload, completion)
        }
    }

    private fun upsert(registrationId: String, name: String, payload: String, completion: (Response<Registration>) -> Unit) {
        val url = URL("$endpoint$path/Registrations/$registrationId?api-version=${NotificationClient.apiVersion}")

        sendRequest(url, HttpMethod.Put, payload) {
            if (it.isSuccessful) {
                this.localStorage[name] = this.registrationParser.parse(it.resource!!).first()
            }

            completion(it.map { this.registrationParser.parse(it).first() })
        }
    }

    private fun registrations(deviceToken: String, completion: (Response<List<Registration>>) -> Unit) {
        val url = URL("$endpoint$path/Registrations/?filter=deviceToken+eq+'$deviceToken'&api-version=${NotificationClient.apiVersion}")

        sendRequest(url, HttpMethod.Get) {
            completion(it.map {
                this.registrationParser.parse(it)
            })
        }
    }

    private fun delete(registration: Registration, completion: (Response<String>) -> Unit) {
        val url = URL("$endpoint$path/Registrations/${registration.id}?api-version=${NotificationClient.apiVersion}")

        sendRequest(url, HttpMethod.Delete, etag = "*") {
            when {
                it.isSuccessful -> {
                    localStorage.remove(registration.name)
                    completion(it)
                }

                else -> {
                    completion(it)
                }
            }
        }
    }

    private fun delete(registrations: MutableList<Registration>, completion: (Response<String>) -> Unit) {
        if (registrations.isEmpty()) {
            completion(Response(""))
            return
        }

        var regs = registrations

        delete(regs.removeAt(0)) {
            when {
                it.isErrored -> completion(it)

                it.isSuccessful -> {
                    delete(regs, completion)
                }

                else -> {
                    completion(Response(DataError(AzurePushError.unexpected)))
                }
            }
        }
    }

    //endregion

    //region

    private fun sendRequest(url: URL, method: HttpMethod, payload: String? = null, etag: String? = null, completion: (Response<String>) -> Unit) {
        val authToken = tokenProvider.getToken(url)

        if (authToken == null) {
            completion(Response(DataError(AzurePushError.failedToRetrieveAuthorizationToken)))
            return
        }

        val builder = Request.Builder()
                .url(url)
                .method(method.name, requestBody(payload))
                .header(HttpHeader.Authorization.name, authToken)
                .header(HttpHeader.UserAgent.name, NotificationClient.userAgent)

        if (etag != null) {
            builder.header(HttpHeader.ETag.name, etag)
        }

        val request = builder.build()

        try {
            client.newCall(request)
                    .enqueue(object : Callback {
                        override fun onResponse(call: Call?, response: okhttp3.Response?) {
                            if (response == null) {
                                completion(Response(DataError(AzurePushError.unexpected)))
                                return
                            }

                            val responseData = response.body()?.string()

                            when {
                                response.isSuccessful -> {
                                    if (responseData == null) {
                                        completion(Response(DataError("No response body received"), request, response))
                                        return
                                    }

                                    completion(Response(request, response, responseData, Result(responseData)))
                                }

                                else -> {
                                    completion(Response(DataError(AzurePushError.unexpected), request, response, responseData))
                                }
                            }
                        }

                        override fun onFailure(call: Call?, e: IOException?) {
                            completion(Response(DataError(DocumentClientError.InternetConnectivityError), request))
                        }
                    })
        } catch (ex: Exception) {
            completion(Response(DataError(ex), request))
        }
    }

    private fun refreshedDeviceToken(newDeviceToken: String): String {
        return localStorage.deviceToken ?: return newDeviceToken
    }

    private fun requestBody(payload: String?): RequestBody? {
        payload?.let {
            val contentType = if (it.startsWith("{")) "application/json" else "application/xml"
            return RequestBody.create(MediaType.parse(contentType), it)
        }

        return null
    }

    //endregion
}