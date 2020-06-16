// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.storage.sample.kotlin.core.credential

import org.threeten.bp.OffsetDateTime

class AccessToken(val token: String, expiresAt: OffsetDateTime) {
    /**
     * @return the time when the token expires, in UTC.
     */
    private val expiresAt: OffsetDateTime = expiresAt.minusMinutes(2)

    /**
     * @return if the token has expired.
     */
    val isExpired: Boolean
        get() = OffsetDateTime.now().isAfter(expiresAt)
}
