package com.azure.data.model

/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

class DataError(val message: String?, val code: String? = null) {

    constructor(error: Error) : this(error.message)

    constructor(error: Exception) : this(error.message)

    constructor(errorType: ErrorType) : this(errorType.message)

    constructor() : this("")

    override fun toString(): String =
            "\r\nError\r\n\t$message\r\n${if (code != null) "\t$code\r\n" else ""}"

    companion object {

        fun fromType(errorType: ErrorType) : DataError = DataError(errorType)
    }
}