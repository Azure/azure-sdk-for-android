package com.azure.data.integration

import android.content.Context
import android.support.test.runner.AndroidJUnit4
import com.azure.data.AzureData
import com.azure.data.integration.common.*
import com.azure.data.model.PermissionMode
import com.azure.data.util.json.gson
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */
@RunWith(AndroidJUnit4::class)
class CustomTypeAdapterTests : ResourceTest<CustomDocument>("CustomTypeAdapterTests", false, false) {

    override fun configureAzureData(appContext: Context) {

        AzureData.configure(
                appContext,
                azureCosmosDbAccount,
                azureCosmosPrimaryKey,
                PermissionMode.All) { gsonBuilder ->

            gsonBuilder.registerTypeAdapter(CustomDocument::class.java, SampleTypeAdapter())
        }
    }

    @Test
    fun testCustomTypeAdapter() {

        // test that we can use a custom, user-specified type adapter
        val newDocument = CustomDocument()

        // this adapter just returns "test" for the json and upon deserialization creates a doc with an Id of "test"
        val json = gson.toJson(newDocument)

        assertEquals(json, "\"test\"")

        val doc = gson.fromJson(json, CustomDocument::class.java)

        assertEquals(doc.id, "test")
    }
}