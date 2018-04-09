package com.azure.data.model

/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

enum class ErrorType(val message: String) {
    NetworkUnavailable("The network is not availble at this time"),
    SetupError("AzureData is not setup.  Must call AzureData.setup() before attempting CRUD operations on resources."),
    InvalidId("Cosmos DB Resource IDs must not exceed 255 characters and cannot contain whitespace"),
    IncompleteIds("This resource is missing the selfLink and/or resourceId properties.  Use an override that takes parent resource or ids instead"),
    UnknownError("An unknown error occured."),
    InternalError("An internal error occured.")
}