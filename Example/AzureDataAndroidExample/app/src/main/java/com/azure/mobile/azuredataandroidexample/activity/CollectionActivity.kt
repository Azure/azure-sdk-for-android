package com.azure.mobile.azuredataandroidexample.activity

import android.os.Bundle
import android.support.v7.app.ActionBar
import com.azure.mobile.azuredataandroidexample.R
import com.azure.mobile.azuredataandroidexample.adapter.TabFragmentPagerAdapter
import com.azure.mobile.azuredataandroidexample.fragment.DocumentsFragment
import com.azure.mobile.azuredataandroidexample.fragment.StoredProceduresFragment
import com.azure.mobile.azuredataandroidexample.fragment.TriggersFragment
import com.azure.mobile.azuredataandroidexample.fragment.UserDefinedFunctionsFragment

/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

class CollectionActivity : BaseTabActivity() {

    private var databaseId: String? = null
    private var collId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {

        intent?.extras?.let {
            databaseId = it.getString("db_id")
            collId = it.getString("coll_id")
        }

        super.onCreate(savedInstanceState)
    }

    override fun configureToolbar(actionBar: ActionBar) {

        actionBar.title = "Collection: $collId"
        actionBar.setDisplayHomeAsUpEnabled(true)
    }

    override fun configureViewPager(adapter: TabFragmentPagerAdapter) {

        adapter.addFragment (DocumentsFragment (), getString(R.string.docs).toUpperCase())
        adapter.addFragment (StoredProceduresFragment (), getString(R.string.stored_procs).toUpperCase())
        adapter.addFragment (TriggersFragment (), getString(R.string.triggers).toUpperCase())
        adapter.addFragment (UserDefinedFunctionsFragment (), getString(R.string.udfs).toUpperCase())
    }
}