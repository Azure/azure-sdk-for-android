package com.azure.data.model

import com.google.gson.annotations.SerializedName

/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

class ResourceList<T: Resource> : ResourceBase() {

    @SerializedName(Keys.countKey)
    var count: Int = 0

    @SerializedName(Document.listName, alternate = [Database.listName, Attachment.listName, DocumentCollection.listName, Offer.listName, Permission.listName, StoredProcedure.listName, Trigger.listName, User.listName, UserDefinedFunction.listName])
    lateinit var items: Array<T>

    val isPopuated: Boolean
            get() = resourceId != null && count > 0

    override fun setAltContentLink(itemTypePath: String, contentPath: String?) {

        items.forEach {item ->

            item.setAltContentLink(itemTypePath, contentPath)
        }
    }

    companion object {

        object Keys {

            const val countKey = "_count"

            val list = listOf(resourceIdKey, countKey)
        }
    }
}