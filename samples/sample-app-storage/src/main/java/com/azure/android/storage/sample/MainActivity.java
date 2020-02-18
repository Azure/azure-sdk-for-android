package com.azure.android.storage.sample;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.azure.android.storage.sample.config.StorageConfiguration;
import com.jakewharton.threetenabp.AndroidThreeTen;

public class MainActivity extends AppCompatActivity {
    public static final int PICK_FILE_RESULT_CODE = 1;
    public static final int REQUEST_READ_EXTERNAL_STORAGE = 1;

    private Button listBlobsButton;
    private Button uploadFileButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        StorageConfiguration storageConfiguration = StorageConfiguration.create(getApplicationContext());

        super.onCreate(savedInstanceState);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED) {
            Log.d("MainActivity", "Permission for storage is not granted yet");

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                REQUEST_READ_EXTERNAL_STORAGE);
        } else {
            Log.d("MainActivity", "Permission for storage was already granted");
        }

        AndroidThreeTen.init(this);
        setContentView(R.layout.activity_main);

        this.listBlobsButton = findViewById(R.id.list_blobs_button);
        this.listBlobsButton.setOnClickListener(v -> {
            Log.d("MainActivity", "setOnClickListener() for listing blobs.");

            Intent intent = new Intent(this, ContainerBlobsActivity.class);
            intent.putExtra(Constants.CONTAINER_NAME_EXTRA, storageConfiguration.getContainerName());
            startActivity(intent);
        });

        this.uploadFileButton = findViewById(R.id.upload_file_button);
        this.uploadFileButton.setOnClickListener(v -> {
            Log.d("MainActivity", "setOnClickListener() for uploading blobs.");

            Intent chooseFile = new Intent(Intent.ACTION_GET_CONTENT);
            chooseFile.setType("*/*");
            chooseFile = Intent.createChooser(chooseFile, "Select a file to upload.");
            startActivityForResult(chooseFile, PICK_FILE_RESULT_CODE);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        switch (requestCode) {
            case PICK_FILE_RESULT_CODE: {
                if (resultCode == -1) {
                    Uri fileUri = data.getData();

                    Log.d("MainActivity", "onActivityResult(): File URI: " + fileUri.toString());

                    Intent intent = new Intent(this, ProcessFileActivity.class);
                    intent.putExtra(Constants.FILE_URI_EXTRA, fileUri);
                    startActivity(intent);
                }
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_READ_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("MainActivity", "onRequestPermissionsResult(): Permission for storage was already granted");
                } else {
                    Log.d("MainActivity", "onRequestPermissionsResult():Permission for storage was denied");
                }
            }
        }
    }
}
