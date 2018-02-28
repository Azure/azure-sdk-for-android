package com.azure.data.model

import com.google.gson.annotations.SerializedName

/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

class Database : Resource() {

    @SerializedName(collectionsLinkKey)
    var collectionsLink: String? = null

    @SerializedName(usersLinkKey)
    var usersLink: String? = null

    companion object {

        const val resourceName = "Datebase"
        const val listName = "Databases"

        const val collectionsLinkKey    = "_colls"
        const val usersLinkKey          = "_users"
    }
}