package com.azure.android.storage.sample.kotlin;

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.jakewharton.threetenabp.AndroidThreeTen

import com.azure.android.storage.sample.kotlin.config.StorageConfiguration


class MainActivity : AppCompatActivity() {
    private lateinit var listBlobsButton: Button
    private lateinit var uploadFileButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestPermission(Manifest.permission.READ_EXTERNAL_STORAGE, REQUEST_READ_EXTERNAL_STORAGE)
        requestPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, REQUEST_WRITE_EXTERNAL_STORAGE)
        AndroidThreeTen.init(this)
        setContentView(R.layout.activity_main)

        val storageConfiguration: StorageConfiguration = StorageConfiguration.create(applicationContext)
        listBlobsButton = findViewById(R.id.list_blobs_button)
        listBlobsButton.setOnClickListener {
            Log.d("MainActivity", "setOnClickListener(): List blobs button.")
            val intent = Intent(this, ListAndDownloadBlobsActivity::class.java)
            intent.putExtra(Constants.CONTAINER_NAME_EXTRA, storageConfiguration.containerName)
            startActivity(intent)
        }
        uploadFileButton = findViewById(R.id.upload_file_button)
        uploadFileButton.setOnClickListener(View.OnClickListener {
            Log.d("MainActivity", "setOnClickListener(): Upload file button.")
            var chooseFile = Intent(Intent.ACTION_OPEN_DOCUMENT)
            chooseFile.type = "*/*"
            chooseFile = Intent.createChooser(chooseFile, "Select a file to upload.")
            startActivityForResult(chooseFile, PICK_FILE_RESULT_CODE)
        })
    }

    private fun requestPermission(permission: String, requestCode: Int) {
        if (ContextCompat.checkSelfPermission(this, permission)
            != PackageManager.PERMISSION_GRANTED) {
            Log.d("MainActivity", "requestPermission(): Permission: $permission is not granted yet.")
            ActivityCompat.requestPermissions(this, arrayOf(permission), requestCode)
        } else {
            Log.d("MainActivity", "requestPermission(): Permission: $permission was already granted.")
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            PICK_FILE_RESULT_CODE -> {
                if (resultCode == -1) {
                    val fileUri = data!!.data
                    Log.d("MainActivity", "onActivityResult(): File URI: " + fileUri.toString())
                    val intent = Intent(this, UploadFileActivity::class.java)
                    intent.putExtra(Constants.FILE_URI_EXTRA, fileUri)
                    startActivity(intent)
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            REQUEST_READ_EXTERNAL_STORAGE -> {

                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty()
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("MainActivity", "onRequestPermissionsResult(): Permission was already granted.")
                } else {
                    Log.d("MainActivity", "onRequestPermissionsResult():Permission was denied.")
                }
            }
        }
    }

    companion object {
        const val PICK_FILE_RESULT_CODE = 1
        const val REQUEST_READ_EXTERNAL_STORAGE = 1
        const val REQUEST_WRITE_EXTERNAL_STORAGE = 2
    }
}
