package com.azure.data.service

import com.azure.data.model.PermissionMode
import com.azure.data.model.ResourceType

/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

class PermissionProviderConfiguration {

    // get readWrite token even for read operations to prevent scenario of
    // getting a read token for a read operation then subsequently performing
    // a write operation on the same resource requiring another round trip to
    // server to get a token with write permissions.
    //
    // if this is set to PermissionMode.All, should always request a readWrite token from server
    //
    // default: PermissionMode.Read
    var defaultPermissionMode: PermissionMode = PermissionMode.Read

    // this specifies the at what level of the resource hierarchy
    // (Database/Collection/Document) to request a resource token
    //
    // for example, if this property is set to ResourceType.Collection and the user tries to
    // write to a document, we'd request a readWrite resource token for the
    // entire collection versus requesting a token only for the document
    //
    // default: ResourceType.Collection
    var defaultResourceType: ResourceType? = ResourceType.Collection

    var defaultTokenDuration: Long = 3600 // 1 hour

    var tokenRefreshThreshold: Long = 600 // 10 minutes

    companion object {

        val default: PermissionProviderConfiguration by lazy {
            PermissionProviderConfiguration()
        }
    }
}