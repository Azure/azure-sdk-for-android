package com.azure.mobile.azuredataandroidexample_java.Adapter;

import android.view.View;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * Created by mww121 on 26/10/17.
 */

public class ViewHolderFactory {

    /**
     * Creates a retrofit service from an arbitrary class (clazz)
     * @param clazz Java interface of the retrofit service
     * @return retrofit service with defined endpoint
     */
    public static <T> Object create(final Class<T> clazz, View v) {
        try {
            Constructor[] cs = clazz.getConstructors();
            //Constructor c = clazz.getConstructor(Activity.class, View.class);
            cs[0].setAccessible(true);
            return cs[0].newInstance(v);
        }
        catch (IllegalAccessException | InstantiationException | InvocationTargetException ex) {
            ex.printStackTrace();
        }

        return null;
    }
}