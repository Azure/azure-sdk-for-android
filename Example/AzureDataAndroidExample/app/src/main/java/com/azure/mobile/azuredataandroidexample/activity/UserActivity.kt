package com.azure.mobile.azuredataandroidexample.activity

import android.os.Bundle
import android.support.v7.app.ActionBar
import com.azure.mobile.azuredataandroidexample.R
import com.azure.mobile.azuredataandroidexample.adapter.TabFragmentPagerAdapter
import com.azure.mobile.azuredataandroidexample.fragment.PermissionsFragment

/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

class UserActivity : BaseTabActivity() {

    private lateinit var databaseId: String
    private lateinit var userId: String

    override fun onCreate(savedInstanceState: Bundle?) {

        intent?.extras?.let {
            databaseId = it.getString("db_id")!!
            userId = it.getString("user_id")!!
        }

        super.onCreate(savedInstanceState)
    }

    override fun configureToolbar(actionBar: ActionBar) {

        actionBar.title = "User: $userId"
        actionBar.setDisplayHomeAsUpEnabled(true)
    }

    override fun configureViewPager(adapter: TabFragmentPagerAdapter) {

        adapter.addFragment (PermissionsFragment (), getString(R.string.permissions).toUpperCase())
    }
}