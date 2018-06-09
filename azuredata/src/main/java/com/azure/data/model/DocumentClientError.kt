package com.azure.data.model

/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

class DocumentClientError private constructor(msg: String) : Error(msg) {

    companion object {

        val UnknownError = DocumentClientError("An unknown error occured.")
        val InternalError = DocumentClientError("An internal error occured.")
        val ConfigureError = DocumentClientError("AzureData is not configured.  Must call AzureData.configure() before attempting CRUD operations on resources.")
        val InvalidId = DocumentClientError("Cosmos DB Resource IDs must not exceed 255 characters and cannot contain whitespace")
        val IncompleteIds = DocumentClientError("This resource is missing the selfLink and/or resourceId properties.  Use an override that takes parent resource or ids instead")
        val PermissionError = DocumentClientError("Configuring AzureData using a PermissionProvider implements access control based on resource-specific Permissions. This authorization model only supports accessing application resources (Collections, Stored Procedures, Triggers, UDFs, Documents, and Attachments). In order to access administrative resources (Database Accounts, Databases, Users, Permission, and Offers) require AzureData is configured using a master key.")
        val NoMoreResultsError = DocumentClientError("Response.next() has been called but there are no more results to fetch. Must check that Response.hasMoreResults is true before calling Response.next().")
        val InvalidMaxPerPageError = DocumentClientError("The maxPerPage parameter must be between 1 and 1000 inclusive")
        val NextCalledTooEarlyError = DocumentClientError("`next` must be called after an initial set of items have been fetched.")
        val NotFound = DocumentClientError("The request resource was not found.")
    }
}