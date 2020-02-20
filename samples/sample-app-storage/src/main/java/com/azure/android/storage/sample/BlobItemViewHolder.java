package com.azure.android.storage.sample;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.azure.android.storage.blob.StorageBlobClient;
import com.azure.android.storage.blob.models.BlobItem;
import com.azure.android.storage.sample.config.StorageConfiguration;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;

import okhttp3.ResponseBody;

public class BlobItemViewHolder extends RecyclerView.ViewHolder {
    private final TextView blobName;

    private BlobItemViewHolder(@NonNull View itemView) {
        super(itemView);
        this.blobName = itemView.findViewById(R.id.blob_name);
    }

    public static BlobItemViewHolder create(ViewGroup parent, StorageBlobClient storageBlobClient) {
        StorageConfiguration storageConfiguration = StorageConfiguration.create(parent.getContext());

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.blob_item, parent, false);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String blobName = (String) ((TextView) v.findViewById(R.id.blob_name)).getText();
                View mainActivity = (View) parent.getParent().getParent().getParent();

                new MyTask(v, mainActivity).execute(storageBlobClient, storageConfiguration.getContainerName(), blobName);
            }
        });

        return new BlobItemViewHolder(view);
    }

    public void bind(BlobItem blobItem) {
        if (blobItem != null) {
            this.blobName.setText(blobItem.getName() != null ? blobItem.getName() : "loading...");
        }
    }

    private static final class MyTask extends AsyncTask<Object, Void, File> {
        private final WeakReference<View> viewWeakReference;
        private final WeakReference<Context> contextWeakReference;
        private final WeakReference<View> backgroundWeakReference;
        private final WeakReference<ProgressBar> progressBarWeakReference;

        MyTask(View view, View mainActivity) {
            viewWeakReference = new WeakReference<>(view);
            contextWeakReference = new WeakReference<>(view.getContext());
            backgroundWeakReference = new WeakReference<>(mainActivity.findViewById(R.id.download_background));
            progressBarWeakReference = new WeakReference<>(mainActivity.findViewById(R.id.download_progress_bar));
        }

        @Override
        protected void onPreExecute() {
            View view = viewWeakReference.get();
            String blobName = (String) ((TextView) view.findViewById(R.id.blob_name)).getText();
            Toast.makeText(contextWeakReference.get(), "Downloading " + blobName, Toast.LENGTH_SHORT).show();
            backgroundWeakReference.get().setVisibility(View.VISIBLE);
            progressBarWeakReference.get().setVisibility(View.VISIBLE);
            ((Activity) contextWeakReference.get()).getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        }

        @Override
        protected File doInBackground(Object... objects) {
            StorageBlobClient storageBlobClient = (StorageBlobClient) objects[0];
            String containerName = (String) objects[1];
            String blobName = (String) objects[2];

            File path = Environment.getExternalStorageDirectory();
            File file = new File(path, blobName);

            ResponseBody response = storageBlobClient.download(containerName, blobName);

            try (FileOutputStream fileOutputStream = new FileOutputStream(file)) {
                fileOutputStream.write(response.bytes());
            } catch (IOException e) {
                Log.e("BlobViewItemHolder", "Error when downloading blob: " + containerName + "/" + blobName, e);
            }

            return file;
        }

        @Override
        protected void onPostExecute(File file) {
            ((Activity) contextWeakReference.get()).getWindow().clearFlags(
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            progressBarWeakReference.get().setVisibility(View.GONE);
            backgroundWeakReference.get().setVisibility(View.GONE);
            Toast.makeText(contextWeakReference.get(), "Download complete", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(Intent.ACTION_VIEW);
            Uri fileUri = FileProvider.getUriForFile(contextWeakReference.get(),
                contextWeakReference.get().getApplicationContext().getPackageName() + ".provider",
                file);
            String contentType = contextWeakReference.get().getContentResolver().getType(fileUri);
            intent.setDataAndType(fileUri, contentType);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            contextWeakReference.get().startActivity(intent);
        }
    }
}
