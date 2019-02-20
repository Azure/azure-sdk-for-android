package com.azure.data.integration.common

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

    open fun getValue(key: String): Any? {

        return when (key) {

            customStringKey -> this.customString
            customNumberKey -> this.customNumber
            customDateKey -> this.customDate
            customBoolKey -> this.customBool
            customArrayKey -> this.customArray
            customObjectKey -> this.customObject
            else -> null
        }
    }

    open fun setValue(key: String, value: Any?) {

        when (key) {
            customStringKey -> this.customString = value as String
            customNumberKey -> this.customNumber = value as Int
            customDateKey -> this.customDate = value as Date
            customBoolKey -> this.customBool = value as Boolean
            customArrayKey -> this.customArray = value as Array<Int>
            customObjectKey -> this.customObject = value as User
        }
    }

    companion object {

        const val customStringKey = "customString"
        const val customNumberKey = "customNumber"
        const val customDateKey = "customDate"
        const val customBoolKey = "customBool"
        const val customArrayKey = "customArray"
        const val customObjectKey = "customObject"
    }
}

class PartitionedCustomDocment(id: String? = null) : CustomDocument(id) {

    @PartitionKey
    var testKey = "MyPartitionKey"

    override fun getValue(key: String): Any? {

        return when (key) {

            testKeyKey -> this.testKey
            else -> super.getValue(key)
        }
    }

    override fun setValue(key: String, value: Any?) {

        when (key) {

            testKeyKey -> this.testKey = value as String
            else -> super.setValue(key, value)
        }
    }

    companion object {

        const val testKeyKey = "testKey"
    }
}