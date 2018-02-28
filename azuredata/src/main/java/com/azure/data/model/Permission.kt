package com.azure.data.model

import com.google.gson.annotations.SerializedName

/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

/**
 * Represents a per-User permission to access a specific resource in the Azure Cosmos DB service,
 * for example Document or Collection.
 */
class Permission() : Resource() {

    /**
     * Gets or sets the permission mode in the Azure Cosmos DB service.
     */
    var permissionMode: PermissionMode? = null

    /**
     * Gets or sets the self-link of resource to which the permission applies in the Azure Cosmos DB service.
     */
    @SerializedName(resourceLinkKey)
    var resourceLink: String? = null

//    Gets or sets optional partition key value for the permission in the Azure Cosmos DB service.
//    /// A permission applies to resources when two conditions are met:
//    ///
//    /// 1. ResourceLink is prefix of resource's link. For example "/dbs/mydatabase/colls/mycollection"
//    ///    applies to "/dbs/mydatabase/colls/mycollection" and "/dbs/mydatabase/colls/mycollection/docs/mydocument"
//    /// 2. ResourcePartitionKey is superset of resource's partition key.
//    ///    For example absent/empty partition key is superset of all partition keys.
//    public private(set) var resourcePartitionKey:   String?

    /**
     * Gets the access token granting the defined permission from the Azure Cosmos DB service.
     */
    @SerializedName(tokenKey)
    var token: String? = null

    constructor(id: String, permissionMode: PermissionMode? = null, forResourceId: String) : this() {

        this.id = id
        this.permissionMode = permissionMode
        resourceLink = forResourceId
    }

    companion object {

        const val resourceName = "Permission"
        const val listName = "Permissions"

        const val tokenKey          = "_token"
        const val resourceLinkKey   = "resource"
    }

    /**
     * These are the access permissions for creating or replacing a Permission resource in the Azure Cosmos DB service.
     *
     * - read: All permission mode will provide the user with full access(read, insert, replace and delete)
     *         to a resource.
     * - all:  Read permission mode will provide the user with Read only access to a resource.
     */
    enum class PermissionMode {

        Read,
        All
    }
}