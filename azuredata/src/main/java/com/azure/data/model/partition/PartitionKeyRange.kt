package com.azure.data.model.partition

import com.azure.data.model.Resource

/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

class PartitionKeyRange : Resource() {

    var minInclusive: String? = null

    var maxExclusive: String? = null

    var throughputFraction: Int? = null

    companion object {

        const val resourceName = "PartitionKeyRange"
        const val listName = "PartitionKeyRanges"
    }
}