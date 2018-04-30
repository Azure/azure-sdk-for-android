package com.azure.mobile.azuredataandroidexample.viewholder

import android.support.v7.widget.RecyclerView
import android.view.View
import java.lang.reflect.InvocationTargetException

/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

object ViewHolderFactory {

    /**
     * Creates a retrofit service from an arbitrary class (clazz)
     * @param clazz Java interface of the retrofit service
     * @return retrofit service with defined endpoint
     */
    fun <T> create(clazz: Class<T>, v: View): RecyclerView.ViewHolder? {
        try {
            val cs = clazz.constructors
            //Constructor c = clazz.getConstructor(Activity.class, View.class);
            cs[0].isAccessible = true
            return cs[0].newInstance(v) as RecyclerView.ViewHolder
        } catch (ex: IllegalAccessException) {
            ex.printStackTrace()
        } catch (ex: InstantiationException) {
            ex.printStackTrace()
        } catch (ex: InvocationTargetException) {
            ex.printStackTrace()
        }


        return null
    }
}