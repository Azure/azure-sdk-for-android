package com.azure.android.storage.sample.kotlin

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

import com.azure.android.storage.sample.kotlin.core.util.paging.PageLoadState

class LoadingStateItemViewHolder private constructor(itemView: View, retryRunnable: Runnable) : RecyclerView.ViewHolder(itemView) {
    private val progressBar: ProgressBar = itemView.findViewById(R.id.progress_bar)
    private val retryButton: Button = itemView.findViewById(R.id.retry_button)
    private val errorMsg: TextView = itemView.findViewById(R.id.error_message)

    fun bind(loadState: PageLoadState?) {
        if (loadState != null) {
            if (loadState == PageLoadState.LOADING) {
                progressBar.visibility = View.VISIBLE
            } else {
                progressBar.visibility = View.GONE
            }
            if (loadState == PageLoadState.FAILED) {
                retryButton.visibility = View.VISIBLE
            } else {
                retryButton.visibility = View.GONE
            }
            if (false /* there is error*/) {
                errorMsg.text = "error-message"
            }
        }
    }

    companion object {
        fun create(parent: ViewGroup, retryRunnable: Runnable): LoadingStateItemViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.load_state_item, parent, false)
            return LoadingStateItemViewHolder(view, retryRunnable)
        }
    }

    init {
        retryButton.setOnClickListener { retryRunnable.run() }
    }
}
