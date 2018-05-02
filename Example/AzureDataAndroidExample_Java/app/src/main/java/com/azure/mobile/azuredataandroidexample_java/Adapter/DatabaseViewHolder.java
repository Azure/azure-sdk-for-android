package com.azure.mobile.azuredataandroidexample_java.Adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.azure.mobile.azuredataandroidexample_java.R;

/**
 * Created by mww121 on 27/10/17.
 */

public class DatabaseViewHolder extends RecyclerView.ViewHolder {

    public TextView idTextView;
    public TextView ridTextView;
    public TextView selfTextView;
    public TextView eTagTextView;
    public TextView collsTextView;
    public TextView usersTextView;
    public TextView tsTextView;

    public DatabaseViewHolder(View itemView){
        super(itemView);

        idTextView = itemView.findViewById(R.id.id);
        ridTextView = itemView.findViewById(R.id.rid);
        selfTextView = itemView.findViewById(R.id.self);
        eTagTextView = itemView.findViewById(R.id.eTag);
        collsTextView = itemView.findViewById(R.id.colls);
        usersTextView = itemView.findViewById(R.id.users);
        tsTextView = itemView.findViewById(R.id.ts);
    }
}
