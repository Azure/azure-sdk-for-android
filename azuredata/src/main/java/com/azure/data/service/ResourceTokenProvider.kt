package com.azure.data.service

import com.azure.core.crypto.CryptoProvider
import com.azure.core.http.HttpMethod
import com.azure.core.util.DateUtil
import com.azure.core.util.urlEncode
import com.azure.data.model.PermissionMode
import com.azure.data.model.service.ResourceLocation
import com.azure.data.model.service.ResourceToken
import java.util.*

/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

class ResourceTokenProvider(private val masterKey: String, private val permissionMode: PermissionMode) {

    private val tokenVersion = "1.0"

    private val dateFormatter = DateUtil.getDateFromatter(DateUtil.Format.Rfc1123Format)

    // https://docs.microsoft.com/en-us/rest/api/documentdb/access-control-on-documentdb-resources#constructkeytoken
    fun getToken(resourceLocation: ResourceLocation, method: HttpMethod) : ResourceToken? {

        if (!method.isRead() && permissionMode != PermissionMode.All) {
            return null
        }

        val dateString = String.format("%s %s", dateFormatter.format(Date()), "GMT")

        val payload = String.format("%s\n%s\n%s\n%s\n\n",
                method.name.toLowerCase(Locale.ROOT),
                resourceLocation.type().toLowerCase(Locale.ROOT),
                resourceLocation.link(),
                dateString.toLowerCase(Locale.ROOT))

        val signature = CryptoProvider.hmacEncrypt(payload, masterKey)

        val authStringEncoded = String.format("type=master&ver=%s&sig=%s", tokenVersion, signature).urlEncode()

        return ResourceToken(authStringEncoded, dateString)
    }
}