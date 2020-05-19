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

import static com.azure.android.storage.sample.Constants.CONTAINER_NAME_EXTRA;
import static com.azure.android.storage.sample.Constants.FILE_URI_EXTRA;

public class MainActivity extends AppCompatActivity {
    public static final int PICK_FILE_RESULT_CODE = 1;
    public static final int REQUEST_READ_EXTERNAL_STORAGE = 1;
    public static final int REQUEST_WRITE_EXTERNAL_STORAGE = 2;

    private Button listBlobsButton;
    private Button uploadFileButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        StorageConfiguration storageConfiguration = StorageConfiguration.create(getApplicationContext());

        super.onCreate(savedInstanceState);

        requestPermission(Manifest.permission.READ_EXTERNAL_STORAGE, REQUEST_READ_EXTERNAL_STORAGE);
        requestPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, REQUEST_WRITE_EXTERNAL_STORAGE);

        AndroidThreeTen.init(this);
        setContentView(R.layout.activity_main);

        this.listBlobsButton = findViewById(R.id.list_blobs_button);
        this.listBlobsButton.setOnClickListener(v -> {
            Log.d("MainActivity", "setOnClickListener(): List blobs button.");

            Intent intent = new Intent(this, ListAndDownloadBlobsActivity.class);
            intent.putExtra(CONTAINER_NAME_EXTRA, storageConfiguration.getContainerName());
            startActivity(intent);
        });

        this.uploadFileButton = findViewById(R.id.upload_file_button);
        this.uploadFileButton.setOnClickListener(v -> {
            Log.d("MainActivity", "setOnClickListener(): Upload file button.");

            Intent chooseFile = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            chooseFile.setType("*/*");
            chooseFile = Intent.createChooser(chooseFile, "Select a file to upload.");
            startActivityForResult(chooseFile, PICK_FILE_RESULT_CODE);
        });
    }

    private void requestPermission(String permission, int requestCode) {
        if (ContextCompat.checkSelfPermission(this, permission)
            != PackageManager.PERMISSION_GRANTED) {
            Log.d("MainActivity", "requestPermission(): Permission: " + permission + " is not granted yet.");

            ActivityCompat.requestPermissions(this, new String[]{permission}, requestCode);
        } else {
            Log.d("MainActivity", "requestPermission(): Permission: " + permission + " was already granted.");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        switch (requestCode) {
            case PICK_FILE_RESULT_CODE: {
                if (resultCode == -1) {
                    Uri fileUri = data.getData();

                    Log.d("MainActivity", "onActivityResult(): File URI: " + fileUri.toString());

                    Intent intent = new Intent(this, UploadFileActivity.class);
                    intent.putExtra(FILE_URI_EXTRA, fileUri);
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
                    Log.d("MainActivity", "onRequestPermissionsResult(): Permission was already granted.");
                } else {
                    Log.d("MainActivity", "onRequestPermissionsResult():Permission was denied.");
                }
            }
        }
    }
}
