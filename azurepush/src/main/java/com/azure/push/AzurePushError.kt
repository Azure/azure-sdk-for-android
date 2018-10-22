package com.azure.push

/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

class AzurePushError private constructor(message: String) : Error(message) {
    companion object {
        val notConfigured = AzurePushError("AzurePush is not yet configured. AzurePush.configure() must be called before attempting any operation.")
        val reservedTemplateName = AzurePushError("the template name is in conflict with a reserved name")
        val invalidTemplateName = AzurePushError("a template name cannot contain the colon character :")
        val failedToRetrieveAuthorizationToken = AzurePushError("the authorization token could not be retrieved")
        val unexpected = AzurePushError("an unexpected error occurred")

        fun invalidConnectionString(message: String): AzurePushError {
            return AzurePushError(message)
        }
    }
}