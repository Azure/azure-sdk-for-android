package com.azure.data.util

import com.azure.data.model.Resource
import com.azure.data.util.ResourceValidationRegEx.invalidCharRegEx
import java.util.regex.Pattern

/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

    fun String.isValidIdForResource() : Boolean =
            !this.isBlank() && this.length <= 255 && !invalidCharRegEx.matcher(this).find()

    fun Resource.hasValidId() : Boolean =
            this.id.isValidIdForResource()

    object ResourceValidationRegEx {

        val invalidCharRegEx : Pattern = Pattern.compile("[/?#\\s+]")
    }