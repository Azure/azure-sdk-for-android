package com.azure.auth

/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

class AuthClientError private constructor(message: String) : Error(message) {
    companion object {
        val unknown = AuthClientError("Unknown")
        val expectedToken = AuthClientError("No token was provided.")
        val invalidToken = AuthClientError("The token provided was invalid.")
        val expectedBodyWithResponse = AuthClientError("The server did not return any data.")
        val invalidResponseSyntax = AuthClientError("The token in the login response was invalid. The token must be a JSON object with both a userId and an authenticationToken.")
        val noCurrentUser = AuthClientError("No current user set.  Must call login() before requesting using the authHeader property.")
    }
}