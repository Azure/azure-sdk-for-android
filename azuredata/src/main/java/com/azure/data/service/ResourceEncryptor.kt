package com.azure.data.service

/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

interface ResourceEncryptor {
    fun encrypt(data: String): String
    fun decrypt(data: String): String
}