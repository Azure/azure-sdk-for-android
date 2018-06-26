package com.azure.data.integration.offlinetests

import android.content.Context
import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import android.util.Log
import com.azure.core.log.startLogging
import com.azure.data.AzureData
import com.azure.data.integration.azureCosmosDbAccount
import com.azure.data.integration.azureCosmosPrimaryKey
import com.azure.data.integration.offlinetests.mocks.MockOkHttpClient
import com.azure.data.model.PermissionMode
import com.azure.data.service.DocumentClient
import com.azure.data.service.ResourceCache
import com.azure.data.service.ResourceWriteOperationQueue
import okhttp3.OkHttpClient
import org.junit.After
import org.junit.Before
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
open class OfflineTests(resourceName: String) {

    protected val databaseId = "${resourceName}Database"
    protected val collectionId = "${resourceName}Collection"
    protected val documentId = "${resourceName}Document"

    protected val appContext: Context = InstrumentationRegistry.getTargetContext()

    @Before
    open fun setUp() {
        startLogging(Log.VERBOSE)

        if (!AzureData.isConfigured) {
            AzureData.configure(appContext, azureCosmosDbAccount, azureCosmosPrimaryKey, PermissionMode.All)
        }

        turnOnInternetConnection()
    }

    @After
    open fun tearDown() {
        purgeCache()
    }

    fun turnOnInternetConnection() {
        DocumentClient.client = OkHttpClient()
    }

    fun turnOffInternetConnection() {
        val client = MockOkHttpClient()
        client.hasNetworkError = true

        DocumentClient.client = client
    }

    private fun purgeCache() {
        ResourceCache.shared.purge()
        ResourceWriteOperationQueue.shared.purge()
    }
}