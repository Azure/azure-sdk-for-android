package com.azure.data.integration.offlinetests.mocks

import okhttp3.Call
import okhttp3.OkHttpClient
import okhttp3.Request

class MockOkHttpClient: OkHttpClient() {

    var response: okhttp3.Response? = null
    var hasNetworkError = false

    override fun newCall(request: Request): Call {

        return MockCall(request, this)
    }
}