package com.azure.data.model

/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

class DataError(message: String?, val code: String? = null) : Error(message) {

    constructor(error: Error) : this(error.message)

    constructor(error: Exception) : this(error.message)

    constructor() : this("")

    override fun toString(): String =
            "\r\nError\r\n\t$message\r\n${if (code != null) "\t$code\r\n" else ""}"

    fun isConnectivityError(): Boolean = this.message.equals(DocumentClientError.InternetConnectivityError.message)
}