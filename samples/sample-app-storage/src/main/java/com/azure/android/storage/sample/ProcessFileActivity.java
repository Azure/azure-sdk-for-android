package com.azure.android.storage.sample;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;

import com.azure.android.storage.blob.StorageBlobClient;
import com.azure.android.storage.blob.credentials.SasTokenCredential;
import com.azure.android.storage.blob.implementation.Constants;
import com.azure.android.storage.blob.interceptor.SasTokenCredentialInterceptor;
import com.azure.android.storage.blob.upload.UploadListener;
import com.azure.android.storage.blob.upload.UploadManager;
import com.azure.android.storage.sample.config.StorageConfiguration;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;
import java.util.UUID;

import javax.inject.Inject;

public class ProcessFileActivity extends AppCompatActivity {
    private ProgressBar progressBar;
    private final int fileSize = 22 * Constants.MB;

    @Inject
    StorageBlobClient storageBlobClient;
    private StorageConfiguration config;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_processfile);
        this.progressBar = findViewById(R.id.progressBar);
        this.config = StorageConfiguration.create(getApplicationContext());
        // Request Dagger to inject default StorageBlobClient
        ((MainApplication) getApplication()).getAppComponent().inject(this);
        // Create a new StorageBlobClient from existing client with different base url and credentials
        // but shares underlying OkHttp Client.
        storageBlobClient = storageBlobClient
                .newBuilder()
                .setBlobServiceUrl(config.getBlobServiceUrl())
                .setCredentialInterceptor(new SasTokenCredentialInterceptor(new SasTokenCredential(config.getSasToken())))
                .build();
    }

    @Override
    protected void onResume() {
        super.onResume();
        final String fileName = "myFile.txt";
        final String blobName = fileName;

        File fileToUpload = createLocalFile(getApplicationContext(), fileName, fileSize);
        this.progressBar.setMax(fileSize);
        UploadManager uploadManager = new UploadManager(this.storageBlobClient);
        uploadManager.upload(this.config.getContainerName(),
                blobName,
                fileToUpload, new UploadListener() {
                    @Override
                    public void onUploadProgress(int totalBytes, int bytesUploaded) {
                        progressBar.setProgress(bytesUploaded);
                    }

                    @Override
                    public void onError(Throwable t) {
                        Log.e("uploadManager", "Uploaded Failed", t);
                    }

                    @Override
                    public void onCompleted() {
                        Log.v("uploadManager", "Uploaded Completed");
                    }
                });

    }

    private static File createLocalFile(Context appContext, String fileName, int fileSize) {
        return createLocalFile(appContext, fileName, generateRandomBytes(fileSize));
    }

    private static File createLocalFile(Context appContext, String fileName, byte [] fileContent) {
        String myFilePath = appContext.getExternalFilesDir(null).getAbsolutePath() + fileName;
        File myFile = null;
        try (FileOutputStream fileOutputStream = new FileOutputStream(myFilePath)) {
            fileOutputStream.write(fileContent, 0, fileContent.length);
            fileOutputStream.flush();
            myFile = new File(myFilePath);
        } catch (FileNotFoundException e) {
            Log.e("ProcessFileActivity", "createLocalFile", e);
        } catch (IOException e) {
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
