package com.azure.mobile

import com.azure.data.model.Document
import com.azure.data.model.User
import com.azure.data.model.partition.PartitionKey
import java.util.*

/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

open class CustomDocument(id: String? = null) : Document(id) {

    var customString = "My Custom String"
    var customNumber = 0
    var customDate: Date = Date()
    var customBool = false
    var customArray = arrayOf(1, 2, 3)
    var customObject: User? = null
}

class PartitionedCustomDocment(id: String? = null) : CustomDocument(id) {

    @PartitionKey
    var testKey = "MyPartitionKey"
}