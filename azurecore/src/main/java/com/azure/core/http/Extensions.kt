package com.azure.core.http

import okhttp3.MediaType
import okhttp3.Request
import okhttp3.RequestBody

/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

fun ByteArray?.toRequestBody(mediaType: MediaType?): RequestBody? {

    return this?.let {

        RequestBody.create(mediaType, it)
    }
}

fun HttpMethod.toBuilder(body: RequestBody? = null): Request.Builder {

    return when (this) {

        HttpMethod.Get -> Request.Builder().get()
        HttpMethod.Post -> Request.Builder().post(body!!)
        HttpMethod.Put -> Request.Builder().put(body!!)
        HttpMethod.Delete -> Request.Builder().delete()
        HttpMethod.Head -> Request.Builder().head()
    }
}

fun Request.Builder.withMethod(method: HttpMethod, body: RequestBody? = null): Request.Builder {

    return when (method) {

        HttpMethod.Get -> this.get()
        HttpMethod.Post -> this.post(body!!)
        HttpMethod.Put -> this.put(body!!)
        HttpMethod.Delete -> this.delete()
        HttpMethod.Head -> this.head()
    }
}