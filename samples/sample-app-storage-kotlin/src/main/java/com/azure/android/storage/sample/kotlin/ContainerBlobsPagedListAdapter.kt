package com.azure.android.storage.sample.kotlin

import android.view.ViewGroup
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.azure.android.storage.blob.models.BlobItem
import com.azure.android.storage.blob.transfer.TransferClient
import com.azure.android.storage.sample.kotlin.core.util.paging.PageLoadState

class ContainerBlobsPagedListAdapter(private val transferClient: TransferClient, private val retryRunnable: Runnable)
    : PagedListAdapter<BlobItem, RecyclerView.ViewHolder>(BlobItemComparer()) {
    private var pageLoadState: PageLoadState? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            R.layout.blob_item -> BlobItemViewHolder.create(parent, transferClient)
            R.layout.load_state_item -> LoadingStateItemViewHolder.create(parent, retryRunnable)
            else -> throw IllegalArgumentException("unknown view type:$viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is BlobItemViewHolder) {
            holder.bind(super.getItem(position))
        } else if (holder is LoadingStateItemViewHolder) {
            holder.bind(pageLoadState)
        }
    }

    private fun hasExtraRow(): Boolean {
        return pageLoadState != null && pageLoadState != PageLoadState.LOADED
    }

    /**
     * @return the total number of items that in the PagedList backing this adapter.
     */
    override fun getItemCount(): Int {
        return if (hasExtraRow()) {
            super.getItemCount() + 1
        } else {
            super.getItemCount()
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (hasExtraRow() && position == this.itemCount - 1) {
            R.layout.load_state_item
        } else {
            R.layout.blob_item
        }
    }

    fun setPageLoadState(newPageLoadState: PageLoadState) {
        val previousPageLoadState: PageLoadState? = pageLoadState
        val hadExtraRow = hasExtraRow()
        pageLoadState = newPageLoadState
        val hasExtraRow = hasExtraRow()
        if (hadExtraRow != hasExtraRow) {
            if (hadExtraRow) {
                notifyItemRemoved(super.getItemCount())
            } else {
                notifyItemInserted(super.getItemCount())
            }
        } else if (hasExtraRow && previousPageLoadState != newPageLoadState) {
            notifyItemChanged(itemCount - 1)
        }
    }

    class BlobItemComparer : DiffUtil.ItemCallback<BlobItem>() {
        override fun areItemsTheSame(oldItem: BlobItem, newItem: BlobItem): Boolean {
            return false
        }

        override fun areContentsTheSame(oldItem: BlobItem, newItem: BlobItem): Boolean {
            return oldItem.name == newItem.name && oldItem.versionId == newItem.versionId
        }
    }
}
