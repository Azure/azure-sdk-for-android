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
    Delete;

    fun isRead() : Boolean = this == Get || this == Head

    fun isWrite() : Boolean = !isRead()
}