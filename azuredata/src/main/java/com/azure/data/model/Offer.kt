package com.azure.data.model

import com.google.gson.annotations.SerializedName

/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

/**
 * Represents the Standard pricing offer for a resource (collection) in the Azure Cosmos DB service.
 *
 * - Remark:
 *   Currently, offers are only bound to the collection resource.
 */
class Offer : Resource() {

    /**
     * Gets or sets the OfferType for the resource offer in the Azure Cosmos DB service.
     */
    var offerType:          String? = null

    /**
     * Gets or sets the version of this offer resource in the Azure Cosmos DB service.
     */
    var offerVersion:       String? = null

    /**
     * Gets or sets the self-link of a resource to which the resource offer applies to in the Azure Cosmos DB service.
     */
    @SerializedName(resourceLinkKey)
    var resourceLink:       String? = null

    var offerResourceId:    String? = null

    /**
     * Gets or sets the OfferContent for the resource offer in the Azure Cosmos DB service.
     */
    var content:            OfferContent? = null

    companion object {

        const val resourceName = "Offer"
        const val listName = "Offers"

        const val resourceLinkKey = "resource"
    }

    /**
     * Represents content properties tied to the Standard pricing tier for the Azure Cosmos DB service.
     */
    class OfferContent {

        /**
         * Represents Request Units(RU)/Minute throughput is enabled/disabled for collection in
         * the Azure Cosmos DB service.
         */
        var offerIsRUPerMinuteThroughputEnabled: Boolean? = null

        /**
         * Represents customizable throughput chosen by user for his collection in the Azure Cosmos DB service.
         */
        var offerThroughput: Int = 1000
    }
}