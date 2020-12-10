package com.azure.android.storage.sample.kotlin

import android.content.Context
import android.content.Intent
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.azure.android.storage.blob.StorageBlobAsyncClient
import com.azure.android.storage.blob.models.BlobItem
import com.azure.android.storage.sample.kotlin.config.StorageConfiguration
import java.io.File

class BlobItemViewHolder private constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val blobName: TextView = itemView.findViewById(R.id.blob_name)

    fun bind(blobItem: BlobItem?) {
        if (blobItem != null) {
            blobName.text = if (blobItem.name != null) blobItem.name else "loading..."
        }
    }

    companion object {
        private val TAG = BlobItemViewHolder::class.java.simpleName

        fun create(parent: ViewGroup, storageBlobAsyncClient: StorageBlobAsyncClient): BlobItemViewHolder {
            val storageConfiguration: StorageConfiguration = StorageConfiguration.create(parent.context)
            val blobItemView = LayoutInflater.from(parent.context).inflate(R.layout.blob_item, parent, false)
            blobItemView.setOnClickListener { view: View ->
                val blobName = (view.findViewById<View>(R.id.blob_name) as TextView).text as String
                Toast.makeText(parent.context, "Downloading $blobName", Toast.LENGTH_SHORT).show()

                val mainActivityView = parent.parent.parent.parent as View
                val progressBar = mainActivityView.findViewById<ProgressBar>(R.id.download_progress_bar)
                showProgress(mainActivityView)

                val path = Environment.getExternalStorageDirectory()
                val file = File(path, blobName)
                try {
                    val containerName: String? = storageConfiguration.containerName
                    storageBlobAsyncClient.download(parent.context, containerName, blobName, file)
                        .observe((parent.context as LifecycleOwner), object : TransferObserver {
                            override fun onStart(transferId: Long) {
                                Log.i(TAG, "onStart() for transfer with ID: $transferId")
                                val cancelButton = mainActivityView.findViewById<Button>(R.id.cancel_button)
                                cancelButton.setOnClickListener {
                                    storageBlobAsyncClient.cancel(parent.context, transferId)
                                    hideProgress(mainActivityView)
                                    Toast.makeText(parent.context, "Download cancelled", Toast.LENGTH_SHORT)
                                        .show()
                                }
                            }

                            override fun onProgress(transferId: Long, totalBytes: Long, bytesTransferred: Long) {
                                Log.i(TAG, "onProgress(" + totalBytes + ", " + bytesTransferred +
                                    ") for transfer with ID:" + transferId)
                                if (progressBar != null) {
                                    if (progressBar.progress == 0) {
                                        progressBar.max = totalBytes.toInt()
                                    }
                                    progressBar.progress = bytesTransferred.toInt()
                                }
                            }

                            override fun onSystemPaused(transferId: Long) {
                                Log.i(TAG, "onSystemPaused() for transfer with ID: $transferId")
                            }

                            override fun onResume(transferId: Long) {
                                Log.i(TAG, "onResumed() for transfer with ID: $transferId")
                            }

                            override fun onComplete(transferId: Long) {
                                Log.i(TAG, "onCompleted() for transfer with ID: $transferId")
                                if (progressBar != null) {
                                    progressBar.progress = progressBar.max
                                }
                                hideProgress(mainActivityView)
                                Toast.makeText(parent.context, "Download complete", Toast.LENGTH_SHORT).show()
                                showFileIntent(parent.context, file)
                                if (progressBar != null) {
                                    progressBar.progress = 0
                                }
                            }

                            override fun onError(transferId: Long, errorMessage: String?) {
                                Log.i(TAG, "onError() for transfer with ID: $transferId -> : $errorMessage")
                                hideProgress(mainActivityView)
                                Toast.makeText(parent.context, "Download failed", Toast.LENGTH_SHORT).show()
                                if (progressBar != null) {
                                    progressBar.progress = 0
                                }
                            }
                        })
                } catch (ex: Exception) {
                    Log.e(TAG, "Blob download failed: ", ex)
                }
            }
            return BlobItemViewHolder(blobItemView)
        }

        private fun showProgress(mainActivityView: View) {
            mainActivityView.findViewById<View>(R.id.download_background).visibility = View.VISIBLE
            mainActivityView.findViewById<View>(R.id.download_progress_bar).visibility = View.VISIBLE
            mainActivityView.findViewById<View>(R.id.download_buttons).visibility = View.VISIBLE
            mainActivityView.findViewById<View>(R.id.cancel_button).visibility = View.VISIBLE
        }

        private fun hideProgress(mainActivityView: View) {
            mainActivityView.findViewById<View>(R.id.cancel_button).visibility = View.GONE
            mainActivityView.findViewById<View>(R.id.download_buttons).visibility = View.GONE
            mainActivityView.findViewById<View>(R.id.download_progress_bar).visibility = View.GONE
            mainActivityView.findViewById<View>(R.id.download_background).visibility = View.GONE
        }

        private fun showFileIntent(context: Context, file: File) {
            val intent = Intent(Intent.ACTION_VIEW)
            val fileUri = FileProvider.getUriForFile(context, "com.azure.android.storage.sample.kotlin" + ".provider", file)
            val contentType = context.contentResolver.getType(fileUri)
            intent.setDataAndType(fileUri, contentType)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            context.startActivity(intent)
        }
    }
}

