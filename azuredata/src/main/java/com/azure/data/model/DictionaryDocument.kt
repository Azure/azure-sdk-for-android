package com.azure.data.model

import com.azure.data.model.Document.Companion.Keys

/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

class DictionaryDocument(id: String? = null) : Document(id) {

    @Transient
    internal var data = DocumentDataMap()

    // will be mapped to indexer, i.e. doc[key]
    operator fun get(key: String) = data[key]

    // will be mapped to indexer, i.e. doc[key] = value
    operator fun set(key: String, value: Any?) {

        if (Keys.list.contains(key)) {
            throw Exception("Error: Cannot use [key] = value syntax to set the following system generated properties: ${Keys.list.joinToString()}")
        }

        data[key] = value
    }
}