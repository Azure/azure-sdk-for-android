package com.azure.mobile.azuredataandroidexample.fragment

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.support.v4.app.ActivityOptionsCompat
import android.support.v4.app.Fragment
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.azure.mobile.azuredataandroidexample.R
import com.azure.mobile.azuredataandroidexample.adapter.RecyclerViewAdapter
import com.azure.mobile.azuredataandroidexample.viewholder.ViewHolder

/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

abstract class RecyclerViewListFragment<TData, TViewHolder : ViewHolder<TData>> : Fragment(), SwipeRefreshLayout.OnRefreshListener {

    var showDividers: Boolean = true
    var enableLongClick: Boolean = true
    var enablePullToRefresh: Boolean = true

    lateinit var recyclerView: RecyclerView
    var layoutManagerCurrent: RecyclerView.LayoutManager? = null
    var itemAnimatorCurrent: RecyclerView.ItemAnimator? = null
    var swipeRefreshLayout: SwipeRefreshLayout? = null

    lateinit var adapter: RecyclerView.Adapter<*>
    lateinit var typedAdapter: RecyclerViewAdapter<TData, TViewHolder>

    // Gets or sets the resource ID of the Tab item layout to use.  Defaults to 'recycler_view_fragment'
    open val viewResourceId : Int = R.layout.recycler_view_fragment

    protected abstract fun createAdapter(): RecyclerViewAdapter<TData, TViewHolder>

    //region Lifecycle Methods

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val rootView = inflater.inflate (viewResourceId, container, false)

        recyclerView = rootView.findViewById (R.id.recyclerView)

        layoutManagerCurrent = getLayoutManager ()
        recyclerView.layoutManager = layoutManagerCurrent
        itemAnimatorCurrent = getItemAnimator ()
        recyclerView.itemAnimator = itemAnimatorCurrent

        //adds item divider lines if ShowDividers == true
        if (showDividers)
        {
            recyclerView.addItemDecoration (DividerItemDecoration (activity, DividerItemDecoration.VERTICAL))
        }

        typedAdapter = createAdapter ()
        adapter = typedAdapter

        recyclerView.scrollToPosition (0)
        recyclerView.adapter = adapter

        // configure swipe to refresh, unless the implementor has turned it off
        swipeRefreshLayout = rootView.findViewById (R.id.swipe_refresh_layout)
        swipeRefreshLayout?.isEnabled = enablePullToRefresh

        //start to load the data that will populate the RecyclerView
        if (enablePullToRefresh) {

            swipeRefreshLayout?.setOnRefreshListener (this)
            doLoadData ()
        }

        return rootView
    }

    override fun onStart() {
        super.onStart()
        attachEvents ()
    }

    override fun onStop ()
    {
        detachEvents ()
        super.onStop ()
    }

    override fun onDestroy ()
    {
        detachEvents ()
        super.onDestroy ()
    }

    //endregion

    open fun attachEvents ()
    {
        typedAdapter.setItemClickHandler (this::onItemClick)

        if (enableLongClick)
        {
            typedAdapter.setItemLongClickHandler (this::onItemLongClick)
        }
    }

    open fun detachEvents ()
    {
        typedAdapter.setItemClickHandler (null)
        typedAdapter.setItemLongClickHandler (null)
    }

    // Gets the layout manager that will be used for this RecyclerView.  Defaults to LinearLayoutManager
    open fun getLayoutManager (): RecyclerView.LayoutManager = LinearLayoutManager (context)

    // Gets the item animator that will be used for this RecyclerView.  Defaults to DefaultItemAnimator
    open fun getItemAnimator (): RecyclerView.ItemAnimator = DefaultItemAnimator ()

    open fun onLoadData () {

        //TODO: //need to push this to background thread

        println ("refreshTask complete; updating adapter(s)")
        activity?.runOnUiThread(this::onDataLoaded)
    }

    private fun doLoadData () {

        //only start a content refresh if there isn't on running already
        if (swipeRefreshLayout?.isRefreshing == false) {
            swipeRefreshLayout?.isRefreshing = true

            onLoadData ()
        }
    }

    open fun onDataLoaded () {

        swipeRefreshLayout?.isRefreshing = false

        //if we don't want pull to refresh functionality, disable the refresh layout after we've loaded here the first time
        if (!enablePullToRefresh)
        {
            swipeRefreshLayout?.isEnabled = false
        }
    }

    open fun onItemClick (view: View, item: TData, position: Int) {
    }

    open fun onItemLongClick (view: View, item: TData, position: Int) {
    }

    override fun onRefresh() {

        println ("Starting data refresh")
        onLoadData ()
    }

    // Transitions to activity, optionally with a shared element transition.
    open fun transitionToActivity (intent: Intent, transitionView: View? = null)
    {
        var options: Bundle? = null

        // shared element transitions are only supported on Android 5.0+
        if (transitionView != null)
        {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                options = ActivityOptionsCompat.makeSceneTransitionAnimation (activity!!, transitionView, transitionView.transitionName).toBundle ()
            }
        }

        startActivity (intent, options)
    }
}