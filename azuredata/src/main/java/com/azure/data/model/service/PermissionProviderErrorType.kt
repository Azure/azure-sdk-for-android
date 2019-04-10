package com.azure.data.model.service

/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

class PermissionProviderError private constructor() : Error() {

    companion object {

        val PermissionCacheFailed = PermissionProviderError()
        val GetPermissionFailed = PermissionProviderError()
        val InvalidResourceType = PermissionProviderError()
        val InvalidDefaultResourceType = PermissionProviderError()
    }
}