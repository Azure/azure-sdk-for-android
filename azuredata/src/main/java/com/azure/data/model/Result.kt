package com.azure.data.model

/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

class Result<out T>(val resource: T? = null, val error: DataError? = null) {

    constructor(error: DataError) : this(null, error)

    companion object {

        val empty = Result<Unit>()
    }
}