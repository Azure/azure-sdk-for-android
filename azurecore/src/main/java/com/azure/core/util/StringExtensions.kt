package com.azure.core.util

import java.net.URLEncoder

/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

fun String.urlEncode(): String {

    return URLEncoder.encode(this, "UTF-8")
}