package com.azure.data.constants

/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

enum class HmacAlgorithm(val value: String) {
    MD5("HmacMD5"),
    SHA1("HmacSHA1"),
    SHA224("HmacSHA224"),
    SHA256 ("HmacSHA256"),
    SHA384("HmacSHA384"),
    SHA512("HmacSHA512");
}