package com.azure.mobile.azuredataandroidexample_java.Adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

public class CardAdapter<T> extends RecyclerView.Adapter<ViewHolder> {
    private List<T> _mItems;

    private int _viewLayoutId;

    private Callback<Object> _onBindViewHolderMethod;
    private Class<?> _viewHolderClazz;

    public CardAdapter(int viewLayoutId, Callback<Object> onBindViewHolderMethod, Class<?> viewHolderClazz) {

        super();

        _viewLayoutId = viewLayoutId;
        _onBindViewHolderMethod = onBindViewHolderMethod;
        _viewHolderClazz = viewHolderClazz;

        _mItems = new ArrayList<T>();
    }

    public void addData(T item) {
        _mItems.add(item);

        notifyDataSetChanged();
    }

    public void removeData(T item) {
        _mItems.remove(item);

        notifyDataSetChanged();
    }

    public void clear() {
        _mItems.clear();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(_viewLayoutId, viewGroup, false);

        return (ViewHolder) ViewHolderFactory.create(_viewHolderClazz, v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        Object item = _mItems.get(i);

        _onBindViewHolderMethod.setResult(item, viewHolder);

        if (item != null) {
            _onBindViewHolderMethod.call();
        }
    }

    @Override
    public int getItemCount() {
        return _mItems.size();
    }
}