package com.azure.core.http

/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

enum class HttpMethod {

    Get,
    Head,
    Post,
    Put,
    Delete,
    Options;

    fun isRead() : Boolean = this == Get || this == Head

    fun isWrite() : Boolean = !isRead()

    override fun toString(): String {
        return when (this) {
            Get     -> "GET"
            Head    -> "HEAD"
            Post    -> "POST"
            Put     -> "PUT"
            Delete  -> "DELETE"
            Options -> "OPTIONS"
        }
    }
}