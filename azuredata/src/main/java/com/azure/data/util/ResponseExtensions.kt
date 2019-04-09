package com.azure.data.util

import com.azure.core.http.HttpStatusCode
import com.azure.data.model.service.Response

fun <T> Response<T>.is404(): Boolean {
    return this.response?.code() == HttpStatusCode.NotFound.code
}