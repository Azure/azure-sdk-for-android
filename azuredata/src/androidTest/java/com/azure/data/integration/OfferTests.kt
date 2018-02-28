package com.azure.data.integration

import android.support.test.runner.AndroidJUnit4
import com.azure.data.AzureData
import com.azure.data.model.Offer
import com.azure.data.model.ResourceType
import org.awaitility.Awaitility.await
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

@RunWith(AndroidJUnit4::class)
class OfferTests : ResourceTest<Offer>(ResourceType.Offer, false, false) {

    @Test
    fun listOffers() {

        AzureData.getOffers {
            resourceListResponse = it
        }

        await().until {
            resourceListResponse != null
        }

        assertResponseSuccess(resourceListResponse)
        assert(resourceListResponse?.resource?.count!! > 0) //can we assume there will always be > 0 offers?
    }

    @Test
    fun getOffer() {

        var offer: Offer? = null

        AzureData.getOffers {
            offer = it.resource?.items?.first()

            AzureData.getOffer(offer!!.id) {
                resourceResponse = it
            }
        }

        await().until {
            resourceResponse != null
        }

        assertResponseSuccess(resourceResponse)
        assertEquals(offer?.id, resourceResponse?.resource?.id)
    }
}