package com.azure.data.model

import com.google.gson.annotations.SerializedName

/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

/**
 * Represents a user in the Azure Cosmos DB service.
 */
class User(id: String? = null) : Resource(id) {

    /**
     * Gets the self-link of the permissions associated with the user for the Azure Cosmos DB service.
     */
    @SerializedName(permissionsLinkKey)
    var permissionsLink: String? = null

    companion object {

        const val resourceName = "User"
        const val listName = "Users"

        const val permissionsLinkKey  = "_permissions"
    }
}