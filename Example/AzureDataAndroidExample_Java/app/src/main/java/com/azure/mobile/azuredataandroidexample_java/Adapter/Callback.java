package com.azure.mobile.azuredataandroidexample_java.Adapter;

import android.support.v7.widget.RecyclerView.ViewHolder;

import java.util.concurrent.Callable;

/**
 * Created by mww121 on 27/10/17.
 */

public abstract class Callback<T> implements Callable<Void> {
    protected T _result;
    protected ViewHolder _viewHolder;

    void setResult (T result, ViewHolder viewHolder) {
        _result = result;
        _viewHolder = viewHolder;
    }

    public T getResult() {
        return _result;
    }

    public ViewHolder getViewHolder() {
        return _viewHolder;
    }

    public abstract Void call ();
}
