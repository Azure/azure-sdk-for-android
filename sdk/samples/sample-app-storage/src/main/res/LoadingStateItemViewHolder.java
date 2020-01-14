package com.anuchandy.learn.msal;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.azure.android.core.util.paging.PageLoadState;

public class LoadingStateItemViewHolder extends RecyclerView.ViewHolder {
    private final ProgressBar progressBar;
    private final Button retryButton;
    private final TextView errorMsg;

    private LoadingStateItemViewHolder(@NonNull View itemView, @NonNull Runnable retryRunnable) {
        super(itemView);
        this.progressBar = itemView.findViewById(R.id.progress_bar);
        this.retryButton = itemView.findViewById(R.id.retry_button);
        this.errorMsg = itemView.findViewById(R.id.error_msg);
        this.retryButton.setOnClickListener(v -> retryRunnable.run());
    }

    public static LoadingStateItemViewHolder create(ViewGroup parent, Runnable retryRunnable) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.load_state_item, parent, false);
        return new LoadingStateItemViewHolder(view, retryRunnable);
    }

    public void bind(PageLoadState loadState) {
        if (loadState != null) {
            if (loadState == PageLoadState.LOADING) {
                this.progressBar.setVisibility(View.VISIBLE);
            } else {
                this.progressBar.setVisibility(View.GONE);
            }
            if (loadState == PageLoadState.FAILED) {
                this.retryButton.setVisibility(View.VISIBLE);
            } else {
                this.retryButton.setVisibility(View.GONE);
            }
            if (false /* there is error*/) {
                this.errorMsg.setText("error-message");
            }
        }
    }
}
