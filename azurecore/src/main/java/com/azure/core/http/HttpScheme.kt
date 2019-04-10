package com.azure.core.http

/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

enum class HttpScheme {

    Http,
    Https;

    override fun toString(): String {

        return this.name.toLowerCase()
    }
}