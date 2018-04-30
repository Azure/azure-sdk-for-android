package com.azure.mobile.azuredataandroidexample.adapter

import android.content.Context
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.azure.mobile.azuredataandroidexample.R

/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

// Adapter where Fragments can be created/configured externally and added/managed here in the adapter.
open class TabFragmentPagerAdapter(private val context: Context, manager: FragmentManager) : FragmentPagerAdapterBase(manager) {

    data class TabConfig(val fragment: Fragment, val title: String)

    private val tabList: MutableList<TabConfig> = mutableListOf()

    // Gets or sets the resource ID of the Tab item layout to use.  Defaults to 'stacked_tab_layout'
    open val tabViewResourceId : Int = R.layout.stacked_tab_layout

    override fun getCount(): Int = tabList.size

    fun addFragment (fragment: Fragment, title: String = "") //showTitle: Boolean
    {
        tabList.add (TabConfig(fragment, title))
    }

    override fun getItem(position: Int): Fragment = tabList[position].fragment

    override fun getPageTitle(position: Int): CharSequence = tabList[position].title

    // Inflates the tab view at the specified position.
    open fun inflateTabView (position: Int) : View
    {
        //get the item view and set the text + icon/image
        val tabItemView = LayoutInflater.from (context).inflate (tabViewResourceId, null)
        val tabText = tabItemView.findViewById<TextView> (R.id.tabText)
//        var tabImage = tabItemView.findViewById<ImageView> (Resource.Id.tabIcon)

        val tab = tabList [position]

        tabText.text = tab.title

//        if (!tab.ShowTitle)
//        {
//            tabText.Visibility = ViewStates.Gone
//            tabImage.LayoutParameters.Height = ViewGroup.LayoutParams.MatchParent
//        }

//        val icon = tab.iconResource

//        if (icon > -1)
//        {
//            tabImage.SetImageResource (icon)
//
//            //tabImage.SetBackgroundResource (icon)
//        }

        tabItemView.layoutParams = ViewGroup.LayoutParams (ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)

        if (position == 0)
        {
            tabItemView.isSelected = true
        }

        return tabItemView
    }

    // Fills the tab layout with the tab fragments that have been added to this adapter.
    open fun fillTabLayout (tabLayout: TabLayout)
    {
        for (i in 0..count) {
            val tab = tabLayout.getTabAt (i)

            tab?.customView = inflateTabView (i)
        }
    }
}