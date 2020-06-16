package com.azure.android.storage.sample.kotlin.config

import android.content.Context
import com.azure.android.storage.sample.kotlin.R
import com.google.gson.GsonBuilder
import com.google.gson.annotations.SerializedName
import java.io.IOException

class StorageConfiguration {
    @SerializedName("blob_service_url")
    var blobServiceUrl: String? = null

    @SerializedName("container_name")
    var containerName: String? = null

    @SerializedName("sas_token")
    var sasToken: String? = null

    companion object {
        fun create(context: Context): StorageConfiguration {
            var buffer: ByteArray
            try {
                context.resources.openRawResource(R.raw.storage_configuration).use { configStream ->
                    buffer = ByteArray(configStream.available())
                    configStream.read(buffer)
                    val configBytes = String(buffer)
                    val gson = GsonBuilder().create()
                    val config = gson.fromJson(configBytes, StorageConfiguration::class.java)
                    if (!config.blobServiceUrl?.endsWith("/")!!) {
                        config.blobServiceUrl += "/"
                    }
                    return config;
                }
            } catch (e: IOException) {
                throw IllegalArgumentException("Unable to open the configuration file - 'R.raw.storage_configuration'.", e)
            }
        }
    }
}
