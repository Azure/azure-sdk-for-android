package com.azure.auth

import com.azure.data.service.Response
import okhttp3.Request

/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

class AzureAuth {
    companion object {
        val user = AuthClient.shared.user

        fun authHeader(): Pair<String, String> {
            return AuthClient.shared.authHeader()
        }

        fun login(urlString: String, provider: IdentityProvider, callback: (Response<AuthUser>) -> Unit) {
            AuthClient.shared.login(urlString, provider, callback)
        }

        fun refresh(urlString: String, callback: (Response<AuthUser>) -> Unit) {
            AuthClient.shared.refresh(urlString, callback)
        }
    }

    fun Request.Builder.authHeader() {
        val header = AuthClient.shared.authHeader()
        this.header(header.first, header.second)
    }
}