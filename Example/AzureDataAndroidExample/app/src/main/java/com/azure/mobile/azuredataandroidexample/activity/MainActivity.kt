package com.azure.mobile.azuredataandroidexample.activity

import com.azure.mobile.azuredataandroidexample.R
import com.azure.mobile.azuredataandroidexample.adapter.TabFragmentPagerAdapter
import com.azure.mobile.azuredataandroidexample.fragment.DatabasesFragment
import com.azure.mobile.azuredataandroidexample.fragment.OffersFragment

/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

class MainActivity : BaseTabActivity() {

    override fun configureViewPager(adapter: TabFragmentPagerAdapter) {

        adapter.addFragment (DatabasesFragment(), getString(R.string.databases).toUpperCase())
        adapter.addFragment (OffersFragment(), getString(R.string.offers).toUpperCase())
    }
}