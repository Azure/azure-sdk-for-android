package com.azure.android.storage.sample;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.paging.PagedListAdapter;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.azure.android.storage.blob.models.BlobItem;
import com.azure.android.storage.sample.core.util.paging.PageLoadState;

public class ContainerBlobsPagedListAdapter
        extends PagedListAdapter<BlobItem, RecyclerView.ViewHolder> {
    private final Runnable retryRunnable;
    private PageLoadState pageLoadState;

    protected ContainerBlobsPagedListAdapter(Runnable retryRunnable) {
        super(new BlobItemComparer());
        this.retryRunnable = retryRunnable;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch (viewType) {
            case R.layout.blob_item:
                return BlobItemViewHolder.create(parent);
            case R.layout.load_state_item:
                return LoadingStateItemViewHolder.create(parent, this.retryRunnable);
            default:
                throw new IllegalArgumentException("unknown view type:" + viewType);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof BlobItemViewHolder) {
            ((BlobItemViewHolder)holder).bind(super.getItem(position));
        } else if (holder instanceof LoadingStateItemViewHolder) {
            ((LoadingStateItemViewHolder)holder).bind(this.pageLoadState);
        }
    }

    private boolean hasExtraRow() {
        return this.pageLoadState != null && this.pageLoadState != PageLoadState.LOADED;
    }

    /**
     * @return the total number of items that in the PagedList backing this adapter.
     */
    public int getItemCount() {
        if (this.hasExtraRow()) {
            return super.getItemCount() + 1;
        } else {
            return super.getItemCount();
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (this.hasExtraRow() && position == (this.getItemCount() - 1)) {
            return R.layout.load_state_item;
        } else {
            return R.layout.blob_item;
        }
    }

    void setPageLoadState(PageLoadState newPageLoadState) {
        PageLoadState previousPageLoadState = this.pageLoadState;
        boolean hadExtraRow = this.hasExtraRow();
        this.pageLoadState = newPageLoadState;
        boolean hasExtraRow = hasExtraRow();
        if (hadExtraRow != hasExtraRow) {
            if (hadExtraRow) {
                notifyItemRemoved(super.getItemCount());
            } else {
                notifyItemInserted(super.getItemCount());
            }
        } else if (hasExtraRow && previousPageLoadState != newPageLoadState) {
            notifyItemChanged(getItemCount() - 1);
        }
    }


    public static class BlobItemComparer extends DiffUtil.ItemCallback<BlobItem> {
        @Override
        public boolean areItemsTheSame(@NonNull BlobItem oldItem, @NonNull BlobItem newItem) {
            return false;
        }

        @Override
        public boolean areContentsTheSame(@NonNull BlobItem oldItem, @NonNull BlobItem newItem) {
            return oldItem.getName().equals(newItem.getName()) && oldItem.getVersionId().equals(newItem.getVersionId());
        }
    }
}
