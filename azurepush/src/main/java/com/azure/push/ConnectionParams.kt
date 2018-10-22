package com.azure.push

import java.net.URL

/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

internal class ConnectionParams(connectionString: String) {
    private var params = mutableMapOf<String, Any>()

    //region

    internal val endpoint: URL = params[ConnectionParams.endpoint] as URL

    internal val sharedAccessKeyName: String = params[ConnectionParams.sharedAccessKeyName] as String

    internal val sharedAccessKey: String = params[ConnectionParams.sharedAccessKey] as String

    //endregion

    companion object {
        private const val endpoint = "Endpoint"
        private const val sharedAccessKeyName = "SharedAccessKeyName"
        private const val sharedAccessKey = "SharedAccessKey"
    }

    init {
        val components = connectionString.split(";")
        val keyValuePairs = components
                .map { it.split("=", limit = 1) }
                .filter { it.size == 2 }
                .map { Pair(it[0], it[1]) }

        for (keyValue in keyValuePairs) {
            val (key, value) = keyValue
            when (key) {
                ConnectionParams.endpoint    -> params[ConnectionParams.endpoint] = URL(replaceScheme(value, newScheme = "https"))
                else                         -> params[key] = value
            }
        }

        validateParams()
    }

    //region

    private fun replaceScheme(urlString: String, newScheme: String): String {
        val previousScheme = urlString.split(":", limit = 1).firstOrNull() ?: return "$newScheme://$this"
        val result = urlString.replace(previousScheme, newScheme)

        if (!result.endsWith("/")) {
            return "$result/"
        }

        return result
    }

    @Throws(AzurePushError::class)
    private fun validateParams() {
        if (params[ConnectionParams.endpoint] == null) {
            throw AzurePushError.invalidConnectionString("the endpoint is missing or is in an invalid format in the connection string")
        }

        if (params[ConnectionParams.sharedAccessKey] == null || params[ConnectionParams.sharedAccessKeyName] == null) {
            throw AzurePushError.invalidConnectionString("the security information is missing in the connection string")
        }
    }

    //endregion
}