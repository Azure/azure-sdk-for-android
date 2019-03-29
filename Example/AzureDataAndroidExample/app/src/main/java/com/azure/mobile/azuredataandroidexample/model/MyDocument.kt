package com.azure.mobile.azuredataandroidexample.model

import com.azure.data.model.Document
import com.azure.data.model.User
import java.util.*

/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

class MyDocument (id: String? = null) : Document(id) {

    var testString = "My Custom String"
    var testNumber = 0
    var testDate: Date = Date()
    var testBool = false
    var testArray = arrayOf(1, 2, 3)
    var testObject: User? = null
}