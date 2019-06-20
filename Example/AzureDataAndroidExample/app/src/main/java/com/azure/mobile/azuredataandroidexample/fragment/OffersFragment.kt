package com.azure.mobile.azuredataandroidexample.fragment

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import com.azure.data.AzureData
import com.azure.data.model.Offer
import com.azure.data.model.service.ListResponse
import com.azure.data.model.service.Response
import com.azure.mobile.azuredataandroidexample.model.ResourceAction
import java.util.*

/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

class OffersFragment : ResourceListFragment<Offer>() {

    override val actionSupport: EnumSet<ResourceAction> = EnumSet.of(ResourceAction.Get)

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        setHasOptionsMenu(true)
    }

    override fun fetchData(callback: (ListResponse<Offer>) -> Unit) {

        AzureData.getOffers { response ->
            callback(response)
        }
    }

    override fun getItem(id: String, callback: (Response<Offer>) -> Unit) {

        AzureData.getOffer(id) { response ->
            callback(response)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {

        super.onCreateOptionsMenu(menu, inflater)

        menu.clear() //no "Add" command for Offers r.n.
    }
}