package com.azure.data.model

import okhttp3.HttpUrl

/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

data class UrlLink(val url: HttpUrl, val link: String)