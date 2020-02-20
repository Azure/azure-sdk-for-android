package com.azure.android.storage.sample;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.azure.android.core.http.Callback;
import com.azure.android.storage.blob.StorageBlobClient;
import com.azure.android.storage.blob.models.BlobDownloadAsyncResponse;
import com.azure.android.storage.blob.models.BlobItem;
import com.azure.android.storage.sample.config.StorageConfiguration;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class BlobItemViewHolder extends RecyclerView.ViewHolder {
    private final TextView blobName;

    private BlobItemViewHolder(@NonNull View itemView) {
        super(itemView);
        this.blobName = itemView.findViewById(R.id.blob_name);
    }

    public static BlobItemViewHolder create(ViewGroup parent, StorageBlobClient storageBlobClient) {
        StorageConfiguration storageConfiguration = StorageConfiguration.create(parent.getContext());

        View blobItemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.blob_item, parent, false);
        blobItemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String blobName = (String) ((TextView) view.findViewById(R.id.blob_name)).getText();
                String containerName = storageConfiguration.getContainerName();

                Toast.makeText(view.getContext(), "Downloading " + blobName, Toast.LENGTH_SHORT).show();

                View mainActivityView = (View) parent.getParent().getParent().getParent();
                showProgressBar(view, mainActivityView);

                storageBlobClient.download(containerName, blobName, new Callback<BlobDownloadAsyncResponse>() {
                        @Override
                        public void onResponse(BlobDownloadAsyncResponse response) {
                            File path = Environment.getExternalStorageDirectory();
                            File file = new File(path, blobName);

                            try (FileOutputStream fileOutputStream = new FileOutputStream(file)) {
                                fileOutputStream.write(response.getValue().bytes());
                            } catch (IOException e) {
                                Log.e("BlobViewItemHolder", "Error when downloading blob: " + containerName + "/" + blobName, e);
                                onFailure(e);
                            }

                            hideProgressBar(view, mainActivityView);

                            Toast.makeText(blobItemView.getContext(), "Download complete", Toast.LENGTH_SHORT).show();

                            showFileIntent(view, file);
                        }

                        @Override
                        public void onFailure(Throwable t) {
                            hideProgressBar(view, mainActivityView);

                            Toast.makeText(blobItemView.getContext(), "Download failed", Toast.LENGTH_SHORT).show();
                        }
                    });
            }
        });

        return new BlobItemViewHolder(blobItemView);
    }

    public void bind(BlobItem blobItem) {
        if (blobItem != null) {
            this.blobName.setText(blobItem.getName() != null ? blobItem.getName() : "loading...");
        }
    }

    private static void showProgressBar(View view, View mainActivityView) {
        mainActivityView.findViewById(R.id.download_progress_bar).setVisibility(View.VISIBLE);
        mainActivityView.findViewById(R.id.download_background).setVisibility(View.VISIBLE);
        ((Activity) view.getContext()).getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    private static void hideProgressBar(View view, View mainActivityView) {
        ((Activity) view.getContext()).getWindow().clearFlags(
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        mainActivityView.findViewById(R.id.download_progress_bar).setVisibility(View.GONE);
        mainActivityView.findViewById(R.id.download_background).setVisibility(View.GONE);
    }

    private static void showFileIntent(View view, File file) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        Uri fileUri = FileProvider.getUriForFile(view.getContext(),
            view.getContext().getApplicationContext().getPackageName() + ".provider",
            file);
        String contentType = view.getContext().getContentResolver().getType(fileUri);
        intent.setDataAndType(fileUri, contentType);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        view.getContext().startActivity(intent);
    }
}
