package com.azure.android.storage.sample;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.azure.android.storage.blob.models.BlobItem;

public class BlobItemViewHolder extends RecyclerView.ViewHolder {
    private final TextView blobName;

    private BlobItemViewHolder(@NonNull View itemView) {
        super(itemView);
        this.blobName = itemView.findViewById(R.id.bobname);
    }

    public static BlobItemViewHolder create(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.blob_item, parent, false);
        return new BlobItemViewHolder(view);
    }

    public void bind(BlobItem blobItem) {
        if (blobItem != null) {
            this.blobName.setText(blobItem.getName() != null ? blobItem.getName() : "loading...");
        }
    }
}
