package com.azure.mobile.azuredataandroidexample.viewholder

import android.view.View
import com.azure.data.model.Resource

import kotlinx.android.synthetic.main.two_row_viewcell.view.*

/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

class ResourceViewHolder(itemView: View) : ViewHolder<Resource>(itemView) {

    override fun setData(data: Resource, selected: Boolean, animateSelection: Boolean) {

        super.setData(data, selected, animateSelection)

        itemView.title.text = data.id
        itemView.subTitle.text = data.resourceId
    }
}