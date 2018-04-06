package com.azure.data.service

import com.azure.core.crypto.CryptoProvider
import com.azure.core.http.HttpMethod
import com.azure.core.log.d
import com.azure.data.constants.TokenType
import com.azure.data.model.ResourceType
import com.azure.data.model.Token
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.*

/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

class TokenProvider(private var key: String, private var keyType: TokenType = TokenType.MASTER, private var tokenVersion: String = "1.0") {

    private val dateFormatter : SimpleDateFormat = SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss", Locale.ROOT)

    init {
        dateFormatter.timeZone = TimeZone.getTimeZone("GMT")
    }

    // https://docs.microsoft.com/en-us/rest/api/documentdb/access-control-on-documentdb-resources#constructkeytoken
    fun getToken(verb: HttpMethod, resourceType: ResourceType, resourceLink: String) : Token {

        val dateString = String.format("%s %s", dateFormatter.format(Date()), "GMT")

        val payload = String.format("%s\n%s\n%s\n%s\n\n",
                verb.name.toLowerCase(Locale.ROOT),
                resourceType.path.toLowerCase(Locale.ROOT),
                resourceLink,
                dateString.toLowerCase(Locale.ROOT))
                .also { d{it} }

        val signature = CryptoProvider.hmacEncrypt(payload, key)

        val authStringEncoded = URLEncoder.encode(String.format("type=%s&ver=%s&sig=%s", keyType, tokenVersion, signature), "UTF-8")

        return Token(authStringEncoded, dateString)
    }
}