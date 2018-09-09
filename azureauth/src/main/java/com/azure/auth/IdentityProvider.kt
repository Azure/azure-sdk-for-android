package com.azure.auth

import com.azure.data.util.json.gson

/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

sealed class IdentityProvider {
    class Aad(val accessToken: String): IdentityProvider()
    class Microsoft: IdentityProvider()
    class Facebook(val tokenString: String): IdentityProvider()
    class Google(val idToken: String, val serverAuthCode: String): IdentityProvider()
    class Twitter(val authToken: String, val authTokenSecret: String): IdentityProvider()

    val name: String
        get() = when (this) {
            is Aad -> "AAD"
            is Microsoft -> "microsoft"
            is Facebook -> "facebook"
            is Google -> "google"
            is Twitter -> "twitter"
        }

    val displayName: String
        get() = when (this) {
            is Aad -> "AAD"
            is Microsoft -> "Microsoft"
            is Facebook -> "Facebook"
            is Google -> "Google"
            is Twitter -> "Twitter"
        }

    val tokenPath: String = ".auth/login/$name"

    val payloadDict: HashMap<String, String>
        get() = when (this) {
            is Aad -> hashMapOf("access_token" to this.accessToken)
            is Microsoft -> hashMapOf()
            is Facebook -> hashMapOf("access_token" to this.tokenString)
            is Google -> hashMapOf("id_token" to this.idToken, "authorization_code" to this.serverAuthCode)
            is Twitter -> hashMapOf("access_token" to this.authToken, "access_token_secret" to authTokenSecret)
        }

    val payload: String = gson.toJson(payloadDict)

    val secureStorageKey: String
        get() = "authprovider.$name"

    companion object {
        val refreshPath: String = ".auth/refresh"
    }
}
