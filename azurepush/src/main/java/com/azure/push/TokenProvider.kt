package com.azure.push

import com.azure.core.crypto.CryptoProvider
import com.azure.core.crypto.base64Encoded
import java.net.URL
import java.util.Date
import java.net.URLEncoder

/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

internal class TokenProvider(private val connectionParams: ConnectionParams) {

    companion object {
        private const val defaultTokenTTLInSeconds = 1200
    }

    private class Token(val value: String, val obtainedAt: Date, val timeToLiveInSeconds: Int) {
        val isExpired = obtainedAt.time + (timeToLiveInSeconds * 1000) > Date().time
    }

    private var cache = mutableMapOf<URL, Token>()

    internal fun getToken(url: URL): String? {

        cache[url]?.let {

            if (!it.isExpired) {
                return it.value
            }
        }

        return try {
            val expiresOn = (Date().time / 1000) + TokenProvider.defaultTokenTTLInSeconds
            val audienceUri = URLEncoder.encode(url.toString().replace("https", "http"), "UTF-8").toLowerCase()
            val signature = URLEncoder.encode(CryptoProvider.hmacEncrypt("$audienceUri\n$expiresOn", connectionParams.sharedAccessKey.base64Encoded()), "UTF-8")
            val token = "SharedAccessSignature sr=$audienceUri&sig=$signature&se=$expiresOn&skn=${connectionParams.sharedAccessKeyName}"

            cache[url] = Token(token, obtainedAt = Date(), timeToLiveInSeconds = defaultTokenTTLInSeconds)

            token
        } catch (e: Exception) {
            null
        }
    }
}