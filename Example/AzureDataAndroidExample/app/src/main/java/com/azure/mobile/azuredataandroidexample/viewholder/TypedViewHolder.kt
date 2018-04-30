package com.azure.mobile.azuredataandroidexample.viewholder

import android.view.View

/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

interface TypedViewHolder<in TData> {

    fun findViews(rootView: View)

    fun setData(data: TData, selected: Boolean, animateSelection: Boolean)

    fun setClickHandler(handler: ClickHandler)
}