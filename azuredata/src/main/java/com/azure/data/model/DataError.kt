package com.azure.data.model

/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

class DataError(message: String?, val code: String? = null) : Error(message) {

    constructor(serverError: ServerError) : this(serverError.message, serverError.code)

    constructor(error: Error) : this(error.message)

    constructor(error: Exception) : this(error.message)

    constructor() : this("")

    override fun toString(): String =
            "Error: ${if (code != null) "\n\tCode: $code" else ""} \n\tMessage: $message"

    fun isConnectivityError(): Boolean = this.message.equals(DocumentClientError.InternetConnectivityError.message)
}

// intermediary class used to deserialize DataErrors
data class ServerError (val message: String?, val code: String? = null)