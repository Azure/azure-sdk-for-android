package com.azure.android.storage.sample;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.azure.android.storage.blob.StorageBlobClient;
import com.azure.android.storage.blob.transfer.TransferClient;
import com.azure.android.storage.sample.config.StorageConfiguration;
import com.azure.android.storage.sample.core.util.tokenrequest.TokenRequestObservableAuthInterceptor;
import com.azure.android.storage.sample.core.util.tokenrequest.TokenRequestObserver;
import com.azure.android.storage.sample.core.util.tokenrequest.TokenResponseCallback;
import com.microsoft.identity.client.PublicClientApplication;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import static com.azure.android.storage.sample.Constants.FILE_URI_EXTRA;

public class UploadFileActivity extends AppCompatActivity {
    private static final String TAG = UploadFileActivity.class.getSimpleName();

    private ProgressBar progressBar;
    private StorageConfiguration storageConfiguration;

    // Singleton StorageBlobClient that will be created by Dagger. The singleton object is shared across various
    // activities in the application.
    @Inject
    StorageBlobClient storageBlobClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_uploadfile);

        // Request Dagger to get singleton StorageBlobClient and initialize this.storageBlobClient
        ((MainApplication) getApplication()).getAppComponent().inject(this);

        this.progressBar = findViewById(R.id.progressBar);
        this.storageConfiguration = StorageConfiguration.create(getApplicationContext());

        // Set up Login
        PublicClientApplication aadApp = new PublicClientApplication(this.getApplicationContext(),
            R.raw.authorization_configuration);

        final List<String> blobEndpointScopes = Arrays.asList(storageBlobClient.getBlobServiceUrl() + ".default");
        TokenRequestObservableAuthInterceptor authInterceptor =
            new TokenRequestObservableAuthInterceptor(blobEndpointScopes);

        authInterceptor.getTokenRequestObservable().observe(this, new TokenRequestObserver() {
            @Override
            public void onTokenRequest(String[] scopes, TokenResponseCallback callback) {
                MsalClient.signIn(aadApp, getActivity(), scopes, callback);
            }
        });

        // Create a new StorageBlobClient from the existing client with different base URL and credentials but sharing
        // the underlying OkHttp Client.
        storageBlobClient = storageBlobClient
            .newBuilder()
            .setBlobServiceUrl(storageConfiguration.getBlobServiceUrl())
            .setCredentialInterceptor(authInterceptor)
            .build();
    }

    @Override
    protected void onResume() {
        super.onResume();

        Uri fileUri = getIntent().getParcelableExtra(FILE_URI_EXTRA);
        //String contentType = this.getContentResolver().getType(fileUri);
        String filePath = PathUtil.getPath(this, fileUri);
        File fileToUpload = new File(filePath);
        int fileSize = (int) fileToUpload.length();

        this.progressBar.setMax(fileSize);

        final String containerName = storageConfiguration.getContainerName();
        final String blobName = fileToUpload.getName();

        Log.d("Upload file", "File path: " + filePath);
        Log.d("Upload file", "Blob name: " + blobName);
        Log.d("Upload file", "File size: " + fileSize);

        try {
            TransferClient transferClient = new TransferClient.Builder(getApplicationContext())
                .setStorageClient(storageBlobClient)
                .setRequiredNetworkType(androidx.work.NetworkType.CONNECTED)
                .build();

            transferClient.upload(containerName, blobName, fileToUpload)
                .observe(this, new TransferObserver() {
                    @Override
                    public void onStart(long transferId) {
                        Log.i(TAG, "onStart()");
                    }

                    public void onProgress(long transferId, long totalBytes, long bytesTransferred) {
                        Log.i(TAG, "onProgress(" + totalBytes + ", " + bytesTransferred + ")");

                        progressBar.setProgress((int) bytesTransferred);
                    }

                    public void onSystemPaused(long transferId) {
                        Log.i(TAG, "onSystemPaused()");
                    }

                    public void onResume(long transferId) {
                        Log.i(TAG, "onResumed()");
                    }

                    public void onComplete(long transferId) {
                        Log.i(TAG, "onCompleted()");

                        progressBar.setProgress(fileSize);

                        Toast.makeText(getApplicationContext(), "Upload complete", Toast.LENGTH_SHORT).show();
                    }

                    public void onError(long transferId, String errorMessage) {
                        Log.i(TAG, "onError() -> : " + errorMessage);
                    }
                });
        } catch (Exception ex) {
            Log.e(TAG, "Upload submit failed: ", ex);
        }
    }

    private Activity getActivity() {
        return this;
    }
}
