package com.azure.mobile.azuredataandroidexample.extensions

import android.view.Menu

/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

fun Menu.updateItemEnabledStatus(menuItemId: Int, isEnabled: Boolean) {

    val menuItem = this.findItem(menuItemId)
    menuItem.isEnabled = isEnabled
    menuItem.icon.alpha = if (isEnabled) 255 else 64
}