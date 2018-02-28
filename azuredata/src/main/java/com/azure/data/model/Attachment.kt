package com.azure.data.model

import com.google.gson.annotations.SerializedName

/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

class Attachment(id: String, contentType: String, url: String) : Resource(id) {

    /**
     * Gets or sets the MIME content type of the attachment in the Azure Cosmos DB service.
     */
    var contentType: String? = contentType

    /**
     * Gets or sets the media link associated with the attachment content in the Azure Cosmos DB service.
     */
    @SerializedName(mediaLinkKey)
    var mediaLink: String? = url

    companion object {

        const val resourceName = "Attachment"
        const val listName = "Attachments"

        const val mediaLinkKey = "media"
    }
}