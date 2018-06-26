package com.azure.data.util

import com.azure.data.service.Response

fun <T> Response<T>.is404(): Boolean {
    return this.response?.code() == 404
}