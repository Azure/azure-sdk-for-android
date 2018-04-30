package com.azure.mobile.azuredataandroidexample.activity

import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v7.app.ActionBar
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import com.azure.mobile.azuredataandroidexample.R
import com.azure.mobile.azuredataandroidexample.adapter.TabFragmentPagerAdapter
import kotlinx.android.synthetic.main.tab_layout.*

/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

abstract class BaseTabActivity : AppCompatActivity() {

    private lateinit var pagerAdapter: TabFragmentPagerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        setContentView(R.layout.tab_layout)

        //Toolbar will now take on default Action Bar characteristics
        setSupportActionBar (toolbar)

        configureToolbar(supportActionBar!!)

        //set up tabs + view pager
        setupViewPager ()
    }

    protected open fun configureToolbar(actionBar: ActionBar) {

        actionBar.title = title
    }

    abstract fun configureViewPager(adapter: TabFragmentPagerAdapter)

    private fun setupViewPager() {

        // create & config our adapter
        pagerAdapter = TabFragmentPagerAdapter (this, supportFragmentManager)

        configureViewPager(pagerAdapter)

        viewPager.adapter = pagerAdapter

        // configure tabLayout & viewPager
        with(tabLayout) {
            tabMode =  TabLayout.MODE_FIXED
            tabGravity = TabLayout.GRAVITY_FILL
            setupWithViewPager (viewPager)
        }

        // finally, glue it all together
        pagerAdapter.fillTabLayout (tabLayout)


//        toolbarTitle.Text = PagerAdapter.GetTabFragment (0).Title;

//        viewPager.PageSelected += (sender, e) =>
//        {
//            ////update the query listener
//            //var fragment = PagerAdapter.GetFragmentAtPosition (e.Position);
//            //queryListener = (SearchView.IOnQueryTextListener)fragment;
//
//            //searchView?.SetOnQueryTextListener (queryListener);
//
//            //swap the title into the app bar title rather than including it in the tab
//            var tabFragment = PagerAdapter.GetTabFragment (e.Position);
//            //SupportActionBar.Title = tabFragment.Title;
//            toolbarTitle.Text = tabFragment.Title;
//        };
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = false
}