package com.azure.android.storage.sample.kotlin

import android.app.Activity
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LifecycleOwner
import androidx.work.NetworkType
import com.azure.android.core.credential.TokenRequestObservable
import com.azure.android.core.credential.TokenRequestObservableAuthInterceptor
import com.azure.android.core.credential.TokenRequestObserver
import com.azure.android.core.credential.TokenResponseCallback
import com.azure.android.storage.blob.StorageBlobAsyncClient
import com.azure.android.storage.blob.transfer.TransferClient
import com.azure.android.storage.sample.kotlin.config.StorageConfiguration
import com.microsoft.identity.client.IMultipleAccountPublicClientApplication
import com.microsoft.identity.client.IPublicClientApplication.IMultipleAccountApplicationCreatedListener
import com.microsoft.identity.client.PublicClientApplication
import com.microsoft.identity.client.exception.MsalException

class UploadFileActivity : AppCompatActivity() {
    private lateinit var progressBar: ProgressBar
    private lateinit var storageConfiguration: StorageConfiguration
    private lateinit var storageBlobAsyncClient: StorageBlobAsyncClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_uploadfile)
        progressBar = findViewById(R.id.progressBar)

        val initStorageBlobAsyncClient: StorageBlobAsyncClient = StorageBlobAsyncClient
            .Builder("upload-file-activity")
            .setBlobServiceUrl(storageConfiguration.blobServiceUrl)
            .build()

        // Set up Login
        storageConfiguration = StorageConfiguration.create(applicationContext)
        val blobEndpointScopes = listOf(initStorageBlobAsyncClient.getBlobServiceUrl() + ".default")
        val authInterceptor = TokenRequestObservableAuthInterceptor(blobEndpointScopes)
        val tokenRequestObservable: TokenRequestObservable = authInterceptor.tokenRequestObservable
        val lifecycleOwner: LifecycleOwner = this
        PublicClientApplication.createMultipleAccountPublicClientApplication(
            this.applicationContext,
            R.raw.authorization_configuration,
            object : IMultipleAccountApplicationCreatedListener {
                override fun onCreated(application: IMultipleAccountPublicClientApplication) {
                    tokenRequestObservable.observe(lifecycleOwner, object : TokenRequestObserver() {
                        override fun onTokenRequest(scopes: Array<String>, callback: TokenResponseCallback) {
                            MsalClient.signIn(application, activity, scopes, callback)
                        }
                    })
                }

                override fun onError(exception: MsalException) {
                    Log.e(TAG, "Exception found when trying to sign in.", exception)
                }
            })

        // Create a new StorageBlobClient from the existing client with different base URL and credentials but sharing
        // the underlying OkHttp Client.
        storageBlobAsyncClient = storageBlobAsyncClient
            .newBuilder("com.azure.android.storage.sample.upload")
            .setBlobServiceUrl(storageConfiguration.blobServiceUrl)
            .setCredentialInterceptor(authInterceptor)
            .setTransferRequiredNetworkType(NetworkType.CONNECTED)
            .build()
    }

    override fun onResume() {
        super.onResume()
        val fileUri : Uri? = intent.getParcelableExtra<Uri>(Constants.FILE_URI_EXTRA)
        // Use content resolver to get file size and name.
        // https://developer.android.com/training/secure-file-sharing/retrieve-info
        val cursor = contentResolver.query(fileUri!!, null, null, null, null)
        val sizeIndex = cursor!!.getColumnIndex(OpenableColumns.SIZE)
        val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        cursor.moveToFirst()
        val fileSize = cursor.getLong(sizeIndex)
        val blobName = cursor.getString(nameIndex)
        val containerName: String? = storageConfiguration.containerName
        progressBar.max = fileSize.toInt()
        Log.d("Upload Content", "Content Uri: $fileUri")
        Log.d("Upload Content", "Blob name: $blobName")
        Log.d("Upload Content", "File size: $fileSize")
        try {
            storageBlobAsyncClient.upload(applicationContext, containerName, blobName, false, fileUri)
                .observe(this, object : TransferObserver {
                    override fun onStart(transferId: Long) {
                        Log.i(TAG, "onStart()")
                    }

                    override fun onProgress(transferId: Long, totalBytes: Long, bytesTransferred: Long) {
                        Log.i(TAG, "onProgress($totalBytes, $bytesTransferred)")
                        progressBar.progress = bytesTransferred.toInt()
                    }

                    override fun onSystemPaused(transferId: Long) {
                        Log.i(TAG, "onSystemPaused()")
                    }

                    override fun onResume(transferId: Long) {
                        Log.i(TAG, "onResumed()")
                    }

                    override fun onComplete(transferId: Long) {
                        Log.i(TAG, "onCompleted()")
                        progressBar.progress = fileSize.toInt()
                        Toast.makeText(applicationContext, "Upload complete", Toast.LENGTH_SHORT).show()
                    }

                    override fun onError(transferId: Long, errorMessage: String?) {
                        Log.i(TAG, "onError() -> : $errorMessage")
                    }
                })
        } catch (ex: Exception) {
            Log.e(TAG, "Upload submit failed: ", ex)
        }
    }

    private val activity: Activity
        get() = this

    companion object {
        private val TAG = UploadFileActivity::class.java.simpleName
    }
}

