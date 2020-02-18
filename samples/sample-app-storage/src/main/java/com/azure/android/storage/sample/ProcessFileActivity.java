package com.azure.android.storage.sample;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.azure.android.storage.blob.StorageBlobClient;
import com.azure.android.storage.blob.upload.UploadListener;
import com.azure.android.storage.blob.upload.UploadManager;
import com.azure.android.storage.sample.config.StorageConfiguration;
import com.azure.android.storage.sample.core.util.tokenrequest.TokenRequestObservableAuthInterceptor;
import com.azure.android.storage.sample.core.util.tokenrequest.TokenRequestObserver;
import com.azure.android.storage.sample.core.util.tokenrequest.TokenResponseCallback;
import com.microsoft.identity.client.PublicClientApplication;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import javax.inject.Inject;

public class ProcessFileActivity extends AppCompatActivity {
    //private final int fileSize = 22 * Constants.MB;

    private StorageConfiguration storageConfiguration;
    private ProgressBar progressBar;

    // Singleton StorageBlobClient that will be created by Dagger. The singleton object is shared across various
    // activities in the application.
    @Inject
    StorageBlobClient storageBlobClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_processfile);

        // Request Dagger to get singleton StorageBlobClient and initialize this.storageBlobClient
        ((MainApplication) getApplication()).getAppComponent().inject(this);

        this.progressBar = findViewById(R.id.progressBar);
        this.storageConfiguration = StorageConfiguration.create(getApplicationContext());

        // Set up Login
        PublicClientApplication aadApp = new PublicClientApplication(this.getApplicationContext(),
            R.raw.auth_configuration);

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

        //final String fileName = "myFile.txt";
        //final String blobName = fileName;
        //File fileToUpload = createLocalFile(getApplicationContext(), fileName, fileSize);

        Uri fileUri = getIntent().getParcelableExtra(Constants.FILE_URI_EXTRA);
        String contentType = this.getContentResolver().getType(fileUri);
        String filePath = PathUtils.getPath(this, fileUri);
        File fileToUpload = new File(filePath);
        String blobName = fileToUpload.getName();
        int fileSize = (int) fileToUpload.length();

        Log.d("Upload file", "File path: " + filePath);
        Log.d("Upload file", "Blob name: " + blobName);
        Log.d("Upload file", "File size: " + fileSize);

        this.progressBar.setMax(fileSize);

        UploadManager uploadManager = new UploadManager(this.storageBlobClient);
        uploadManager.upload(this.storageConfiguration.getContainerName(), blobName, contentType, fileToUpload,
            new UploadListener() {
                @Override
                public void onUploadProgress(int totalBytes, int bytesUploaded) {
                    progressBar.setProgress(bytesUploaded);
                }

                @Override
                public void onError(Throwable t) {
                    Log.e("UploadManager", "Uploaded Failed", t);
                }

                @Override
                public void onCompleted() {
                    Log.v("UploadManager", "Uploaded Completed");

                    progressBar.setProgress(fileSize);
                    Toast.makeText(getApplicationContext(), "Upload complete", Toast.LENGTH_SHORT).show();
                }
            });
    }

    private Activity getActivity() {
        return this;
    }

    // Can be used to create a random data file of a given size.
    private static File createLocalFile(Context appContext, String fileName, int fileSize) {
        return createLocalFile(appContext, fileName, generateRandomBytes(fileSize));
    }

    private static File createLocalFile(Context appContext, String fileName, byte[] fileContent) {
        String myFilePath = appContext.getExternalFilesDir(null).getAbsolutePath() + fileName;
        File myFile = null;

        try (FileOutputStream fileOutputStream = new FileOutputStream(myFilePath)) {
            fileOutputStream.write(fileContent, 0, fileContent.length);
            fileOutputStream.flush();
            myFile = new File(myFilePath);
        } catch (IOException e) { // FileNotFoundException
            Log.e("ProcessFileActivity", "createLocalFile", e);
        }

        return myFile;
    }

    private static byte[] generateRandomBytes(int size) {
        long seed = UUID.randomUUID().getMostSignificantBits() & Long.MAX_VALUE;
        Random rand = new Random(seed);
        byte[] data = new byte[size];
        rand.nextBytes(data);

        return data;
    }
}
