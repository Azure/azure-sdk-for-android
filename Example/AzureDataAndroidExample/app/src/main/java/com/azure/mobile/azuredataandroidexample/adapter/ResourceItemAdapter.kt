package com.azure.mobile.azuredataandroidexample.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import com.azure.data.model.Resource
import com.azure.mobile.azuredataandroidexample.R
import com.azure.mobile.azuredataandroidexample.viewholder.ResourceViewHolder

/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

class ResourceItemAdapter<TData: Resource> : RecyclerViewAdapter<TData, ResourceViewHolder>() {

    override fun createViewHolder(inflater: LayoutInflater, parent: ViewGroup): ResourceViewHolder {

        val itemView = inflater.inflate (R.layout.two_row_viewcell, parent, false)

        return ResourceViewHolder(itemView)
    }
}