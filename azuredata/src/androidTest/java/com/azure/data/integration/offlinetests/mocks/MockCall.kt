package com.azure.data.integration.offlinetests.mocks

import okhttp3.Call
import okhttp3.Callback
import okhttp3.Request
import okhttp3.Response
import okio.Timeout
import java.io.IOException

class MockCall(private val originalRequest: Request, private val client: MockOkHttpClient): Call {

    private var isExecuted = false
    private var isCanceled = false

    override fun request(): Request {
        return originalRequest
    }

    override fun execute(): Response {
        return client.response!!
    }

    override fun enqueue(responseCallback: Callback) {
        isCanceled = false

        if (client.response != null) {
            responseCallback.onResponse(this, client.response!!)
            return
        }

        if (client.hasNetworkError) {
            responseCallback.onFailure(this, IOException())
            return
        }

        isExecuted = true
    }

    override fun cancel() {
        isCanceled = true
    }

    override fun isExecuted(): Boolean {
        return isExecuted
    }

    override fun isCanceled(): Boolean {
        return isCanceled
    }

    override fun clone(): Call {
        return MockCall(this.originalRequest, this.client)
    }

    override fun timeout(): Timeout {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}