package com.azure.data.constants

/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

class HttpHeaderValue {

    companion object {

        // https://docs.microsoft.com/en-us/rest/api/documentdb/#supported-rest-api-versions
        const val apiVersion = "2018-12-31"

        const val trueValue = "True"

        const val minDatabaseThroughput = 400

        const val maxDatabaseThroughput = 250000

        const val databaseThroughputStep = 100

        const val noCache = "no-cache"
    }
}