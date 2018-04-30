package com.azure.mobile.azuredataandroidexample.viewholder

import android.support.v7.widget.RecyclerView
import android.view.View

/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

abstract class ViewHolderBase<in T: Any>(itemView: View) : RecyclerView.ViewHolder(itemView) {

    abstract fun setData(item: T)
}