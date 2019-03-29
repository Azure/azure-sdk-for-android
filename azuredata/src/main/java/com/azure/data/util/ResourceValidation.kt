package com.azure.data.util

import com.azure.data.constants.HttpHeaderValue
import com.azure.data.model.Resource
import com.azure.data.util.ResourceValidationRegEx.invalidCharRegEx
import java.util.regex.Pattern

/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

fun String.isValidIdForResource() : Boolean =
        !this.isBlank() && this.length <= 255 && !ResourceValidationRegEx.invalidCharRegEx.matcher(this).find()

fun Resource.hasValidId() : Boolean =
        this.id.isValidIdForResource()

object ResourceValidationRegEx {

    val invalidCharRegEx : Pattern = Pattern.compile("[/?#\\s+]")
}

fun String.isValidPartitionKeyPath() : Boolean =
    !this.isBlank() && this.length <= 255 && this.startsWith('/') && !PartitionKeyValidationRegEx.invalidCharRegEx.matcher(this).find()

object PartitionKeyValidationRegEx {

    val invalidCharRegEx : Pattern = Pattern.compile("[?#\\s+]")
}

fun Int.isValidThroughput() : Boolean =
        this >= HttpHeaderValue.minDatabaseThroughput && this <= HttpHeaderValue.maxDatabaseThroughput && (this % HttpHeaderValue.databaseThroughputStep) == 0