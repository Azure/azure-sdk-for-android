package com.azure.data.integration

import android.support.test.runner.AndroidJUnit4
import com.azure.data.AzureData
import com.azure.data.integration.common.ResourceTest
import com.azure.data.model.Offer
import com.azure.data.model.service.ListResponse
import org.awaitility.Awaitility.await
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

@RunWith(AndroidJUnit4::class)
class OfferTests : ResourceTest<Offer>("OfferTests", false, false) {

    @Test
    fun listOffers() {

        AzureData.getOffers {
            resourceListResponse = it
        }

        await().until {
            resourceListResponse != null
        }

        assertListResponseSuccess(resourceListResponse, false, false)
       //assert(resourceListResponse?.resource?.count!! > 0) //can we assume there will always be > 0 offers? Answer == no
    }

    // Note: doesn't seem like there will be enough offers returned here to support a paging test...currently getting 0 offers on 6/12/19

//    @Test
//    fun listOffersPaging() {
//
//        val idsFound = mutableListOf<String>()
//        var waitForResponse : ListResponse<Offer>? = null
//
//        // Get the first one
//        AzureData.getOffers(1) { waitForResponse = it }
//
//        await().until { waitForResponse != null }
//
//        assertPageN(idsFound, waitForResponse, checkCreatedId = false)
//
//        // Get the remaining
//        while (waitForResponse?.hasMoreResults == true) {
//
//            waitForResponse.let { response ->
//
//                waitForResponse = null
//
//                response!!.next {
//
//                    if (it.hasMoreResults) {
//                        assertPageN(idsFound, it, checkCreatedId = false)
//                    } else {
//                        assertPageLast(idsFound, it, checkCreatedId = false)
//                    }
//
//                    waitForResponse = it
//                }
//            }
//
//            await().until { waitForResponse != null }
//        }
//
//        // Try to get one more
//        waitForResponse!!.next {
//            assertPageOnePastLast(it)
//        }
//    }

    @Test
    fun getOffer() {

        var offer: Offer? = null

        AzureData.getOffers {

            offer = it.resource?.items?.firstOrNull()

            offer?.let {

                AzureData.getOffer(it.id) { offerResponse ->
                    response = offerResponse
                }

                await().until { response != null }

                assertResourceResponseSuccess(response)
                assertEquals(offer?.id, response?.resource?.id)
            } ?: run {

                // no offers!
                assertEquals(0, it.resource?.items?.size)
            }
        }
    }
}