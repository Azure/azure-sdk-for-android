package com.azure.mobile.azuredataandroidexample.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.azure.mobile.azuredataandroidexample.viewholder.ViewHolder

/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

abstract class RecyclerViewAdapter<TData, TViewHolder: ViewHolder<TData>>(private val dataSet: ArrayList<TData>) : RecyclerView.Adapter<TViewHolder>() {

    private var itemClick: ((View, TData, Int) -> Unit)? = null
    private var itemLongClick: ((View, TData, Int) -> Unit)? = null

    private var isClearingSelections: Boolean = false
    private val isLongClickEnabled: Boolean
        get() = itemLongClick != null

    lateinit var originalDataSet: List<TData>

    // index is used to animate only the last selected/deselected row
    private var lastSelectionIndex: Int = -1
    private val selectedItemIndices: MutableSet<Int> = mutableSetOf()

    val selectedItemCount: Int
        get() = selectedItemIndices.count()

    constructor() : this(ArrayList<TData>()) //create with empty list of data

    override fun onBindViewHolder(holder: TViewHolder, position: Int) {

        //want to see if a) this item is selected, and b) if this selection is 'new' and needs to be (optionally) animated
        val selected = selectedItemIndices.contains(position)
        val animateSelection = lastSelectionIndex == position || isClearingSelections && selected //was it just selected or are we clearing all selections?

        // Get element from your dataset at this position and replace the contents of the view with that element
        holder.setData(dataSet[position], selected && !isClearingSelections, animateSelection)

        //reset our selection tracking vars
        if (animateSelection) {
            lastSelectionIndex = -1

            if (isClearingSelections && selected) {
                selectedItemIndices.remove(position)
            }

            //see if we're done clearing selections
            isClearingSelections = isClearingSelections and (selectedItemIndices.count() != 0)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TViewHolder {

        val inflater = LayoutInflater.from (parent.context)
        val holder = createViewHolder (inflater, parent)

        holder.setClickHandler (this::onClick)

        if (isLongClickEnabled)
        {
            holder.setLongClickHandler (this::onLongClick)
        }

        return holder
    }

    abstract fun createViewHolder(inflater: LayoutInflater, parent: ViewGroup): TViewHolder

    override fun getItemCount(): Int = dataSet.count()

    fun setItemClickHandler (handler: ((View, TData, Int) -> Unit)?) {
        itemClick = handler
    }

    private fun onClick (view: View, position: Int) {
        itemClick?.invoke (view, dataSet [position], position)
    }

    fun setItemLongClickHandler (handler: ((View, TData, Int) -> Unit)?) {
        itemLongClick = handler
    }

    private fun onLongClick (view: View, position: Int) {
        itemLongClick?.invoke (view, dataSet [position], position)
    }

    //region Selection

    fun toggleSelection(position: Int) {

        lastSelectionIndex = position

        if (selectedItemIndices.contains(position)) {
            selectedItemIndices.remove(position)
        } else {
            selectedItemIndices.add(position)
        }

        notifyItemChanged(position)
    }

    fun clearSelectedItems() {

        //not actually clearing anything here, just starting the clear operation here
        //	then, when ViewHolders are rebinding above, each will be evaluated and removed if selected
        isClearingSelections = true
        notifyDataSetChanged()
    }

    fun getSelectedItems() : List<TData> {

        return selectedItemIndices.map { index ->
            dataSet[index]
        }
    }

    //endregion

    //region Item Operations

    fun getItem (position: Int): TData = dataSet [position]

    fun itemExists (item: TData): Boolean = dataSet.contains (item)

    // Sets the items - use for situations where list is loaded async and isn't populated when the constructor is called.
    fun setItems (items: List<TData>) {

        dataSet.clear ()
        dataSet.addAll (items)

        notifyDataSetChanged ()
    }

    fun removeItem (position: Int): TData
    {
        val item = getItem (position)
        dataSet.removeAt (position)
        notifyItemRemoved (position)

        return item
    }

    fun removeItem (item: TData) : Boolean
    {
        val index = dataSet.indexOf(item)

        if (index >= 0) {
            dataSet.removeAt (index)
            notifyItemRemoved (index)

            return true
        }

        return false
    }

    fun addItem (position: Int, item: TData)
    {
        dataSet.add (position, item)
        notifyItemInserted (position)
    }

    fun addItems (items: List<TData>)
    {
        val initialCount = dataSet.count()
        dataSet.addAll (items)
        notifyItemRangeInserted (initialCount - 1, dataSet.count() - initialCount)
    }

    fun moveItem (fromPosition: Int, toPosition: Int)
    {
        val item = getItem (fromPosition)
        dataSet.removeAt (fromPosition)
        dataSet.add (toPosition, item)
        notifyItemMoved (fromPosition, toPosition)
    }

    fun clearItems () {
        dataSet.clear()
        notifyDataSetChanged()
    }

    //endregion
}