package com.azure.android.storage.sample;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.lifecycle.LifecycleOwner;
import androidx.recyclerview.widget.RecyclerView;

import com.azure.android.storage.blob.StorageBlobClient;
import com.azure.android.storage.blob.models.BlobItem;
import com.azure.android.storage.sample.config.StorageConfiguration;

import java.io.File;

public class BlobItemViewHolder extends RecyclerView.ViewHolder {
    private static final String TAG = BlobItemViewHolder.class.getSimpleName();
    private final TextView blobName;

    private BlobItemViewHolder(@NonNull View itemView) {
        super(itemView);
        this.blobName = itemView.findViewById(R.id.blob_name);
    }

    public static BlobItemViewHolder create(ViewGroup parent, StorageBlobClient storageBlobClient) {
        StorageConfiguration storageConfiguration = StorageConfiguration.create(parent.getContext());
        View blobItemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.blob_item, parent, false);

        blobItemView.setOnClickListener(view -> {
            String blobName = (String) ((TextView) view.findViewById(R.id.blob_name)).getText();
            String containerName = storageConfiguration.getContainerName();

            Toast.makeText(parent.getContext(), "Downloading " + blobName, Toast.LENGTH_SHORT).show();

            View mainActivityView = (View) parent.getParent().getParent().getParent();
            ProgressBar progressBar = mainActivityView.findViewById(R.id.download_progress_bar);

            showProgress(mainActivityView);

            File path = Environment.getExternalStorageDirectory();
            File file = new File(path, blobName);

            try {
                storageBlobClient.download(parent.getContext(), containerName, blobName, file)
                    .enqueue()
                    .observe((LifecycleOwner) parent.getContext(), new TransferObserver() {
                        @Override
                        public void onStart(String transferId) {
                            Log.i(TAG, "onStart() for transfer with ID: " + transferId);

                            Button cancelButton = mainActivityView.findViewById(R.id.cancel_button);

                            cancelButton.setOnClickListener(v -> {
                                storageBlobClient.cancel(parent.getContext(), transferId);
                                hideProgress(mainActivityView);
                                Toast.makeText(parent.getContext(), "Download cancelled", Toast.LENGTH_SHORT)
                                    .show();
                            });
                        }

                        @Override
                        public void onProgress(String transferId, long totalBytes, long bytesTransferred) {
                            Log.i(TAG, "onProgress(" + totalBytes + ", " + bytesTransferred +
                                ") for transfer with ID:" + transferId);


                            if (progressBar != null) {
                                if (progressBar.getProgress() == 0) {
                                    progressBar.setMax((int) totalBytes);
                                }

                                progressBar.setProgress((int) bytesTransferred);
                            }
                        }

                        @Override
                        public void onSystemPaused(String transferId) {
                            Log.i(TAG, "onSystemPaused() for transfer with ID: " + transferId);
                        }

                        @Override
                        public void onResume(String transferId) {
                            Log.i(TAG, "onResumed() for transfer with ID: " + transferId);
                        }

                        @Override
                        public void onComplete(String transferId) {
                            Log.i(TAG, "onCompleted() for transfer with ID: " + transferId);

                            if (progressBar != null) {
                                progressBar.setProgress(progressBar.getMax());
                            }

                            hideProgress(mainActivityView);
                            Toast.makeText(parent.getContext(), "Download complete", Toast.LENGTH_SHORT).show();
                            showFileIntent(parent.getContext(), file);

                            if (progressBar != null) {
                                progressBar.setProgress(0);
                            }
                        }

                        @Override
                        public void onError(String transferId, String errorMessage) {
                            Log.i(TAG, "onError() for transfer with ID: " + transferId + " -> : " + errorMessage);

                            hideProgress(mainActivityView);
                            Toast.makeText(parent.getContext(), "Download failed", Toast.LENGTH_SHORT).show();

                            if (progressBar != null) {
                                progressBar.setProgress(0);
                            }
                        }
                    });
            } catch (Exception ex) {
                Log.e(TAG, "Blob download failed: ", ex);
            }
        });

        return new BlobItemViewHolder(blobItemView);
    }

    public void bind(BlobItem blobItem) {
        if (blobItem != null) {
            this.blobName.setText(blobItem.getName() != null ? blobItem.getName() : "loading...");
        }
    }

    private static void showProgress(View mainActivityView) {
        mainActivityView.findViewById(R.id.download_background).setVisibility(View.VISIBLE);
        mainActivityView.findViewById(R.id.download_progress_bar).setVisibility(View.VISIBLE);
        mainActivityView.findViewById(R.id.download_buttons).setVisibility(View.VISIBLE);
        mainActivityView.findViewById(R.id.cancel_button).setVisibility(View.VISIBLE);
    }

    private static void hideProgress(View mainActivityView) {
        mainActivityView.findViewById(R.id.cancel_button).setVisibility(View.GONE);
        mainActivityView.findViewById(R.id.download_buttons).setVisibility(View.GONE);
        mainActivityView.findViewById(R.id.download_progress_bar).setVisibility(View.GONE);
        mainActivityView.findViewById(R.id.download_background).setVisibility(View.GONE);
    }

    private static void showFileIntent(Context context, File file) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        Uri fileUri =
            FileProvider.getUriForFile(context,context.getApplicationContext().getPackageName() + ".provider", file);
        String contentType = context.getContentResolver().getType(fileUri);
        intent.setDataAndType(fileUri, contentType);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        context.startActivity(intent);
    }
}
