package com.azure.data.constants

/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

class HttpHeaderValue {

    companion object {

        // https://docs.microsoft.com/en-us/rest/api/documentdb/#supported-rest-api-versions
        const val apiVersion = "2017-02-22"

        const val trueValue = "true"

        const val minDatabaseThroughput = 400

        const val maxDatabaseThroughput = 250000

        const val databaseThroughputStep = 100
    }
}